package com.example.ruleoftheday333.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ruleoftheday333.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView tvGreenCount, tvRedCount, tvMoodLabel, tvStreak;
    private ProgressBar progressBar;
    private FrameLayout rootLayout;

    private final Map<String, String> allStatuses = new HashMap<>();

    public CalendarFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tvGreenCount = view.findViewById(R.id.tvGreenCount);
        tvRedCount   = view.findViewById(R.id.tvRedCount);
        tvMoodLabel  = view.findViewById(R.id.tvMoodLabel);
        progressBar  = view.findViewById(R.id.progressBar);
        rootLayout   = view.findViewById(R.id.rootLayout);
        tvStreak     = view.findViewById(R.id.tvStreak);

        calendarView.setSelectionColor(Color.parseColor("#F48FB1"));
        calendarView.addDecorator(new TodayDecorator());

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (!selected) return;
            String key = date.getYear() + "-"
                    + String.format("%02d", date.getMonth() + 1) + "-"
                    + String.format("%02d", date.getDay());
            showDayBottomSheet(key);
        });

        loadCalendarData();
        return view;
    }

    // ─── Bottom Sheet ─────────────────────────────────────────────────────────

    private void showDayBottomSheet(String dateKey) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.PinkBottomSheet);
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_day, null);

        TextView tvDate   = sheetView.findViewById(R.id.tvSheetDate);
        TextView tvStatus = sheetView.findViewById(R.id.tvSheetStatus);
        TextView tvNote   = sheetView.findViewById(R.id.tvSheetNote);

        tvDate.setText(dateKey);

        String status = allStatuses.get(dateKey);
        if (status == null) {
            tvStatus.setText("No data yet 🌙");
            tvNote.setText("Nothing recorded for this day.");
        } else if (status.equals("green")) {
            tvStatus.setText("✅ Rule followed!");
            tvNote.setText("Great job keeping your rule today 🌸");
        } else {
            tvStatus.setText("❌ Rule broken");
            tvNote.setText("That's okay — tomorrow is a fresh start 💪");
        }

        dialog.setContentView(sheetView);
        dialog.show();
    }

    // ─── Firebase Load ────────────────────────────────────────────────────────

    private void loadCalendarData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("calendar");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<CalendarDay> greenDays = new HashSet<>();
                Set<CalendarDay> redDays   = new HashSet<>();
                int greenCount = 0, redCount = 0;

                for (DataSnapshot daySnap : snapshot.getChildren()) {
                    String date   = daySnap.getKey();
                    String status = daySnap.getValue(String.class);
                    if (date == null || status == null) continue;

                    allStatuses.put(date, status);

                    String[] parts = date.split("-");
                    if (parts.length != 3) continue;

                    try {
                        int year  = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int day   = Integer.parseInt(parts[2]);
                        CalendarDay calDay = CalendarDay.from(year, month - 1, day);

                        if (status.equals("green")) { greenDays.add(calDay); greenCount++; }
                        else if (status.equals("red")) { redDays.add(calDay); redCount++; }

                    } catch (NumberFormatException e) { e.printStackTrace(); }
                }

                // Heatmap decorators — colored backgrounds + no emoji clutter
                calendarView.addDecorator(new HeatmapDecorator(greenDays,
                        Color.parseColor("#A5D6A7"),   // green fill
                        Color.parseColor("#1B5E20"))); // green text
                calendarView.addDecorator(new HeatmapDecorator(redDays,
                        Color.parseColor("#EF9A9A"),   // red fill
                        Color.parseColor("#B71C1C"))); // red text

                int streak = calculateStreak();
                updateStatsCard(greenCount, redCount, streak);
                updateMoodBackground(greenCount, redCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ─── Streak Calculation ───────────────────────────────────────────────────
    // Counts how many consecutive green days up to today

    private int calculateStreak() {
        List<String> sortedDates = new ArrayList<>(allStatuses.keySet());
        Collections.sort(sortedDates, Collections.reverseOrder()); // newest first

        int streak = 0;
        java.util.Calendar cal = java.util.Calendar.getInstance();

        for (String date : sortedDates) {
            String expected = String.format("%d-%02d-%02d",
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH) + 1,
                    cal.get(java.util.Calendar.DAY_OF_MONTH));

            if (!date.equals(expected)) break; // gap in days — streak ends

            String status = allStatuses.get(date);
            if (!"green".equals(status)) break; // red day — streak ends

            streak++;
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1); // go back one day
        }
        return streak;
    }

    // ─── Stats Card ───────────────────────────────────────────────────────────

    private void updateStatsCard(int green, int red, int streak) {
        tvGreenCount.setText("✅ " + green + " days");
        tvRedCount.setText("❌ " + red + " days");

        int total = green + red;
        progressBar.setMax(total > 0 ? total : 1);
        progressBar.setProgress(green);

        if (tvStreak != null) {
            tvStreak.setText(streak > 0
                    ? "🔥 " + streak + " day streak!"
                    : "Start your streak today!");
        }
    }

    // ─── Mood Background ──────────────────────────────────────────────────────

    private void updateMoodBackground(int green, int red) {
        int total = green + red;
        if (total == 0) return;

        float ratio = (float) green / total;

        int startColor, endColor;
        String moodText;

        if (ratio >= 0.8f) {
            startColor = Color.parseColor("#FCE4EC");
            endColor   = Color.parseColor("#F8BBD0");
            moodText   = "🌸 You're thriving!";
        } else if (ratio >= 0.5f) {
            startColor = Color.parseColor("#F3E5F5");
            endColor   = Color.parseColor("#EDE7F6");
            moodText   = "🌷 Keep going, you're growing!";
        } else if (ratio >= 0.3f) {
            startColor = Color.parseColor("#EDE0E8");
            endColor   = Color.parseColor("#D7CCD6");
            moodText   = "🌙 It's okay, be gentle with yourself.";
        } else {
            startColor = Color.parseColor("#D9C5CF");
            endColor   = Color.parseColor("#C9B8C4");
            moodText   = "💜 Every day is a new chance.";
        }

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{startColor, endColor});
        rootLayout.setBackground(gradient);
        tvMoodLabel.setText(moodText);
    }

    // ─── Decorators ───────────────────────────────────────────────────────────

    /** Heatmap-style decorator: colored circle background + colored date text */
    public static class HeatmapDecorator implements DayViewDecorator {
        private final Set<CalendarDay> dates;
        private final GradientDrawable background;
        private final int textColor;

        public HeatmapDecorator(Set<CalendarDay> dates, int bgColor, int textColor) {
            this.dates     = dates;
            this.textColor = textColor;

            background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(bgColor);
            background.setSize(80, 80);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) { return dates.contains(day); }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(background);
            view.addSpan(new ForegroundColorSpan(textColor));
            view.addSpan(new StyleSpan(Typeface.BOLD));
        }
    }

    /** Bold underline for today */
    public static class TodayDecorator implements DayViewDecorator {
        private final CalendarDay today = CalendarDay.today();

        @Override
        public boolean shouldDecorate(CalendarDay day) { return day.equals(today); }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new StyleSpan(Typeface.BOLD));
            view.addSpan(new android.text.style.UnderlineSpan());
        }
    }
}