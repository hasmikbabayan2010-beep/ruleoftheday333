package com.example.ruleoftheday333.fragments;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ruleoftheday333.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    EditText goalInput, habitInput, hoursInput;
    TextView saveButton, tvDifficultyLabel;
    SeekBar sliderDifficulty;
    RadioGroup radioAge;
    SwitchCompat switchKids, switchPets, switchCity, switchWFH;
    FirebaseAuth mAuth;

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

        goalInput        = view.findViewById(R.id.goalInput);
        habitInput       = view.findViewById(R.id.habitInput);
        hoursInput       = view.findViewById(R.id.hoursInput);
        saveButton       = view.findViewById(R.id.saveButton);
        tvDifficultyLabel = view.findViewById(R.id.tvDifficultyLabel);
        sliderDifficulty = view.findViewById(R.id.sliderDifficulty);
        radioAge         = view.findViewById(R.id.radioAge);
        switchKids       = view.findViewById(R.id.switchKids);
        switchPets       = view.findViewById(R.id.switchPets);
        switchCity       = view.findViewById(R.id.switchCity);
        switchWFH        = view.findViewById(R.id.switchWFH);

        mAuth = FirebaseAuth.getInstance();

        // Slider label update
        sliderDifficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean b) {
                tvDifficultyLabel.setText(difficultyLabels[p]);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        saveButton.setOnClickListener(v -> saveUserData());

        loadUserData();

        return view;
    }

    // ─── Load existing data ───────────────────────────────────────────────────

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

                        // Difficulty
                        Long diff = snap.child("difficulty").getValue(Long.class);
                        if (diff != null) {
                            sliderDifficulty.setProgress(diff.intValue());
                            tvDifficultyLabel.setText(difficultyLabels[diff.intValue()]);
                        }

                        // Age
                        String age = snap.child("ageRange").getValue(String.class);
                        if (age != null) {
                            switch (age) {
                                case "<18":  radioAge.check(R.id.ageUnder18); break;
                                case "18-25": radioAge.check(R.id.age18to25); break;
                                case "26-35": radioAge.check(R.id.age26to35); break;
                                case "36+":  radioAge.check(R.id.age36plus);  break;
                            }
                        }

                        // Toggles
                        Boolean kids = snap.child("hasKids").getValue(Boolean.class);
                        Boolean pets = snap.child("hasPets").getValue(Boolean.class);
                        Boolean city = snap.child("inCity").getValue(Boolean.class);
                        Boolean wfh  = snap.child("workFromHome").getValue(Boolean.class);
                        if (kids != null) switchKids.setChecked(kids);
                        if (pets != null) switchPets.setChecked(pets);
                        if (city != null) switchCity.setChecked(city);
                        if (wfh  != null) switchWFH.setChecked(wfh);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

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

        // Age range
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