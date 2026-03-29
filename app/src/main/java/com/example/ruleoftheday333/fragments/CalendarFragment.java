////package com.example.ruleoftheday333.fragments;
////
////import android.os.Bundle;
////
////import androidx.fragment.app.Fragment;
////
////import android.view.LayoutInflater;
////import android.view.View;
////import android.view.ViewGroup;
////
////import com.google.firebase.database.FirebaseDatabase;
////import com.google.firebase.database.DatabaseReference;
////import com.google.firebase.database.ValueEventListener;
////import com.google.firebase.database.DataSnapshot;
////import com.google.firebase.database.DatabaseError;
////
////import com.example.ruleoftheday333.R;
////
/////**
//// * A simple {@link Fragment} subclass.
//// * Use the {@link CalendarFragment#newInstance} factory method to
//// * create an instance of this fragment.
//// */
////public class CalendarFragment extends Fragment {
////
////    // TODO: Rename parameter arguments, choose names that match
////    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
////    private static final String ARG_PARAM1 = "param1";
////    private static final String ARG_PARAM2 = "param2";
////
////    // TODO: Rename and change types of parameters
////    private String mParam1;
////    private String mParam2;
////
////    public CalendarFragment() {
////        // Required empty public constructor
////    }
////
////    /**
////     * Use this factory method to create a new instance of
////     * this fragment using the provided parameters.
////     *
////     * @param param1 Parameter 1.
////     * @param param2 Parameter 2.
////     * @return A new instance of fragment CalendarFragment.
////     */
////    // TODO: Rename and change types and number of parameters
////    public static CalendarFragment newInstance(String param1, String param2) {
////        CalendarFragment fragment = new CalendarFragment();
////        Bundle args = new Bundle();
////        args.putString(ARG_PARAM1, param1);
////        args.putString(ARG_PARAM2, param2);
////        fragment.setArguments(args);
////        return fragment;
////    }
////
////    @Override
////    public void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        if (getArguments() != null) {
////            mParam1 = getArguments().getString(ARG_PARAM1);
////            mParam2 = getArguments().getString(ARG_PARAM2);
////        }
////    }
////
////    @Override
////    public View onCreateView(LayoutInflater inflater, ViewGroup container,
////                             Bundle savedInstanceState) {
////        // Inflate the layout for this fragment
////        return inflater.inflate(R.layout.fragment_calendar, container, false);
////    }
////}
//package com.example.ruleoftheday333.fragments;
//
//import android.graphics.drawable.ColorDrawable;
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.example.ruleoftheday333.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.*;
//
//import com.prolificinteractive.materialcalendarview.*;
//
//import android.graphics.Color;
//
//import java.util.HashSet;
//import java.util.Set;
//
//public class CalendarFragment extends Fragment {
//
//    MaterialCalendarView calendarView;
//
//    public CalendarFragment() {}
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
//
//        calendarView = view.findViewById(R.id.calendarView);
//
//        loadCalendarData();
//
//        return view;
//    }
//
//    private void loadCalendarData() {
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
//
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(userId)
//                .child("calendar");
//
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//
//                Set<CalendarDay> greenDays = new HashSet<>();
//                Set<CalendarDay> redDays = new HashSet<>();
//
//                for (DataSnapshot daySnap : snapshot.getChildren()) {
//
//                    String date = daySnap.getKey();
//                    String status = daySnap.getValue(String.class);
//
//                    if (date == null || status == null) continue;
//
//                    String[] parts = date.split("-");
//                    int year = Integer.parseInt(parts[0]);
//                    int month = Integer.parseInt(parts[1]);
//                    int day = Integer.parseInt(parts[2]);
//
//                    CalendarDay calendarDay = CalendarDay.from(year, month - 1, day);
//
//                    if (status.equals("green")) {
//                        greenDays.add(calendarDay);
//                    } else if (status.equals("red")) {
//                        redDays.add(calendarDay);
//                    }
//                }
//
//                calendarView.addDecorator(new ColorDecorator(greenDays, Color.GREEN));
//                calendarView.addDecorator(new ColorDecorator(redDays, Color.RED));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {}
//        });
//    }
//
//    public static class ColorDecorator implements DayViewDecorator {
//
//        private final Set<CalendarDay> dates;
//        private final int color;
//
//        public ColorDecorator(Set<CalendarDay> dates, int color) {
//            this.dates = dates;
//            this.color = color;
//        }
//
//        @Override
//        public boolean shouldDecorate(CalendarDay day) {
//            return dates.contains(day);
//        }
//
//        @Override
//        public void decorate(DayViewFacade view) {
//            view.setBackgroundDrawable(new ColorDrawable(color));
//        }
//    }
//}

//package com.example.ruleoftheday333.fragments;
//
//import android.content.Context;
//import android.graphics.drawable.GradientDrawable;
//import android.os.Bundle;
//
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.example.ruleoftheday333.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.prolificinteractive.materialcalendarview.CalendarDay;
//import com.prolificinteractive.materialcalendarview.DayViewDecorator;
//import com.prolificinteractive.materialcalendarview.DayViewFacade;
//import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
//
//import java.util.HashSet;
//import java.util.Set;
//
//public class CalendarFragment extends Fragment {
//
//    private MaterialCalendarView calendarView;
//
//    public CalendarFragment() {}
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
//
//        calendarView = view.findViewById(R.id.calendarView);
//
//        loadCalendarData();
//
//        return view;
//    }
//
//    private void loadCalendarData() {
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
//
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(userId)
//                .child("calendar");
//
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                Set<CalendarDay> greenDays = new HashSet<>();
//                Set<CalendarDay> redDays = new HashSet<>();
//
//                for (DataSnapshot daySnap : snapshot.getChildren()) {
//                    String date = daySnap.getKey();
//                    String status = daySnap.getValue(String.class);
//
//                    if (date == null || status == null) continue;
//
//                    String[] parts = date.split("-");
//                    if (parts.length != 3) continue;
//
//                    try {
//                        int year = Integer.parseInt(parts[0]);
//                        int month = Integer.parseInt(parts[1]);
//                        int day = Integer.parseInt(parts[2]);
//
//                        CalendarDay calendarDay = CalendarDay.from(year, month - 1, day);
//
//                        if (status.equals("green")) {
//                            greenDays.add(calendarDay);
//                        } else if (status.equals("red")) {
//                            redDays.add(calendarDay);
//                        }
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                int greenColor = ContextCompat.getColor(requireContext(), R.color.green);
//                int redColor = ContextCompat.getColor(requireContext(), R.color.red);
//
//                calendarView.addDecorator(new ColorDecorator(requireContext(), greenDays, greenColor));
//                calendarView.addDecorator(new ColorDecorator(requireContext(), redDays, redColor));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {}
//        });
//    }
//
//    public static class ColorDecorator implements DayViewDecorator {
//
//        private final Set<CalendarDay> dates;
//        private final GradientDrawable drawable;
//
//        public ColorDecorator(Context context, Set<CalendarDay> dates, int color) {
//            this.dates = dates;
//
//            drawable = new GradientDrawable();
//            drawable.setShape(GradientDrawable.OVAL);
//            drawable.setColor(color);
//            drawable.setSize(60, 60); // adjust circle size
//        }
//
//        @Override
//        public boolean shouldDecorate(CalendarDay day) {
//            return dates.contains(day);
//        }
//
//        @Override
//        public void decorate(DayViewFacade view) {
//            view.setBackgroundDrawable(drawable);
//        }
//    }
//}
package com.example.ruleoftheday333.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
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
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.HashSet;
import java.util.Set;

public class CalendarFragment extends Fragment {

    public static class TodayDecorator implements DayViewDecorator {

        private final CalendarDay today = CalendarDay.today();

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(today);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD));
        }
    }

    private MaterialCalendarView calendarView;

    public CalendarFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);

        // 🔥 UI IMPROVEMENTS (PASTE HERE)
        calendarView.setSelectionColor(Color.parseColor("#4CAF50")); // green selection
//        calendarView.setTodayTextColor(Color.BLACK); // today text color

        loadCalendarData();
        calendarView.addDecorator(new TodayDecorator());

        return view;
    }

    private void loadCalendarData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("calendar");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Set<CalendarDay> greenDays = new HashSet<>();
                Set<CalendarDay> redDays = new HashSet<>();

                for (DataSnapshot daySnap : snapshot.getChildren()) {
                    String date = daySnap.getKey();
                    String status = daySnap.getValue(String.class);

                    if (date == null || status == null) continue;

                    String[] parts = date.split("-");
                    if (parts.length != 3) continue;

                    try {
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int day = Integer.parseInt(parts[2]);

                        CalendarDay calendarDay = CalendarDay.from(year, month - 1, day);

                        if (status.equals("green")) {
                            greenDays.add(calendarDay);
                        } else if (status.equals("red")) {
                            redDays.add(calendarDay);
                        }

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                int greenColor = ContextCompat.getColor(requireContext(), R.color.green);
                int redColor = ContextCompat.getColor(requireContext(), R.color.red);

                calendarView.addDecorator(new ColorDecorator(greenDays, greenColor));
                calendarView.addDecorator(new ColorDecorator(redDays, redColor));
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    public static class ColorDecorator implements DayViewDecorator {

        private final Set<CalendarDay> dates;
        private final GradientDrawable drawable;

        public ColorDecorator(Set<CalendarDay> dates, int color) {
            this.dates = dates;

            drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setSize(60, 60);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(drawable);
        }
    }
}