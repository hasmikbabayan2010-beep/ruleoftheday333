package com.example.ruleoftheday333.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ruleoftheday333.R;
import com.example.ruleoftheday333.itunes.ItunesPreviewHelper;
import com.example.ruleoftheday333.spotify.SpotifyHelper;
import com.example.ruleoftheday333.spotify.SpotifyTrack;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private TextView ruleText;
    private Button generateRule, btnFollowed, btnNotFollowed;
    private LinearLayout matchedSongsContainer;
    private ExoPlayer exoPlayer;
    private PlayerView playerView;

    // Fixed: 4 threads so Spotify + iTunes calls don't block each other
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private int currentRuleTemp = 50;

    private static final String GEMINI_API_KEY = "AIzaSyBKfL2laBbVg2tD0n7TZyvbA6F36rGlYMo";

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ruleText       = view.findViewById(R.id.ruleText);
        generateRule   = view.findViewById(R.id.generateRule);
        btnFollowed    = view.findViewById(R.id.btnFollowed);
        btnNotFollowed = view.findViewById(R.id.btnNotFollowed);

        matchedSongsContainer = new LinearLayout(getContext());
        matchedSongsContainer.setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) view.findViewById(R.id.rvMatchedSongs).getParent())
                .addView(matchedSongsContainer);

        playerView = new PlayerView(getContext());
        exoPlayer  = new ExoPlayer.Builder(getContext()).build();
        playerView.setPlayer(exoPlayer);
        ((LinearLayout) view.findViewById(R.id.rvMatchedSongs).getParent())
                .addView(playerView);

        generateRule.setOnClickListener(v -> generateAIRule());
        btnFollowed.setOnClickListener(v -> saveDayStatus("green"));
        btnNotFollowed.setOnClickListener(v -> saveDayStatus("red"));

        return view;
    }

    private void generateAIRule() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            ruleText.setText("Please log in first!");
            return;
        }

        ruleText.setText("✨ Generating your rule...");
        generateRule.setEnabled(false);
        matchedSongsContainer.removeAllViews();
        exoPlayer.stop();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance("https://ruleoftheday333-default-rtdb.firebaseio.com")
                .getReference("users")
                .child(userId)
                .child("profile")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String goal  = snapshot.child("goal").getValue(String.class);
                        String habit = snapshot.child("habit").getValue(String.class);

                        if (goal  == null || goal.isEmpty())  goal  = "general self-improvement";
                        if (habit == null || habit.isEmpty()) habit = "building better daily habits";

                        generateRuleWithAI(goal, habit);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        generateRuleWithAI("general self-improvement", "building better daily habits");
                    }
                });
    }

    private void generateRuleWithAI(String goal, String habit) {
        executorService.execute(() -> {

            String prompt =
                    "The user's main goal is: " + goal + ".\n" +
                            "The habit they want to work on is: " + habit + ".\n\n" +
                            "Generate a short, personalized daily rule that helps them work toward this goal. " +
                            "Respond in EXACTLY this format on three separate lines, nothing else:\n" +
                            "RULE: <one sentence, personalized to their goal>\n" +
                            "TEMP: <a number 10-90: 10=very calm, 50=balanced, 90=high energy>\n" +
                            "MUSIC: <a short mood/genre phrase for Spotify, e.g. 'calm focus', 'upbeat pop', 'jazz morning'>";

            GenerativeModel gm = new GenerativeModel("gemini-2.0-flash", GEMINI_API_KEY);
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content content = new Content.Builder()
                    .addText(prompt)
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String raw = result.getText().trim();

                    String ruleStr  = extractLine(raw, "RULE:");
                    String tempStr  = extractLine(raw, "TEMP:");
                    String musicStr = extractLine(raw, "MUSIC:");

                    int temp = 50;
                    try { temp = Integer.parseInt(tempStr.trim()); } catch (Exception ignored) {}
                    final int    finalTemp  = Math.max(10, Math.min(90, temp));
                    final String finalRule  = ruleStr.isEmpty()  ? raw     : ruleStr;
                    final String finalMusic = musicStr.isEmpty() ? "chill" : musicStr;

                    requireActivity().runOnUiThread(() -> {
                        currentRuleTemp = finalTemp;
                        ruleText.setText(finalRule + "\n\n🔥 Energy: " + finalTemp);
                        generateRule.setEnabled(true);
                        loadSongsAsync(finalMusic, finalTemp);
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    android.util.Log.e("GeminiError", "API call failed: " + t.getMessage(), t);
                    requireActivity().runOnUiThread(() -> {
                        ruleText.setText("⚠️ Error: " + t.getMessage());
                        generateRule.setEnabled(true);
                    });
                }
            }, executorService);
        });
    }

    private void loadSongsAsync(String searchTerm, int ruleTemp) {
        executorService.execute(() -> {

            // Fetch from Spotify with a 10-second timeout (set in SpotifyHelper)
            List<SpotifyTrack> spotifyTracks = SpotifyHelper.searchTracks(searchTerm, 10);

            if (spotifyTracks == null || spotifyTracks.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "No songs found on Spotify 😕", Toast.LENGTH_SHORT).show());
                return;
            }

            // Fixed: snapshot the original order BEFORE sorting
            // so indexOf() gives stable results
            final List<SpotifyTrack> original = new ArrayList<>(spotifyTracks);
            int n = original.size();

            spotifyTracks.sort((a, b) -> {
                int idxA    = original.indexOf(a);
                int idxB    = original.indexOf(b);
                int energyA = 80 - (idxA * 60 / Math.max(n - 1, 1));
                int energyB = 80 - (idxB * 60 / Math.max(n - 1, 1));
                return Math.abs(energyA - ruleTemp) - Math.abs(energyB - ruleTemp);
            });

            // Fetch iTunes previews for top 3 — each on a separate thread
            // so one slow/failed iTunes call doesn't block the others
            int limit = Math.min(3, spotifyTracks.size());
            for (int i = 0; i < limit; i++) {
                final SpotifyTrack track = spotifyTracks.get(i);
                executorService.execute(() -> {
                    String previewUrl = ItunesPreviewHelper.fetchPreviewUrl(
                            track.trackName, track.artistName);
                    final String finalPreview = previewUrl;
                    requireActivity().runOnUiThread(() -> addSongCard(track, finalPreview));
                });
            }
        });
    }

    private void addSongCard(SpotifyTrack track, String previewUrl) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(16, 16, 16, 16);
        card.setBackgroundResource(R.drawable.song_card_background);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = 16;
        card.setLayoutParams(cardParams);

        ImageView cover = new ImageView(getContext());
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(150, 150);
        imgParams.setMarginEnd(16);
        cover.setLayoutParams(imgParams);
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this)
                .load(track.albumArtUrl)
                .placeholder(R.drawable.placeholder_album_foreground)
                .into(cover);

        LinearLayout info = new LinearLayout(getContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(getContext());
        title.setText(track.trackName);
        title.setTextSize(16);
        title.setTextColor(0xFFAD1457);

        TextView artist = new TextView(getContext());
        artist.setText(track.artistName);
        artist.setTextSize(14);
        artist.setTextColor(0xFF880E4F);

        LinearLayout btnRow = new LinearLayout(getContext());
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = 8;
        btnRow.setLayoutParams(rowParams);

        Button previewBtn = new Button(getContext());
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        previewParams.setMarginEnd(8);
        previewBtn.setLayoutParams(previewParams);
        previewBtn.setTextColor(0xFFFFFFFF);
        previewBtn.setTextSize(12);

        if (previewUrl != null) {
            previewBtn.setText("▶ Preview");
            previewBtn.setBackgroundColor(0xFFF48FB1);
            previewBtn.setOnClickListener(v -> playPreview(previewUrl));
        } else {
            previewBtn.setText("No Preview");
            previewBtn.setBackgroundColor(0xFFCCCCCC);
            previewBtn.setEnabled(false);
        }

        Button spotifyBtn = new Button(getContext());
        spotifyBtn.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        spotifyBtn.setText("🎵 Spotify");
        spotifyBtn.setBackgroundColor(0xFF1DB954);
        spotifyBtn.setTextColor(0xFFFFFFFF);
        spotifyBtn.setTextSize(12);
        spotifyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(track.spotifyUrl));
            startActivity(intent);
        });

        btnRow.addView(previewBtn);
        btnRow.addView(spotifyBtn);

        info.addView(title);
        info.addView(artist);
        info.addView(btnRow);

        card.addView(cover);
        card.addView(info);
        matchedSongsContainer.addView(card);
    }

    private void playPreview(String previewUrl) {
        if (previewUrl == null || previewUrl.isEmpty()) return;
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(previewUrl)));
        exoPlayer.prepare();
        exoPlayer.play();
    }

    private String extractLine(String text, String prefix) {
        for (String line : text.split("\n")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        exoPlayer.release();
        executorService.shutdownNow();
    }

    private void saveDayStatus(String status) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please log in first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String today  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("calendar")
                .child(today);

        ref.setValue(status).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Saved for today ✅", Toast.LENGTH_SHORT).show();
                btnFollowed.setEnabled(false);
                btnNotFollowed.setEnabled(false);
            } else {
                Toast.makeText(getContext(), "Failed to save ❌", Toast.LENGTH_SHORT).show();
            }
        });
    }
}