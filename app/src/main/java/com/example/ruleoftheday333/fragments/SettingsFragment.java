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
import com.example.ruleoftheday333.ui.login.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.example.ruleoftheday333.ui.login.LoginActivity;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private boolean languageExpanded = false;
    private boolean themeExpanded    = false;

    private String  currentLang  = "en";
    private String  currentTheme = ThemeManager.PINK;

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setupExtras(view);
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_settings", 0);
        currentLang  = prefs.getString("language", "en");
        currentTheme = ThemeManager.getSavedTheme(requireContext());

        setupProfileHeader(view);
        setupLanguageDropdown(view, prefs);
        setupThemeDropdown(view, prefs);

        // Apply current theme to this screen
        ThemeManager.apply(requireContext(), view);
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
                                requireActivity(), LoginActivity.class);
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
                    .setMessage("This will permanently delete all your calendar data.")
                    .setPositiveButton("Yes, reset", (dialog, which) -> {
                        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                        if (uid == null) return;
                        FirebaseDatabase.getInstance()
                                .getReference("users").child(uid).child("calendar")
                                .removeValue()
                                .addOnSuccessListener(unused ->
                                        android.widget.Toast.makeText(requireContext(),
                                                "Calendar reset 🌸",
                                                android.widget.Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // ─── Profile Header ───────────────────────────────────────────────────────

    private void setupProfileHeader(View view) {
        TextView tvInitial = view.findViewById(R.id.tvInitial);
        TextView tvEmail   = view.findViewById(R.id.tvEmail);
        FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
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

        tvLangValue.setText(getLangLabel(currentLang));

        headerLang.setOnClickListener(v -> {
            languageExpanded = !languageExpanded;
            contentLang.setVisibility(languageExpanded ? View.VISIBLE : View.GONE);
            rotateArrow(arrowLang, languageExpanded);
        });

//        setupLangRow(view, R.id.rowEnglish, "en", tvLangValue, prefs);
//        setupLangRow(view, R.id.rowRussian, "ru", tvLangValue, prefs);
//        setupLangRow(view, R.id.rowChinese, "zh", tvLangValue, prefs);
//        highlightLangRow(view, currentLang);

        int[] rowIds   = {R.id.rowEnglish, R.id.rowRussian, R.id.rowChinese, R.id.rowArmenian};
        String[] codes = {"en", "ru", "zh", "hy"};
    }

    private void setupLangRow(View root, int rowId, String langCode,
                              TextView tvValue, SharedPreferences prefs) {
        root.findViewById(rowId).setOnClickListener(v -> {
            currentLang = langCode;
            tvValue.setText(getLangLabel(langCode));
            prefs.edit().putString("language", langCode).apply();
            highlightLangRow(root, langCode);
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
                    ? R.drawable.row_selected_bg : R.drawable.row_normal_bg);
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

        tvThemeValue.setText(ThemeManager.getLabel(currentTheme));

        headerTheme.setOnClickListener(v -> {
            themeExpanded = !themeExpanded;
            contentTheme.setVisibility(themeExpanded ? View.VISIBLE : View.GONE);
            rotateArrow(arrowTheme, themeExpanded);
        });

        // Build theme rows programmatically inside contentTheme
        for (String theme : ThemeManager.allThemes()) {
            LinearLayout row = buildThemeRow(theme, tvThemeValue, prefs, view);
            contentTheme.addView(row);
        }
    }

    private LinearLayout buildThemeRow(String theme, TextView tvThemeValue,
                                       SharedPreferences prefs, View rootView) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPaddingRelative(52, 0, 18, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 140);
        row.setLayoutParams(params);
        row.setBackgroundResource(theme.equals(currentTheme)
                ? R.drawable.row_selected_bg : R.drawable.row_normal_bg);

        // Color preview circle
        android.view.View circle = new android.view.View(requireContext());
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(28, 28);
        circleParams.setMarginEnd(16);
        circle.setLayoutParams(circleParams);
        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        bg.setColor(getThemePreviewColor(theme));
        bg.setStroke(2, android.graphics.Color.parseColor("#CCCCCC"));
        circle.setBackground(bg);

        // Label
        TextView label = new TextView(requireContext());
        label.setText(ThemeManager.getLabel(theme));
        label.setTextSize(14);
        label.setTextColor(android.graphics.Color.parseColor("#5D4037"));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);

        // Tick
        ImageView tick = new ImageView(requireContext());
        tick.setImageResource(R.drawable.ic_check_pink);
        LinearLayout.LayoutParams tickParams =
                new LinearLayout.LayoutParams(18, 18);
        tick.setLayoutParams(tickParams);
        tick.setVisibility(theme.equals(currentTheme) ? View.VISIBLE : View.GONE);

        row.addView(circle);
        row.addView(label);
        row.addView(tick);

        row.setOnClickListener(v -> {
            currentTheme = theme;
            ThemeManager.saveTheme(requireContext(), theme);
            tvThemeValue.setText(ThemeManager.getLabel(theme));

            // Update row highlights
            LinearLayout contentTheme = rootView.findViewById(R.id.contentTheme);
            for (int i = 0; i < contentTheme.getChildCount(); i++) {
                View child = contentTheme.getChildAt(i);
                boolean isThis = (child == row);
                child.setBackgroundResource(isThis
                        ? R.drawable.row_selected_bg : R.drawable.row_normal_bg);
                // Toggle tick visibility
                if (child instanceof LinearLayout) {
                    for (int j = 0; j < ((LinearLayout) child).getChildCount(); j++) {
                        View sub = ((LinearLayout) child).getChildAt(j);
                        if (sub instanceof ImageView) {
                            sub.setVisibility(isThis ? View.VISIBLE : View.GONE);
                        }
                    }
                }
            }

            // Apply immediately
            ThemeManager.applyTheme(theme, requireView());

            // Collapse
            rootView.findViewById(R.id.contentTheme).setVisibility(View.GONE);
            rotateArrow(rootView.findViewById(R.id.arrowTheme), false);
            themeExpanded = false;
        });

        return row;
    }

    private int getThemePreviewColor(String theme) {
        switch (theme) {
            case ThemeManager.PURPLE:       return android.graphics.Color.parseColor("#CE93D8");
            case ThemeManager.BLUE:         return android.graphics.Color.parseColor("#90CAF9");
            case ThemeManager.GREEN:        return android.graphics.Color.parseColor("#A5D6A7");
            case ThemeManager.ORANGE:       return android.graphics.Color.parseColor("#FFCC80");
            case ThemeManager.RED:          return android.graphics.Color.parseColor("#EF9A9A");
            case ThemeManager.PASTEL_PEACH: return android.graphics.Color.parseColor("#FFCCBC");
            case ThemeManager.PASTEL_MINT:  return android.graphics.Color.parseColor("#80DEEA");
            case ThemeManager.GRADIENT:     return android.graphics.Color.parseColor("#FFB3C6");
            case ThemeManager.VIVID:        return android.graphics.Color.parseColor("#FF6B6B");
            case ThemeManager.YELLOW:       return android.graphics.Color.parseColor("#FFF176");
            default:                        return android.graphics.Color.parseColor("#F48FB1");
        }
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