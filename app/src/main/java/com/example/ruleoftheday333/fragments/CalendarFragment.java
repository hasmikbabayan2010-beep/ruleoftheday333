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
import com.example.ruleoftheday333.ui.login.ThemeManager;
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

import android.widget.LinearLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

    private final Map<String, String> allStatuses  = new HashMap<>();
    private final Map<String, Integer> allRatings   = new HashMap<>();
    private LinearLayout weeklyStatsContainer;
    private LinearLayout chartContainer;

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
        tvStreak             = view.findViewById(R.id.tvStreak);
        weeklyStatsContainer = view.findViewById(R.id.weeklyStatsContainer);
        chartContainer       = view.findViewById(R.id.chartContainer);

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

        ThemeManager.apply(requireContext(), view);
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

        // Also load ratings for weekly avg
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("ratings");
        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    Integer r = d.getValue(Integer.class);
                    if (d.getKey() != null && r != null) allRatings.put(d.getKey(), r);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

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
                buildWeeklyStats();
                buildProgressChart();
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

    // ─── Progress Chart (last 4 weeks) ───────────────────────────────────────

    private void buildProgressChart() {
        if (chartContainer == null || !isAdded()) return;
        chartContainer.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        java.util.Calendar cal = java.util.Calendar.getInstance();

        // Find the most recent Monday
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);

        // Collect data for last 4 weeks (newest = week 4, oldest = week 1)
        int[] greenCounts = new int[4];
        int[] redCounts   = new int[4];
        String[] weekLabels = new String[4];
        SimpleDateFormat monthDay = new SimpleDateFormat("MMM d", Locale.getDefault());

        for (int w = 3; w >= 0; w--) {
            java.util.Calendar weekStart = (java.util.Calendar) cal.clone();
            weekStart.add(java.util.Calendar.WEEK_OF_YEAR, w - 3);
            weekLabels[3 - w] = monthDay.format(weekStart.getTime());

            for (int d = 0; d < 7; d++) {
                String date = sdf.format(weekStart.getTime());
                String status = allStatuses.get(date);
                if ("green".equals(status)) greenCounts[3 - w]++;
                else if ("red".equals(status)) redCounts[3 - w]++;
                weekStart.add(java.util.Calendar.DAY_OF_MONTH, 1);
            }
        }

        // Find max for scaling
        int maxTotal = 7; // max possible per week

        // Chart title
        TextView title = new TextView(getContext());
        title.setText("📈 Last 4 Weeks");
        title.setTextSize(13);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#AD1457"));
        title.setPadding(0, 0, 0, 12);
        chartContainer.addView(title);

        // Chart area
        LinearLayout chartArea = new LinearLayout(getContext());
        chartArea.setOrientation(LinearLayout.HORIZONTAL);
        chartArea.setGravity(android.view.Gravity.BOTTOM);
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 300);
        chartArea.setLayoutParams(chartParams);

        for (int i = 0; i < 4; i++) {
            LinearLayout barGroup = new LinearLayout(getContext());
            barGroup.setOrientation(LinearLayout.VERTICAL);
            barGroup.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            groupParams.setMargins(8, 0, 8, 0);
            barGroup.setLayoutParams(groupParams);

            int green = greenCounts[i];
            int red   = redCounts[i];
            int empty = maxTotal - green - red;

            // Count label on top
            TextView countLabel = new TextView(getContext());
            countLabel.setText(green + "/" + (green + red));
            countLabel.setTextSize(10);
            countLabel.setTextColor(Color.parseColor("#888888"));
            countLabel.setGravity(android.view.Gravity.CENTER);
            barGroup.addView(countLabel);

            // Stacked bar column
            LinearLayout barCol = new LinearLayout(getContext());
            barCol.setOrientation(LinearLayout.VERTICAL);
            barCol.setGravity(android.view.Gravity.BOTTOM);
            LinearLayout.LayoutParams barColParams = new LinearLayout.LayoutParams(
                    40, LinearLayout.LayoutParams.MATCH_PARENT);
            barCol.setLayoutParams(barColParams);

            // Empty space on top (unlogged days)
            if (empty > 0) {
                View emptySegment = new View(getContext());
                LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, empty);
                emptySegment.setLayoutParams(ep);
                barCol.addView(emptySegment);
            }

            // Red segment
            if (red > 0) {
                View redSegment = new View(getContext());
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, red);
                GradientDrawable redBg = new GradientDrawable();
                redBg.setColor(Color.parseColor("#EF9A9A"));
                redSegment.setBackground(redBg);
                redSegment.setLayoutParams(rp);
                barCol.addView(redSegment);
            }

            // Green segment (bottom)
            if (green > 0) {
                View greenSegment = new View(getContext());
                LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, green);
                GradientDrawable greenBg = new GradientDrawable();
                greenBg.setCornerRadii(new float[]{6, 6, 6, 6, 0, 0, 0, 0}); // round top
                greenBg.setColor(Color.parseColor("#A5D6A7"));
                greenSegment.setBackground(greenBg);
                greenSegment.setLayoutParams(gp);
                barCol.addView(greenSegment);
            }

            barGroup.addView(barCol);
            chartArea.addView(barGroup);
        }

        chartContainer.addView(chartArea);

        // Week labels row
        LinearLayout labelsRow = new LinearLayout(getContext());
        labelsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        labelsParams.setMargins(0, 6, 0, 0);
        labelsRow.setLayoutParams(labelsParams);

        for (int i = 0; i < 4; i++) {
            TextView lbl = new TextView(getContext());
            lbl.setText(weekLabels[i]);
            lbl.setTextSize(10);
            lbl.setTextColor(Color.parseColor("#888888"));
            lbl.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lbl.setLayoutParams(lp);
            labelsRow.addView(lbl);
        }
        chartContainer.addView(labelsRow);

        // Legend
        LinearLayout legend = new LinearLayout(getContext());
        legend.setOrientation(LinearLayout.HORIZONTAL);
        legend.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams legendParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        legendParams.setMargins(0, 12, 0, 0);
        legend.setLayoutParams(legendParams);

        legend.addView(makeLegendDot("#A5D6A7"));
        legend.addView(makeLegendText(" Followed   "));
        legend.addView(makeLegendDot("#EF9A9A"));
        legend.addView(makeLegendText(" Missed"));
        chartContainer.addView(legend);
    }

    private View makeLegendDot(String color) {
        View dot = new View(getContext());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(14, 14);
        p.setMargins(0, 4, 0, 0);
        dot.setLayoutParams(p);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor(color));
        dot.setBackground(bg);
        return dot;
    }

    private TextView makeLegendText(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(11);
        tv.setTextColor(Color.parseColor("#888888"));
        return tv;
    }

    // ─── Weekly Stats ─────────────────────────────────────────────────────────

    private void buildWeeklyStats() {
        if (weeklyStatsContainer == null || !isAdded()) return;
        weeklyStatsContainer.removeAllViews();

        // Get Monday of current week
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        int weekGreen = 0, weekRed = 0, ratingSum = 0, ratingCount = 0;
        List<String> weekDates = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            weekDates.add(sdf.format(cal.getTime()));
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        // Summary row — day cells
        LinearLayout daysRow = new LinearLayout(getContext());
        daysRow.setOrientation(LinearLayout.HORIZONTAL);
        daysRow.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 12);
        daysRow.setLayoutParams(rowParams);

        java.util.Calendar today = java.util.Calendar.getInstance();
        String todayStr = sdf.format(today.getTime());

        for (int i = 0; i < 7; i++) {
            String date   = weekDates.get(i);
            String status = allStatuses.get(date);

            LinearLayout cell = new LinearLayout(getContext());
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            cell.setLayoutParams(cellParams);

            // Day name
            android.widget.TextView tvDay = new android.widget.TextView(getContext());
            tvDay.setText(dayNames[i]);
            tvDay.setTextSize(11);
            tvDay.setGravity(android.view.Gravity.CENTER);
            tvDay.setTextColor(date.equals(todayStr) ? Color.parseColor("#AD1457") : Color.parseColor("#888888"));
            if (date.equals(todayStr))
                tvDay.setTypeface(null, Typeface.BOLD);
            cell.addView(tvDay);

            // Status circle
            android.view.View circle = new android.view.View(getContext());
            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(36, 36);
            circleParams.setMargins(4, 6, 4, 4);
            circle.setLayoutParams(circleParams);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            if ("green".equals(status)) {
                bg.setColor(Color.parseColor("#A5D6A7"));
                weekGreen++;
            } else if ("red".equals(status)) {
                bg.setColor(Color.parseColor("#EF9A9A"));
                weekRed++;
            } else {
                bg.setColor(Color.parseColor("#E0E0E0")); // no data
            }
            circle.setBackground(bg);
            cell.addView(circle);

            // Status emoji under circle
            android.widget.TextView tvEmoji = new android.widget.TextView(getContext());
            tvEmoji.setText("green".equals(status) ? "✅" : "red".equals(status) ? "❌" : "");
            tvEmoji.setTextSize(10);
            tvEmoji.setGravity(android.view.Gravity.CENTER);
            cell.addView(tvEmoji);

            daysRow.addView(cell);

            // Weekly rating
            Integer r = allRatings.get(date);
            if (r != null) { ratingSum += r; ratingCount++; }
        }

        weeklyStatsContainer.addView(daysRow);

        // Summary text
        android.widget.TextView tvSummary = new android.widget.TextView(getContext());
        String avgRating = ratingCount > 0
                ? String.format(Locale.getDefault(), "  ·  ⭐ %.1f avg", (double) ratingSum / ratingCount)
                : "";
        tvSummary.setText("✅ " + weekGreen + "  ❌ " + weekRed + "  ⬜ " + (7 - weekGreen - weekRed) + avgRating);
        tvSummary.setTextSize(13);
        tvSummary.setTextColor(Color.parseColor("#AD1457"));
        tvSummary.setGravity(android.view.Gravity.CENTER);
        tvSummary.setTypeface(null, Typeface.BOLD);
        weeklyStatsContainer.addView(tvSummary);
    }

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