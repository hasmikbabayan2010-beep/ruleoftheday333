package com.example.ruleoftheday333.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ruleoftheday333.R;
import com.example.ruleoftheday333.itunes.ItunesPreviewHelper;
import com.example.ruleoftheday333.spotify.SpotifyHelper;
import com.example.ruleoftheday333.spotify.SpotifyTrack;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map;
import java.util.concurrent.*;

import com.example.ruleoftheday333.share.ShareCardRenderer;
import com.example.ruleoftheday333.share.ShareHelper;

public class HomeFragment extends Fragment {

    private TextView ruleText;
    private Button generateRule, btnFollowed, btnNotFollowed;
    private LinearLayout matchedSongsContainer;

    private ExoPlayer exoPlayer;
    private Button currentPlayingBtn;
    private Button rateRuleBtn;
    private int    lastRating = 0; // 0 = not yet rated

    // Mood check-in
    private String selectedMood = ""; // "Tired" | "Okay" | "Great" | "Hyped"
    private Button lastMoodBtn  = null;

    // Rule history tracking
    private final List<String> todaySongNames = new ArrayList<>();

    // Share card state — updated as rule/songs load
    private String lastRuleText   = "";
    private String lastSongName   = "";
    private String lastArtistName = "";
    private String lastAlbumArtUrl= "";
    private int    currentStreak  = 0;

    private int currentRuleTemp = 50; // temperature of the current rule (0-100)

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    // ⚠️ Put your Groq API key here
//    private static final String GROQ_API_KEY = "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";
    private static final String GROQ_API_URL  = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL    = "llama-3.1-8b-instant";

    // Helper: a Spotify track paired with a random temperature for sorting
    private static class SongWithTemp {
        SpotifyTrack track;
        int temperature;
        SongWithTemp(SpotifyTrack track, int temperature) {
            this.track = track;
            this.temperature = temperature;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ruleText              = view.findViewById(R.id.ruleText);
        generateRule          = view.findViewById(R.id.generateRule);
        btnFollowed           = view.findViewById(R.id.btnFollowed);
        btnNotFollowed        = view.findViewById(R.id.btnNotFollowed);
        matchedSongsContainer = view.findViewById(R.id.rvMatchedSongs);

        // Notes + share button are in fragment_home.xml — just wire them up
        EditText notesInput = view.findViewById(R.id.notesInput);
        Button shareBtn = view.findViewById(R.id.btnShare);
        shareBtn.setOnClickListener(v -> {
            String note = notesInput.getText().toString().trim();
            generateAndShareCard(note);
        });

        rateRuleBtn = view.findViewById(R.id.btnRateRule);
        rateRuleBtn.setOnClickListener(v -> showRatingDialog());

        // Mood buttons
        int[] moodIds = {R.id.btnMoodTired, R.id.btnMoodOkay, R.id.btnMoodGreat, R.id.btnMoodHyped};
        String[] moods = {"Tired", "Okay", "Great", "Hyped"};
        for (int i = 0; i < moodIds.length; i++) {
            Button moodBtn = view.findViewById(moodIds[i]);
            String mood = moods[i];
            moodBtn.setOnClickListener(v -> {
                // Deselect previous
                if (lastMoodBtn != null) {
                    lastMoodBtn.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
                    lastMoodBtn.setTextColor(0xFFAD1457);
                }
                // Select this one
                moodBtn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFF48FB1));
                moodBtn.setTextColor(0xFFFFFFFF);
                selectedMood = mood;
                lastMoodBtn  = moodBtn;
                saveMood(mood);
            });
        }

        // Headless ExoPlayer — no PlayerView needed for audio-only
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();

        generateRule.setOnClickListener(v -> generateAIRule());
        btnFollowed.setOnClickListener(v -> saveDayStatus("green"));
        btnNotFollowed.setOnClickListener(v -> saveDayStatus("red"));

        return view;
    }

    // ─── Step 1: fetch user profile from Firebase ────────────────────────────

    private void generateAIRule() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            ruleText.setText("Please log in first!");
            return;
        }

        ruleText.setText("✨ Generating...");
        generateRule.setEnabled(false);

        matchedSongsContainer.removeAllViews();
        currentPlayingBtn = null;
        lastSongName = "";
        lastArtistName = "";
        lastAlbumArtUrl = "";
        todaySongNames.clear();
        lastRating = 0;
        if (rateRuleBtn != null) rateRuleBtn.setText("⭐ Rate this rule");

        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
        }

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("profile")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String goal  = snapshot.child("goal").getValue(String.class);
                        String habit = snapshot.child("habit").getValue(String.class);
                        if (goal  == null) goal  = "self-improvement";
                        if (habit == null) habit = "better habits";
                        final String finalGoal  = goal;
                        final String finalHabit = habit;
                        // Fetch last 5 ratings to personalize Groq prompt
                        fetchRecentRatings(finalGoal, finalHabit, selectedMood);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        generateRuleWithGroq("self-improvement", "better habits", "", selectedMood);
                    }
                });
    }

    // ─── Step 2: Groq generates rule + temperature + music mood ──────────────

    private void generateRuleWithGroq(String goal, String habit, String ratingContext, String mood) {
        executor.execute(() -> {
            try {
                String userMessage =
                        "Goal: " + goal + "\n" +
                                "Habit: " + habit + "\n" +
                                (mood.isEmpty() ? "" : "User mood right now: " + mood + ". " +
                                        "If mood is Tired, give a gentle low-energy rule and calm music. " +
                                        "If mood is Okay, give a balanced rule and chill music. " +
                                        "If mood is Great, give an uplifting rule and upbeat music. " +
                                        "If mood is Hyped, give a bold challenging rule and energetic music.\n") +
                                (ratingContext.isEmpty() ? "" : ratingContext + "\n") + "\n" +
                                "Give me one short daily rule to follow, a temperature (integer 0-100 " +
                                "representing energy level: 0=very calm, 100=very energetic), " +
                                "and a music genre/mood that fits that energy.\n" +
                                "Return ONLY in this exact format, no extra text:\n" +
                                "RULE: <the rule>\n" +
                                "TEMP: <integer 0-100>\n" +
                                "MUSIC: <genre or mood>";

                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", userMessage);

                JSONArray messages = new JSONArray();
                messages.put(message);

                JSONObject body = new JSONObject();
                body.put("model", GROQ_MODEL);
                body.put("messages", messages);
                body.put("max_tokens", 150);
                body.put("temperature", 0.8);

                URL url = new URL(GROQ_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                InputStream stream = responseCode == 200
                        ? conn.getInputStream() : conn.getErrorStream();

                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }

                if (responseCode != 200) {
                    throw new IOException("Groq error " + responseCode + ": " + sb);
                }

                JSONObject json   = new JSONObject(sb.toString());
                String rawText    = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                String rule  = extractLine(rawText, "RULE:");
                String music = extractLine(rawText, "MUSIC:");
                String tempStr = extractLine(rawText, "TEMP:");

                // Parse temperature, fallback to 50
                int temp = 50;
                try { temp = Integer.parseInt(tempStr.trim()); } catch (Exception ignored) {}
                temp = Math.max(0, Math.min(100, temp));
                final int ruleTemp = temp;

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    currentRuleTemp = ruleTemp;
                    lastRuleText = rule.isEmpty() ? rawText : rule;
                    ruleText.setText(lastRuleText);
                    generateRule.setEnabled(true);
                    saveRuleToHistory(lastRuleText);
                    if (!music.isEmpty()) loadSongs(music);
                });

            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    ruleText.setText("Error: " + e.getMessage());
                    generateRule.setEnabled(true);
                });
            }
        });
    }

    // ─── Step 3: Spotify search → temperature sort → preview fetch ───────────

    private void loadSongs(String moodKeyword) {
        executor.execute(() -> {

            // Fetch 20 Spotify tracks matching the mood keyword
            List<SpotifyTrack> tracks = SpotifyHelper.searchTracks(moodKeyword, 20);

            if (tracks == null || tracks.isEmpty()) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "No songs found", Toast.LENGTH_SHORT).show());
                return;
            }

            // Assign a random temperature (0-99) to each track, just like the
            // original working version — this gives variety while keeping
            // results mood-relevant (Spotify already filtered by keyword)
            Random rand = new Random();
            List<SongWithTemp> songsWithTemp = new ArrayList<>();
            for (SpotifyTrack track : tracks) {
                songsWithTemp.add(new SongWithTemp(track, rand.nextInt(100)));
            }

            // Sort by closeness to the rule's temperature
            songsWithTemp.sort(Comparator.comparingInt(s ->
                    Math.abs(s.temperature - currentRuleTemp)));

            // Take top 3 after sorting
            List<SpotifyTrack> finalTracks = new ArrayList<>();
            for (int i = 0; i < Math.min(3, songsWithTemp.size()); i++) {
                finalTracks.add(songsWithTemp.get(i).track);
            }

            // For each track: try Spotify preview first, then iTunes fallback
            for (SpotifyTrack track : finalTracks) {
                executor.execute(() -> {
                    String previewUrl = track.previewUrl;

                    if (previewUrl == null || previewUrl.isEmpty()) {
                        previewUrl = ItunesPreviewHelper.fetchPreviewUrl(
                                requireContext(), track.trackName, track.artistName);
                    }

                    if (!isAdded()) return;
                    final String finalUrl = previewUrl;
                    requireActivity().runOnUiThread(() -> addSongCard(track, finalUrl));
                });
            }
        });
    }

    // ─── UI: song card ────────────────────────────────────────────────────────

    private void addSongCard(SpotifyTrack track, String previewUrl) {
        if (!isAdded()) return;

        // Save first song's info for share card
        if (lastSongName.isEmpty()) {
            lastSongName    = track.trackName;
            lastArtistName  = track.artistName;
            lastAlbumArtUrl = track.albumArtUrl;
        }
        // Track all song names for history
        todaySongNames.add(track.trackName + " – " + track.artistName);
        saveSongsToHistory(todaySongNames);

        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(16, 16, 16, 16);
        card.setBackgroundResource(R.drawable.song_card_background);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);

        // Album art
        ImageView cover = new ImageView(getContext());
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(150, 150);
        imgParams.setMarginEnd(16);
        cover.setLayoutParams(imgParams);
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this)
                .load(track.albumArtUrl)

                .into(cover);

        // Text column
        LinearLayout textCol = new LinearLayout(getContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(textParams);
        textCol.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView trackName = new TextView(getContext());
        trackName.setText(track.trackName);
        trackName.setTextSize(15);
        trackName.setTypeface(null, android.graphics.Typeface.BOLD);
        trackName.setTextColor(0xFFAD1457);
        trackName.setMaxLines(1);
        trackName.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView artistName = new TextView(getContext());
        artistName.setText(track.artistName);
        artistName.setTextSize(13);
        artistName.setTextColor(0xFF880E4F);
        artistName.setMaxLines(1);
        artistName.setEllipsize(android.text.TextUtils.TruncateAt.END);

        textCol.addView(trackName);
        textCol.addView(artistName);

        // Play/pause button
        Button btn = new Button(getContext());
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setBackgroundColor(0xFFF48FB1);
        btn.setTextColor(0xFFFFFFFF);

        if (previewUrl != null) {
            btn.setText("▶");
            btn.setOnClickListener(v -> {
                if (currentPlayingBtn != null && currentPlayingBtn != btn) {
                    currentPlayingBtn.setText("▶");
                }
                if (btn.getText().equals("▶")) {
                    btn.setText("⏸");
                    currentPlayingBtn = btn;
                    playPreview(previewUrl);
                } else {
                    btn.setText("▶");
                    currentPlayingBtn = null;
                    if (exoPlayer != null) exoPlayer.pause();
                }
            });
        } else {
            btn.setText("—");
            btn.setEnabled(false);
        }

        card.addView(cover);
        card.addView(textCol);
        card.addView(btn);
        matchedSongsContainer.addView(card);
    }

    private void playPreview(String url) {
        if (exoPlayer == null || url == null) return;
        exoPlayer.stop();
        exoPlayer.clearMediaItems();
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        exoPlayer.prepare();
        exoPlayer.play();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String extractLine(String text, String key) {
        if (text == null) return "";
        for (String line : text.split("\n")) {
            if (line.startsWith(key)) return line.replace(key, "").trim();
        }
        return "";
    }

    private void saveDayStatus(String status) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("calendar")
                .child(today)
                .setValue(status);
    }

    // ─── Share card ───────────────────────────────────────────────────────────

    private void generateAndShareCard(String userNote) {
        if (lastRuleText.isEmpty()) {
            android.widget.Toast.makeText(getContext(),
                    "Generate a rule first!", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        android.widget.Toast.makeText(getContext(),
                "Creating share card...", android.widget.Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                android.graphics.Bitmap card = ShareCardRenderer.render(
                        requireContext(),
                        "Rule of the Day",
                        lastRuleText,
                        lastSongName,
                        lastArtistName,
                        lastAlbumArtUrl,
                        currentStreak,
                        userNote
                );

                String filename = "rule_" + System.currentTimeMillis();
                ShareHelper.saveAndShare(requireContext(), card, filename);

            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        android.widget.Toast.makeText(getContext(),
                                "Share failed: " + e.getMessage(),
                                android.widget.Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ─── Ratings ─────────────────────────────────────────────────────────────

    private void fetchRecentRatings(String goal, String habit, String mood) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { generateRuleWithGroq(goal, habit, "", mood); return; }

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("ratings")
                .orderByKey()
                .limitToLast(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        StringBuilder ratingContext = new StringBuilder();
                        int count = 0;
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Integer rating = s.getValue(Integer.class);
                            if (rating != null) {
                                ratingContext.append(rating).append("/5, ");
                                count++;
                            }
                        }
                        String context = count > 0
                                ? "The user's last " + count + " rule ratings were: "
                                + ratingContext.toString().replaceAll(", $", "") + ". "
                                + "If ratings are low (1-2), make the rule easier and more fun. "
                                + "If ratings are high (4-5), make the rule more challenging."
                                : "";
                        generateRuleWithGroq(goal, habit, context, mood);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        generateRuleWithGroq(goal, habit, "", mood);
                    }
                });
    }

    private void showRatingDialog() {
        if (lastRuleText.isEmpty()) {
            Toast.makeText(getContext(), "Generate a rule first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build a simple star rating dialog
        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Rate today's rule");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(32, 32, 32, 16);

        Button[] stars = new Button[5];
        int[] selectedRating = {lastRating > 0 ? lastRating : 0};

        for (int i = 0; i < 5; i++) {
            Button star = new Button(getContext());
            star.setText(i < selectedRating[0] ? "⭐" : "☆");
            star.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            star.setTextSize(28);
            stars[i] = star;
            final int rating = i + 1;
            star.setOnClickListener(v -> {
                selectedRating[0] = rating;
                for (int j = 0; j < 5; j++) {
                    stars[j].setText(j < rating ? "⭐" : "☆");
                }
            });
            layout.addView(star);
        }

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            if (selectedRating[0] > 0) {
                lastRating = selectedRating[0];
                rateRuleBtn.setText("⭐ ".repeat(lastRating).trim());
                saveRating(lastRating);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveRating(int rating) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("ratings")
                .child(today)
                .setValue(rating);
    }

    // ─── History saving ──────────────────────────────────────────────────────

    private void saveRuleToHistory(String rule) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("rules")
                .child(today)
                .setValue(rule);
    }

    private void saveSongsToHistory(List<String> songs) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Map<String, Object> songMap = new HashMap<>();
        for (int i = 0; i < songs.size(); i++) {
            songMap.put(String.valueOf(i), songs.get(i));
        }
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("songs")
                .child(today)
                .setValue(songMap);
    }

    private void saveMood(String mood) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("moods")
                .child(today)
                .setValue(mood);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        executor.shutdownNow();
    }
}