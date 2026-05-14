package com.example.ruleoftheday333.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.ruleoftheday333.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountFragment extends Fragment {

    EditText goalInput, habitInput, hoursInput;
    TextView saveButton, tvDifficultyLabel;
    SeekBar sliderDifficulty;
    RadioGroup radioAge;
    SwitchCompat switchKids, switchPets, switchCity, switchWFH;
    FirebaseAuth mAuth;

    // History UI
    private LinearLayout historyContainer;
    private TextView tvHistoryToggle;
    private boolean historyExpanded = false;
    private boolean historyLoaded   = false;

    private final String[] difficultyLabels = {
            "🌱 Beginner — give me easy rules",
            "🚶 Getting started — keep it simple",
            "🏃 Intermediate — I can handle more",
            "💪 Advanced — push me harder",
            "🏆 Pro — give me professional rules"
    };

    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        goalInput         = view.findViewById(R.id.goalInput);
        habitInput        = view.findViewById(R.id.habitInput);
        hoursInput        = view.findViewById(R.id.hoursInput);
        saveButton        = view.findViewById(R.id.saveButton);
        tvDifficultyLabel = view.findViewById(R.id.tvDifficultyLabel);
        sliderDifficulty  = view.findViewById(R.id.sliderDifficulty);
        radioAge          = view.findViewById(R.id.radioAge);
        switchKids        = view.findViewById(R.id.switchKids);
        switchPets        = view.findViewById(R.id.switchPets);
        switchCity        = view.findViewById(R.id.switchCity);
        switchWFH         = view.findViewById(R.id.switchWFH);

        historyContainer = view.findViewById(R.id.historyContainer);
        tvHistoryToggle  = view.findViewById(R.id.tvHistoryToggle);

        mAuth = FirebaseAuth.getInstance();

        sliderDifficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean b) {
                tvDifficultyLabel.setText(difficultyLabels[p]);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        saveButton.setOnClickListener(v -> saveUserData());

        // History dropdown toggle
        tvHistoryToggle.setOnClickListener(v -> {
            if (historyExpanded) {
                historyContainer.setVisibility(View.GONE);
                tvHistoryToggle.setText("📖 Rule History  ▼");
                historyExpanded = false;
            } else {
                historyContainer.setVisibility(View.VISIBLE);
                tvHistoryToggle.setText("📖 Rule History  ▲");
                historyExpanded = true;
                if (!historyLoaded) {
                    loadHistory();
                    historyLoaded = true;
                }
            }
        });

        loadUserData();
        return view;
    }

    // ─── Load History ─────────────────────────────────────────────────────────

    private void loadHistory() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference base = FirebaseDatabase.getInstance(
                        "https://ruleoftheday333-default-rtdb.firebaseio.com")
                .getReference("users").child(uid);

        // We need rules, ratings, moods, songs — fetch all four in parallel
        // and merge by date key
        Map<String, String>              rulesMap   = new HashMap<>();
        Map<String, Integer>             ratingsMap = new HashMap<>();
        Map<String, String>              moodsMap   = new HashMap<>();
        Map<String, List<String>>        songsMap   = new HashMap<>();
        int[] pending = {4}; // countdown — render when all 4 loaded

        Runnable tryRender = () -> {
            pending[0]--;
            if (pending[0] > 0) return;

            // Collect all dates, sort newest first
            List<String> dates = new ArrayList<>(rulesMap.keySet());
            Collections.sort(dates, Collections.reverseOrder());

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                historyContainer.removeAllViews();

                if (dates.isEmpty()) {
                    TextView empty = new TextView(getContext());
                    empty.setText("No rule history yet — start generating! 🌸");
                    empty.setTextColor(0xFFAD1457);
                    empty.setPadding(16, 16, 16, 16);
                    historyContainer.addView(empty);
                    return;
                }

                for (String date : dates) {
                    addHistoryCard(date,
                            rulesMap.get(date),
                            moodsMap.get(date),
                            ratingsMap.get(date),
                            songsMap.get(date));
                }
            });
        };

        // Load rules
        base.child("rules").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot d : s.getChildren())
                    if (d.getKey() != null && d.getValue(String.class) != null)
                        rulesMap.put(d.getKey(), d.getValue(String.class));
                tryRender.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryRender.run(); }
        });

        // Load ratings
        base.child("ratings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot d : s.getChildren()) {
                    Integer r = d.getValue(Integer.class);
                    if (d.getKey() != null && r != null) ratingsMap.put(d.getKey(), r);
                }
                tryRender.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryRender.run(); }
        });

        // Load moods
        base.child("moods").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot d : s.getChildren())
                    if (d.getKey() != null && d.getValue(String.class) != null)
                        moodsMap.put(d.getKey(), d.getValue(String.class));
                tryRender.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryRender.run(); }
        });

        // Load songs
        base.child("songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot daySnap : s.getChildren()) {
                    if (daySnap.getKey() == null) continue;
                    List<String> songList = new ArrayList<>();
                    for (DataSnapshot song : daySnap.getChildren()) {
                        String val = song.getValue(String.class);
                        if (val != null) songList.add(val);
                    }
                    songsMap.put(daySnap.getKey(), songList);
                }
                tryRender.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryRender.run(); }
        });
    }

    // ─── History Card UI ──────────────────────────────────────────────────────

    private void addHistoryCard(String date, String rule, String mood,
                                Integer rating, List<String> songs) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24, 20, 24, 20);
        card.setBackgroundResource(R.drawable.song_card_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        // Date header
        TextView tvDate = new TextView(getContext());
        tvDate.setText("📅 " + date);
        tvDate.setTextSize(13);
        tvDate.setTextColor(0xFF880E4F);
        tvDate.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvDate);

        // Rule
        if (rule != null) {
            TextView tvRule = new TextView(getContext());
            tvRule.setText("📌 " + rule);
            tvRule.setTextSize(14);
            tvRule.setTextColor(0xFF111111);
            tvRule.setPadding(0, 8, 0, 4);
            card.addView(tvRule);
        }

        // Mood + rating on same row
        LinearLayout metaRow = new LinearLayout(getContext());
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        metaRow.setPadding(0, 4, 0, 4);

        if (mood != null && !mood.isEmpty()) {
            String moodEmoji = mood.equals("Tired") ? "😴"
                    : mood.equals("Okay")  ? "😐"
                    : mood.equals("Great") ? "😊" : "🔥";
            TextView tvMood = new TextView(getContext());
            tvMood.setText(moodEmoji + " " + mood);
            tvMood.setTextSize(13);
            tvMood.setTextColor(0xFFAD1457);
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvMood.setLayoutParams(mp);
            metaRow.addView(tvMood);
        }

        if (rating != null && rating > 0) {
            TextView tvRating = new TextView(getContext());
            tvRating.setText("⭐".repeat(rating));
            tvRating.setTextSize(13);
            metaRow.addView(tvRating);
        }

        card.addView(metaRow);

        // Songs
        if (songs != null && !songs.isEmpty()) {
            TextView tvSongsLabel = new TextView(getContext());
            tvSongsLabel.setText("🎵 Matched songs:");
            tvSongsLabel.setTextSize(12);
            tvSongsLabel.setTextColor(0xFFAD1457);
            tvSongsLabel.setPadding(0, 6, 0, 2);
            card.addView(tvSongsLabel);

            for (String song : songs) {
                TextView tvSong = new TextView(getContext());
                tvSong.setText("  • " + song);
                tvSong.setTextSize(12);
                tvSong.setTextColor(0xFF555555);
                card.addView(tvSong);
            }
        }

        historyContainer.addView(card);
    }

    // ─── Load Profile ─────────────────────────────────────────────────────────

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance("https://ruleoftheday333-default-rtdb.firebaseio.com")
                .getReference("users").child(uid).child("profile")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.child("goal").getValue() != null)
                            goalInput.setText(snap.child("goal").getValue(String.class));
                        if (snap.child("habit").getValue() != null)
                            habitInput.setText(snap.child("habit").getValue(String.class));
                        if (snap.child("hoursPerDay").getValue() != null)
                            hoursInput.setText(snap.child("hoursPerDay").getValue(String.class));

                        Long diff = snap.child("difficulty").getValue(Long.class);
                        if (diff != null) {
                            sliderDifficulty.setProgress(diff.intValue());
                            tvDifficultyLabel.setText(difficultyLabels[diff.intValue()]);
                        }

                        String age = snap.child("ageRange").getValue(String.class);
                        if (age != null) {
                            switch (age) {
                                case "<18":   radioAge.check(R.id.ageUnder18); break;
                                case "18-25": radioAge.check(R.id.age18to25);  break;
                                case "26-35": radioAge.check(R.id.age26to35);  break;
                                case "36+":   radioAge.check(R.id.age36plus);  break;
                            }
                        }

                        Boolean kids = snap.child("hasKids").getValue(Boolean.class);
                        Boolean pets = snap.child("hasPets").getValue(Boolean.class);
                        Boolean city = snap.child("inCity").getValue(Boolean.class);
                        Boolean wfh  = snap.child("workFromHome").getValue(Boolean.class);
                        if (kids != null) switchKids.setChecked(kids);
                        if (pets != null) switchPets.setChecked(pets);
                        if (city != null) switchCity.setChecked(city);
                        if (wfh  != null) switchWFH.setChecked(wfh);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // ─── Save Profile ─────────────────────────────────────────────────────────

    private void saveUserData() {
        String goal  = goalInput.getText().toString().trim();
        String habit = habitInput.getText().toString().trim();
        String hours = hoursInput.getText().toString().trim();

        if (goal.isEmpty() || habit.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), getString(R.string.toast_user_not_logged_in), Toast.LENGTH_SHORT).show();
            return;
        }

        String ageRange = "";
        int ageId = radioAge.getCheckedRadioButtonId();
        if      (ageId == R.id.ageUnder18) ageRange = "<18";
        else if (ageId == R.id.age18to25)  ageRange = "18-25";
        else if (ageId == R.id.age26to35)  ageRange = "26-35";
        else if (ageId == R.id.age36plus)  ageRange = "36+";

        Map<String, Object> profile = new HashMap<>();
        profile.put("goal",         goal);
        profile.put("habit",        habit);
        profile.put("hoursPerDay",  hours.isEmpty() ? "0" : hours);
        profile.put("difficulty",   sliderDifficulty.getProgress());
        profile.put("ageRange",     ageRange);
        profile.put("hasKids",      switchKids.isChecked());
        profile.put("hasPets",      switchPets.isChecked());
        profile.put("inCity",       switchCity.isChecked());
        profile.put("workFromHome", switchWFH.isChecked());

        String uid = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance("https://ruleoftheday333-default-rtdb.firebaseio.com")
                .getReference("users").child(uid).child("profile")
                .updateChildren(profile)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), getString(R.string.toast_saved), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), getString(R.string.toast_error) + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}