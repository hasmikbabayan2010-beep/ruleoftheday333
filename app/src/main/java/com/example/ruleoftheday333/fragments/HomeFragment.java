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
import java.util.concurrent.*;

public class HomeFragment extends Fragment {

    private TextView ruleText;
    private Button generateRule, btnFollowed, btnNotFollowed;
    private LinearLayout matchedSongsContainer;

    private ExoPlayer exoPlayer;
    private Button currentPlayingBtn;

    private int currentRuleTemp = 50; // temperature of the current rule (0-100)

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    // ⚠️ Put your Groq API key here
<<<<<<< HEAD
    private static final String GROQ_API_KEY = "YOUR_GROQ_KEY_HERE";
=======
    private static final String GROQ_API_KEY = "ESTEX GREL APIY AMEN ANGAM VOR ASHXATI";
>>>>>>> 952a867 (wleDVXLQIEFD)
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
                        generateRuleWithGroq(goal, habit);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        generateRuleWithGroq("self-improvement", "better habits");
                    }
                });
    }

    // ─── Step 2: Groq generates rule + temperature + music mood ──────────────

    private void generateRuleWithGroq(String goal, String habit) {
        executor.execute(() -> {
            try {
                String userMessage =
                        "Goal: " + goal + "\n" +
                                "Habit: " + habit + "\n\n" +
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
                    ruleText.setText(rule.isEmpty() ? rawText : rule);
                    generateRule.setEnabled(true);
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