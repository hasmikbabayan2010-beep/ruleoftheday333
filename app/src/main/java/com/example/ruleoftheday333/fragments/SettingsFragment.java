////package com.example.ruleoftheday333.fragments;
////
////import android.content.res.Configuration;
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
////import java.util.Locale;
////
/////**
//// * A simple {@link Fragment} subclass.
//// * Use the {@link SettingsFragment#newInstance} factory method to
//// * create an instance of this fragment.
//// */
////public class SettingsFragment extends Fragment {
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
////    public SettingsFragment() {
////        // Required empty public constructor
////    }
////
////    /**
////     * Use this factory method to create a new instance of
////     * this fragment using the provided parameters.
////     *
////     * @param param1 Parameter 1.
////     * @param param2 Parameter 2.
////     * @return A new instance of fragment SettingsFragment.
////     */
////    // TODO: Rename and change types and number of parameters
////    public static SettingsFragment newInstance(String param1, String param2) {
////        SettingsFragment fragment = new SettingsFragment();
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
////        return inflater.inflate(R.layout.fragment_settings, container, false);
////    }
////
////    public void setLocale(String langCode) {
////        Locale locale = new Locale(langCode);
////        Locale.setDefault(locale);
////        Configuration config = getResources().getConfiguration();
////        config.setLocale(locale);
////        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
////
////        // Refresh the fragment/activity
////        getActivity().recreate();
////    }
////    setLocale("ru"); // switch to Russian
////    setLocale("zh"); // switch to Chinese
////    setLocale("en"); // switch to English
////}
//
//package com.example.ruleoftheday333.fragments;
//
//import android.content.res.Configuration;
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.example.ruleoftheday333.R;
//
//import java.util.Locale;
//
//public class SettingsFragment extends Fragment {
//
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private String mParam1;
//    private String mParam2;
//
//    public SettingsFragment() {
//        // Required empty public constructor
//    }
//
//    public static SettingsFragment newInstance(String param1, String param2) {
//        SettingsFragment fragment = new SettingsFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_settings, container, false);
//    }
//    // Example inside your fragment after the view is created
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // Example: switch to Russian
//        view.findViewById(R.id.btn_russian).setOnClickListener(v -> setLocale("ru"));
//
//        // Example: switch to Chinese
//        view.findViewById(R.id.btn_chinese).setOnClickListener(v -> setLocale("zh"));
//
//        // Example: switch to English
//        view.findViewById(R.id.btn_english).setOnClickListener(v -> setLocale("en"));
//
//        // Switch to Light Mode
//        view.findViewById(R.id.btn_light_mode).setOnClickListener(v -> setThemeMode(false));
//
//// Switch to Dark Mode
//        view.findViewById(R.id.btn_dark_mode).setOnClickListener(v -> setThemeMode(true));
//    }
//    /**
//     * Switches the app language dynamically.
//     * Call this method from a button click or other UI event, NOT in the class body.
//     *
//     * @param langCode "en", "ru", or "zh"
//     */
//    public void setLocale(String langCode) {
//        Locale locale = new Locale(langCode);
//        Locale.setDefault(locale);
//        Configuration config = getResources().getConfiguration();
//        config.setLocale(locale);
//        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
//
//        // Refresh the activity so new strings are loaded
//        if (getActivity() != null) {
//            getActivity().recreate();
//        }
//    }
//}
package com.example.ruleoftheday333.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ruleoftheday333.R;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- LANGUAGE SWITCHING ---
        view.findViewById(R.id.btn_russian).setOnClickListener(v -> setLocale("ru"));
        view.findViewById(R.id.btn_chinese).setOnClickListener(v -> setLocale("zh"));
        view.findViewById(R.id.btn_english).setOnClickListener(v -> setLocale("en"));

        // --- THEME SWITCHING ---
        view.findViewById(R.id.btn_light_mode).setOnClickListener(v -> setThemeMode(false));
        view.findViewById(R.id.btn_dark_mode).setOnClickListener(v -> setThemeMode(true));
    }

    /**
     * Switches the app language dynamically.
     * @param langCode "en", "ru", or "zh"
     */
    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Refresh the activity so new strings are loaded
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }

    /**
     * Switches the app theme dynamically (light/dark).
     * @param darkMode true for dark mode, false for light mode
     */
    private void setThemeMode(boolean darkMode) {
        if (getActivity() == null) return;

        // Save preference
        SharedPreferences prefs = getActivity().getSharedPreferences("app_settings", 0);
        prefs.edit().putBoolean("dark_mode", darkMode).apply();

        // Apply theme
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Recreate activity to apply changes immediately
        getActivity().recreate();
    }
}