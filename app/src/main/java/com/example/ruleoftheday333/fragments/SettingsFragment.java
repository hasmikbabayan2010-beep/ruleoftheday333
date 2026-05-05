package com.example.ruleoftheday333.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.ruleoftheday333.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import com.example.ruleoftheday333.ui.login.LoginActivity;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    // Track expanded state
    private boolean languageExpanded = false;
    private boolean themeExpanded = false;

    // Track current selections
    private String currentLang = "en";
    private boolean isDarkMode = false;

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
// ─── Sign Out + Rule + Reset ──────────────────────────────────────────────

    private void setupExtras(View view) {
        loadCurrentRule(view);
        setupEditRule(view);
        setupSignOut(view);
        setupResetCalendar(view);
    }

    private void loadCurrentRule(View view) {
        TextView tvRule = view.findViewById(R.id.tvCurrentRule);
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("profile").child("goal")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snap) {
                        String rule = snap.getValue(String.class);
                        tvRule.setText(rule != null ? rule : "No rule set yet 🌸");
                    }
                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError e) {}
                });
    }

    private void setupEditRule(View view) {
        view.findViewById(R.id.tvEditRule).setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            if (uid == null) return;

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
            builder.setTitle("Edit your rule 🎯");

            android.widget.EditText input = new android.widget.EditText(requireContext());
            input.setHint("e.g. drink 2L of water");
            TextView tvRule = view.findViewById(R.id.tvCurrentRule);
            input.setText(tvRule.getText().toString());
            input.setPadding(48, 32, 48, 32);
            builder.setView(input);

            builder.setPositiveButton("Save 🌸", (dialog, which) -> {
                String newRule = input.getText().toString().trim();
                if (newRule.isEmpty()) return;
                FirebaseDatabase.getInstance()
                        .getReference("users").child(uid).child("profile").child("goal")
                        .setValue(newRule)
                        .addOnSuccessListener(unused -> tvRule.setText(newRule));
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    private void setupSignOut(View view) {
        view.findViewById(R.id.rowSignOut).setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Sign out?")
                    .setMessage("You'll need to log back in to access your data.")
                    .setPositiveButton("Sign out", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        android.content.Intent intent = new android.content.Intent(
                                requireActivity(),
                                LoginActivity.class);
                        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
    private void setupResetCalendar(View view) {
        view.findViewById(R.id.rowResetCalendar).setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Reset calendar? 🗑️")
                    .setMessage("This will permanently delete all your calendar data. This can't be undone.")
                    .setPositiveButton("Yes, reset", (dialog, which) -> {
                        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                        if (uid == null) return;

                        FirebaseDatabase.getInstance()
                                .getReference("users").child(uid).child("calendar")
                                .removeValue()
                                .addOnSuccessListener(unused -> {
                                    android.widget.Toast.makeText(requireContext(),
                                            "Calendar reset 🌸", android.widget.Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // in onViewCreated, add this line:
        setupExtras(view);
        super.onViewCreated(view, savedInstanceState);

        // Load saved prefs
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_settings", 0);
        currentLang = prefs.getString("language", "en");
        isDarkMode  = prefs.getBoolean("dark_mode", false);

        setupProfileHeader(view);
        setupLanguageDropdown(view, prefs);
        setupThemeDropdown(view, prefs);
    }

    // ─── Profile Header ───────────────────────────────────────────────────────

    private void setupProfileHeader(View view) {
        TextView tvInitial = view.findViewById(R.id.tvInitial);
        TextView tvEmail   = view.findViewById(R.id.tvEmail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail() != null ? user.getEmail() : "user@email.com";
            tvEmail.setText(email);
            tvInitial.setText(String.valueOf(email.charAt(0)).toUpperCase());
        }
    }

    // ─── Language Dropdown ────────────────────────────────────────────────────

    private void setupLanguageDropdown(View view, SharedPreferences prefs) {
        LinearLayout headerLang  = view.findViewById(R.id.headerLanguage);
        LinearLayout contentLang = view.findViewById(R.id.contentLanguage);
        ImageView    arrowLang   = view.findViewById(R.id.arrowLanguage);
        TextView     tvLangValue = view.findViewById(R.id.tvLanguageValue);

        // Show current selection
        tvLangValue.setText(getLangLabel(currentLang));

        headerLang.setOnClickListener(v -> {
            languageExpanded = !languageExpanded;
            contentLang.setVisibility(languageExpanded ? View.VISIBLE : View.GONE);
            rotateArrow(arrowLang, languageExpanded);
        });

        // Language rows
        setupLangRow(view, R.id.rowEnglish,  "en",  tvLangValue, prefs);
        setupLangRow(view, R.id.rowRussian,  "ru",  tvLangValue, prefs);
        setupLangRow(view, R.id.rowChinese,  "zh",  tvLangValue, prefs);

        // Highlight current
        highlightLangRow(view, currentLang);
    }

    private void setupLangRow(View root, int rowId, String langCode,
                              TextView tvValue, SharedPreferences prefs) {
        root.findViewById(rowId).setOnClickListener(v -> {
            currentLang = langCode;
            tvValue.setText(getLangLabel(langCode));
            prefs.edit().putString("language", langCode).apply();
            highlightLangRow(root, langCode);

            // Collapse
            root.findViewById(R.id.contentLanguage).setVisibility(View.GONE);
            rotateArrow(root.findViewById(R.id.arrowLanguage), false);
            languageExpanded = false;

            setLocale(langCode);
        });
    }

    private void highlightLangRow(View root, String activeLang) {
        int[] rowIds   = {R.id.rowEnglish, R.id.rowRussian, R.id.rowChinese};
        String[] codes = {"en", "ru", "zh"};

        for (int i = 0; i < rowIds.length; i++) {
            View row       = root.findViewById(rowIds[i]);
            ImageView tick = row.findViewWithTag("tick_" + codes[i]);
            if (tick != null) tick.setVisibility(codes[i].equals(activeLang) ? View.VISIBLE : View.GONE);
            row.setBackgroundResource(codes[i].equals(activeLang)
                    ? R.drawable.row_selected_bg
                    : R.drawable.row_normal_bg);
        }
    }

    private String getLangLabel(String code) {
        switch (code) {
            case "ru": return "🇷🇺 Russian";
            case "zh": return "🇨🇳 Chinese";
            default:   return "🇬🇧 English";
        }
    }

    // ─── Theme Dropdown ───────────────────────────────────────────────────────

    private void setupThemeDropdown(View view, SharedPreferences prefs) {
        LinearLayout headerTheme  = view.findViewById(R.id.headerTheme);
        LinearLayout contentTheme = view.findViewById(R.id.contentTheme);
        ImageView    arrowTheme   = view.findViewById(R.id.arrowTheme);
        TextView     tvThemeValue = view.findViewById(R.id.tvThemeValue);

        tvThemeValue.setText(isDarkMode ? "🌙 Dark" : "☀️ Light");

        headerTheme.setOnClickListener(v -> {
            themeExpanded = !themeExpanded;
            contentTheme.setVisibility(themeExpanded ? View.VISIBLE : View.GONE);
            rotateArrow(arrowTheme, themeExpanded);
        });

        view.findViewById(R.id.rowLight).setOnClickListener(v -> {
            applyTheme(false, tvThemeValue, prefs);
            collapseTheme(view);
        });

        view.findViewById(R.id.rowDark).setOnClickListener(v -> {
            applyTheme(true, tvThemeValue, prefs);
            collapseTheme(view);
        });

        highlightThemeRow(view, isDarkMode);
    }

    private void applyTheme(boolean dark, TextView tvValue, SharedPreferences prefs) {
        isDarkMode = dark;
        tvValue.setText(dark ? "🌙 Dark" : "☀️ Light");
        prefs.edit().putBoolean("dark_mode", dark).apply();
        highlightThemeRow(requireView(), dark);
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        requireActivity().recreate();
    }

    private void highlightThemeRow(View root, boolean darkActive) {
        View rowLight = root.findViewById(R.id.rowLight);
        View rowDark  = root.findViewById(R.id.rowDark);

        rowLight.setBackgroundResource(!darkActive ? R.drawable.row_selected_bg : R.drawable.row_normal_bg);
        rowDark.setBackgroundResource(darkActive   ? R.drawable.row_selected_bg : R.drawable.row_normal_bg);

        ImageView tickLight = rowLight.findViewWithTag("tick_light");
        ImageView tickDark  = rowDark.findViewWithTag("tick_dark");
        if (tickLight != null) tickLight.setVisibility(!darkActive ? View.VISIBLE : View.GONE);
        if (tickDark  != null) tickDark.setVisibility(darkActive   ? View.VISIBLE : View.GONE);
    }

    private void collapseTheme(View view) {
        view.findViewById(R.id.contentTheme).setVisibility(View.GONE);
        rotateArrow(view.findViewById(R.id.arrowTheme), false);
        themeExpanded = false;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void rotateArrow(ImageView arrow, boolean expanded) {
        float from = expanded ? 0f : 180f;
        float to   = expanded ? 180f : 0f;
        RotateAnimation rotate = new RotateAnimation(from, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(250);
        rotate.setFillAfter(true);
        arrow.startAnimation(rotate);
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = requireContext().getResources().getConfiguration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config,
                requireContext().getResources().getDisplayMetrics());
        requireActivity().recreate();
    }
}