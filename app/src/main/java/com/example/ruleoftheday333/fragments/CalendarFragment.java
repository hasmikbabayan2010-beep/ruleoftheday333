package com.example.ruleoftheday333.fragments;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView tvGreenCount, tvRedCount, tvMoodLabel;
    private ProgressBar progressBar;
    private FrameLayout rootLayout;

    // Store all day statuses for bottom sheet + background logic
    private final Map<String, String> allStatuses = new HashMap<>();

    public CalendarFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView  = view.findViewById(R.id.calendarView);
        tvGreenCount  = view.findViewById(R.id.tvGreenCount);
        tvRedCount    = view.findViewById(R.id.tvRedCount);
        tvMoodLabel   = view.findViewById(R.id.tvMoodLabel);
        progressBar   = view.findViewById(R.id.progressBar);
        rootLayout    = view.findViewById(R.id.rootLayout);

        calendarView.setSelectionColor(Color.parseColor("#F48FB1")); // soft pink selection

        calendarView.addDecorator(new TodayDecorator());

        // Tap a day → bottom sheet
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (!selected) return;
            String key = date.getYear() + "-"
                    + String.format("%02d", date.getMonth() + 1) + "-"
                    + String.format("%02d", date.getDay());
            showDayBottomSheet(key, date);
        });

        loadCalendarData();
        return view;
    }

    // ─── Bottom Sheet ────────────────────────────────────────────────────────

    private void showDayBottomSheet(String dateKey, CalendarDay day) {
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

    // ─── Firebase Load ───────────────────────────────────────────────────────

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

                    allStatuses.put(date, status); // store for bottom sheet

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

                // Decorators with emoji spans
                calendarView.addDecorator(new EmojiDecorator(greenDays, "✅",
                        ContextCompat.getColor(requireContext(), R.color.green)));
                calendarView.addDecorator(new EmojiDecorator(redDays, "❌",
                        ContextCompat.getColor(requireContext(), R.color.red)));

                updateStatsCard(greenCount, redCount);
                updateMoodBackground(greenCount, redCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ─── Stats Card ──────────────────────────────────────────────────────────

    private void updateStatsCard(int green, int red) {
        tvGreenCount.setText("✅ " + green + " days");
        tvRedCount.setText("❌ " + red + " days");

        int total = green + red;
        progressBar.setMax(total > 0 ? total : 1);
        progressBar.setProgress(green);
    }

    // ─── Mood Background ─────────────────────────────────────────────────────

    private void updateMoodBackground(int green, int red) {
        int total = green + red;
        if (total == 0) return;

        float ratio = (float) green / total;

        int startColor, endColor;
        String moodText;

        if (ratio >= 0.8f) {
            // 🌸 Thriving — warm pink bloom
            startColor = Color.parseColor("#FCE4EC");
            endColor   = Color.parseColor("#F8BBD0");
            moodText   = "🌸 You're thriving!";
        } else if (ratio >= 0.5f) {
            // 🌷 Growing — soft lavender-pink
            startColor = Color.parseColor("#F3E5F5");
            endColor   = Color.parseColor("#EDE7F6");
            moodText   = "🌷 Keep going, you're growing!";
        } else if (ratio >= 0.3f) {
            // 🌙 Struggling — muted mauve
            startColor = Color.parseColor("#EDE0E8");
            endColor   = Color.parseColor("#D7CCD6");
            moodText   = "🌙 It's okay, be gentle with yourself.";
        } else {
            // 💜 Rough patch — deep dusty rose
            startColor = Color.parseColor("#D9C5CF");
            endColor   = Color.parseColor("#C9B8C4");
            moodText   = "💜 Every day is a new chance.";
        }

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{startColor, endColor}
        );
        rootLayout.setBackground(gradient);
        tvMoodLabel.setText(moodText);
    }

    // ─── Decorators ──────────────────────────────────────────────────────────

    public static class TodayDecorator implements DayViewDecorator {
        private final CalendarDay today = CalendarDay.today();

        @Override public boolean shouldDecorate(CalendarDay day) { return day.equals(today); }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD));
        }
    }

    public static class EmojiDecorator implements DayViewDecorator {
        private final Set<CalendarDay> dates;
        private final GradientDrawable drawable;
        private final String emoji;

        public EmojiDecorator(Set<CalendarDay> dates, String emoji, int color) {
            this.dates = dates;
            this.emoji = emoji;

            drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setAlpha(180); // slightly transparent — softer look
            drawable.setSize(60, 60);
        }

        @Override public boolean shouldDecorate(CalendarDay day) { return dates.contains(day); }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(drawable);
            // Emoji as text above the number
            view.addSpan(new android.text.style.RelativeSizeSpan(0.5f)); // smaller emoji
        }
    }
}