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
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.*;
import com.google.ai.client.generativeai.java.*;
import com.google.ai.client.generativeai.type.*;
import com.google.common.util.concurrent.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class HomeFragment extends Fragment {

    private TextView ruleText;
    private Button generateRule, btnFollowed, btnNotFollowed;

//    private RecyclerView matchedSongsContainer;
    private LinearLayout matchedSongsContainer;

    private ExoPlayer exoPlayer;
    private PlayerView playerView;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(3);

    private static final String GEMINI_API_KEY = "YOUR_KEY_HERE";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ruleText = view.findViewById(R.id.ruleText);
        generateRule = view.findViewById(R.id.generateRule);
        btnFollowed = view.findViewById(R.id.btnFollowed);
        btnNotFollowed = view.findViewById(R.id.btnNotFollowed);

        matchedSongsContainer = view.findViewById(R.id.rvMatchedSongs);

        // Player setup
        playerView = new PlayerView(requireContext());
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        playerView.setPlayer(exoPlayer);

        matchedSongsContainer.addView(playerView);

        generateRule.setOnClickListener(v -> generateAIRule());
        btnFollowed.setOnClickListener(v -> saveDayStatus("green"));
        btnNotFollowed.setOnClickListener(v -> saveDayStatus("red"));

        return view;
    }

    private void generateAIRule() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            ruleText.setText("Please log in first!");
            return;
        }

        ruleText.setText("✨ Generating...");
        generateRule.setEnabled(false);

        matchedSongsContainer.removeAllViews();
        matchedSongsContainer.addView(playerView);

        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
        }

        String userId = user.getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("profile")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        String goal = snapshot.child("goal").getValue(String.class);
                        String habit = snapshot.child("habit").getValue(String.class);

                        if (goal == null) goal = "self-improvement";
                        if (habit == null) habit = "better habits";

                        generateRuleWithAI(goal, habit);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        generateRuleWithAI("self-improvement", "better habits");
                    }
                });
    }

    private void generateRuleWithAI(String goal, String habit) {

        executor.execute(() -> {

            try {
                String prompt =
                        "Goal: " + goal + "\n" +
                                "Habit: " + habit + "\n\n" +
                                "Return format:\nRULE:\nMUSIC:";

                GenerativeModel model =
                        new GenerativeModel("gemini-2.0-flash", GEMINI_API_KEY);

                GenerativeModelFutures futures =
                        GenerativeModelFutures.from(model);

                Content content = new Content.Builder()
                        .addText(prompt)
                        .build();

                ListenableFuture<GenerateContentResponse> response =
                        futures.generateContent(content);

                Futures.addCallback(response,
                        new FutureCallback<GenerateContentResponse>() {

                            @Override
                            public void onSuccess(GenerateContentResponse result) {

                                if (!isAdded()) return;

                                String raw = result.getText();

                                String rule = extractLine(raw, "RULE:");
                                String music = extractLine(raw, "MUSIC:");

                                requireActivity().runOnUiThread(() -> {
                                    if (!isAdded()) return;

                                    ruleText.setText(rule);
                                    generateRule.setEnabled(true);

                                    loadSongs(music);
                                });
                            }

                            @Override
                            public void onFailure(Throwable t) {

                                if (!isAdded()) return;

                                requireActivity().runOnUiThread(() -> {
                                    ruleText.setText("Error: " + t.getMessage());
                                    generateRule.setEnabled(true);
                                });
                            }

                        }, executor);

            } catch (Exception e) {

                requireActivity().runOnUiThread(() -> {
                    ruleText.setText("AI Error: " + e.getMessage());
                    generateRule.setEnabled(true);
                });
            }
        });
    }

    private void loadSongs(String searchTerm) {

        executor.execute(() -> {

            List<SpotifyTrack> tracks =
                    SpotifyHelper.searchTracks(searchTerm, 10);

            if (tracks == null || tracks.isEmpty()) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "No songs found", Toast.LENGTH_SHORT).show());
                return;
            }

            int limit = Math.min(3, tracks.size());

            for (int i = 0; i < limit; i++) {

                SpotifyTrack track = tracks.get(i);

                executor.execute(() -> {

                    String previewUrl =
                            ItunesPreviewHelper.fetchPreviewUrl(
                                    requireContext(),
                                    track.trackName,
                                    track.artistName
                            );

                    if (!isAdded()) return;

                    requireActivity().runOnUiThread(() ->
                            addSongCard(track, previewUrl)
                    );
                });
            }
        });
    }

    private void addSongCard(SpotifyTrack track, String previewUrl) {

        if (!isAdded()) return;

        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.HORIZONTAL);

        ImageView cover = new ImageView(getContext());
        cover.setLayoutParams(new LinearLayout.LayoutParams(150, 150));

        Glide.with(this)
                .load(track.albumArtUrl)
                .into(cover);

        Button btn = new Button(getContext());

        if (previewUrl != null) {
            btn.setText("▶");
            btn.setOnClickListener(v -> playPreview(previewUrl));
        } else {
            btn.setText("No");
            btn.setEnabled(false);
        }

        card.addView(cover);
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

    private String extractLine(String text, String key) {

        if (text == null) return "";

        for (String line : text.split("\n")) {
            if (line.startsWith(key)) {
                return line.replace(key, "").trim();
            }
        }
        return "";
    }

    private void saveDayStatus(String status) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String today = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(new Date());

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