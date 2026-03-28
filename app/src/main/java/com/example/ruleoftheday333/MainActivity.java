////////package com.example.ruleoftheday333;
////////
////////import android.os.Bundle;
////////
////////import androidx.appcompat.app.AppCompatActivity;
////////import androidx.fragment.app.Fragment;
////////
////////import com.google.firebase.database.FirebaseDatabase;
////////import com.google.firebase.database.DatabaseReference;
////////import com.google.firebase.database.ValueEventListener;
////////import com.google.firebase.database.DataSnapshot;
////////import com.google.firebase.database.DatabaseError;
////////
////////import com.example.ruleoftheday333.fragments.AccountFragment;
////////import com.example.ruleoftheday333.fragments.CalendarFragment;
////////import com.example.ruleoftheday333.fragments.HomeFragment;
////////import com.example.ruleoftheday333.fragments.SettingsFragment;
////////import com.google.android.material.bottomnavigation.BottomNavigationView;
////////
////////public class MainActivity extends AppCompatActivity {
////////
////////    BottomNavigationView bottomNav;
////////
////////    @Override
////////    protected void onCreate(Bundle savedInstanceState) {
////////
////////        super.onCreate(savedInstanceState);
////////        setContentView(R.layout.activity_main);
////////
////////        bottomNav = findViewById(R.id.bottom_nav);
////////
////////        loadFragment(new HomeFragment());
////////
////////        bottomNav.setOnItemSelectedListener(item -> {
////////
////////            Fragment selectedFragment = null;
////////
////////            if(item.getItemId()==R.id.nav_home)
////////                selectedFragment = new HomeFragment();
////////
////////            else if(item.getItemId()==R.id.nav_calendar)
////////                selectedFragment = new CalendarFragment();
////////
////////            else if(item.getItemId()==R.id.nav_account)
////////                selectedFragment = new AccountFragment();
////////
////////            else if(item.getItemId()==R.id.nav_settings)
////////                selectedFragment = new SettingsFragment();
////////
////////            return loadFragment(selectedFragment);
////////        });
////////
////////    }
////////
////////    private boolean loadFragment(Fragment fragment){
////////
////////        if(fragment!=null){
////////
////////            getSupportFragmentManager()
////////                    .beginTransaction()
////////                    .replace(R.id.fragment_container,fragment)
////////                    .commit();
////////
////////            return true;
////////        }
////////
////////        return false;
////////    }
////////}
//////////    @Override
//////////    protected void onCreate(Bundle savedInstanceState) {
////////////        super.onCreate(savedInstanceState);
////////////        EdgeToEdge.enable(this);
////////////        setContentView(R.layout.activity_main);
////////////        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
////////////            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
////////////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
////////////            return insets;
////////////
//////////        super.onCreate(savedInstanceState);
//////////        EdgeToEdge.enable(this);
//////////        setContentView(R.layout.activity_main);
//////////
//////////        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//////////            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//////////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//////////            return insets;
//////////        });
////////
//////////        Button logoutButton = findViewById(R.id.logoutButton);
//////////        logoutButton.setOnClickListener(v -> {
//////////            FirebaseAuth.getInstance().signOut();
//////////            startActivity(new Intent(this, LoginActivity.class));
//////////            finish();
//////////        });
//////////    }
//////////    logoutButton.setOnClickListener(v -> {
//////////
//////////        FirebaseAuth.getInstance().signOut();
//////////
//////////        startActivity(new Intent(this, LoginActivity.class));
//////////        finish();
//////////
//////////    });
////////
////////
//////package com.example.ruleoftheday333;
//////
//////import android.content.Intent;
//////import android.os.Bundle;
//////
//////import androidx.appcompat.app.AppCompatActivity;
//////import androidx.fragment.app.Fragment;
//////
//////import com.example.ruleoftheday333.fragments.AccountFragment;
//////import com.example.ruleoftheday333.fragments.CalendarFragment;
//////import com.example.ruleoftheday333.fragments.HomeFragment;
//////import com.example.ruleoftheday333.fragments.SettingsFragment;
//////import com.example.ruleoftheday333.ui.login.LoginActivity;
//////import com.google.android.material.bottomnavigation.BottomNavigationView;
//////import com.google.firebase.auth.FirebaseAuth;
//////import android.os.Bundle;
//////
//////import androidx.appcompat.app.AppCompatActivity;
//////import androidx.fragment.app.Fragment;
//////
//////import com.google.firebase.database.FirebaseDatabase;
//////import com.google.firebase.database.DatabaseReference;
//////import com.google.firebase.database.ValueEventListener;
//////import com.google.firebase.database.DataSnapshot;
//////import com.google.firebase.database.DatabaseError;
//////
//////import com.example.ruleoftheday333.fragments.AccountFragment;
//////import com.example.ruleoftheday333.fragments.CalendarFragment;
//////import com.example.ruleoftheday333.fragments.HomeFragment;
//////import com.example.ruleoftheday333.fragments.SettingsFragment;
//////import com.google.android.material.bottomnavigation.BottomNavigationView;
//////
//////public class MainActivity extends AppCompatActivity {
//////
//////    private BottomNavigationView bottomNav;
//////    private FirebaseAuth mAuth;
//////
//////    @Override
//////    protected void onCreate(Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        setContentView(R.layout.activity_main);
//////
//////        // Initialize Firebase Auth
//////        mAuth = FirebaseAuth.getInstance();
//////
//////        // If no user logged in, redirect to LoginActivity
//////        if (mAuth.getCurrentUser() == null) {
//////            startActivity(new Intent(this, LoginActivity.class));
//////            finish();
//////            return;
//////        }
//////
//////        // Setup BottomNavigationView
//////        bottomNav = findViewById(R.id.bottom_nav);
//////
//////        // Load HomeFragment by default
//////        loadFragment(new HomeFragment());
//////
//////        // Handle navigation item selection
//////        bottomNav.setOnItemSelectedListener(item -> {
//////            Fragment selectedFragment = null;
//////
//////            switch (item.getItemId()) {
//////                case R.id.nav_home:
//////                    selectedFragment = new HomeFragment();
//////                    break;
//////                case R.id.nav_calendar:
//////                    selectedFragment = new CalendarFragment();
//////                    break;
//////                case R.id.nav_account:
//////                    selectedFragment = new AccountFragment();
//////                    break;
//////                case R.id.nav_settings:
//////                    selectedFragment = new SettingsFragment();
//////                    break;
//////            }
//////
//////            return loadFragment(selectedFragment);
//////        });
//////    }
//////
//////    /**
//////     * Replaces the fragment_container with the provided fragment.
//////     */
//////    private boolean loadFragment(Fragment fragment) {
//////        if (fragment != null) {
//////            getSupportFragmentManager()
//////                    .beginTransaction()
//////                    .replace(R.id.fragment_container, fragment)
//////                    .commit();
//////            return true;
//////        }
//////        return false;
//////    }
//////
//////    /**
//////     * Optional: Logout function if you add a logout button somewhere
//////     */
//////    private void logoutUser() {
//////        mAuth.signOut();
//////        startActivity(new Intent(this, LoginActivity.class));
//////        finish();
//////    }
//////}
////
////
////
////package com.example.ruleoftheday333;
////
////import android.content.Intent;
////import android.os.Bundle;
////
////import androidx.appcompat.app.AppCompatActivity;
////import androidx.fragment.app.Fragment;
////import com.example.ruleoftheday333.R;
////
////import com.example.ruleoftheday333.fragments.AccountFragment;
////import com.example.ruleoftheday333.fragments.CalendarFragment;
////import com.example.ruleoftheday333.fragments.HomeFragment;
////import com.example.ruleoftheday333.fragments.SettingsFragment;
////import com.example.ruleoftheday333.ui.login.LoginActivity;
////import com.google.android.material.bottomnavigation.BottomNavigationView;
////import com.google.firebase.auth.FirebaseAuth;
////
////
////public class MainActivity extends AppCompatActivity {
////
////    private BottomNavigationView bottomNav;
////    private FirebaseAuth mAuth;
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_main);
////
////        // Initialize Firebase Auth
////        mAuth = FirebaseAuth.getInstance();
////
////        // If no user logged in, redirect to LoginActivity
////        if (mAuth.getCurrentUser() == null) {
////            startActivity(new Intent(this, LoginActivity.class));
////            finish();
////            return;
////        }
////
////        // Setup BottomNavigationView
////        bottomNav = findViewById(R.id.bottom_nav);
////
////        // Load HomeFragment by default
////        loadFragment(new HomeFragment());
////
////        // Handle navigation item selection
////        bottomNav.setOnItemSelectedListener(item -> {
//////            Fragment selectedFragment = null;
//////
//////            switch (item.getItemId()) {
//////                case R.id.nav_home:
//////                    selectedFragment = new HomeFragment();
//////                    break;
//////                case R.id.nav_calendar:
//////                    selectedFragment = new CalendarFragment();
//////                    break;
//////                case R.id.nav_account:
//////                    selectedFragment = new AccountFragment();
//////                    break;
//////                case R.id.nav_settings:
//////                    selectedFragment = new SettingsFragment();
//////                    break;
//////            }
//////            if (selectedFragment != null) {
//////                getSupportFragmentManager()
//////                        .beginTransaction()
//////                        .replace(R.id.fragment_container, selectedFragment)
//////                        .commit();
//////            }
////            Fragment selectedFragment = null;
////
//////            switch (item.getItemId()) {
//////                case R.id.nav_home:
//////                    selectedFragment = new HomeFragment();
//////                    break;
//////                case R.id.nav_calendar:
//////                    selectedFragment = new CalendarFragment();
//////                    break;
//////                case R.id.nav_account:
//////                    selectedFragment = new AccountFragment();
//////                    break;
//////                case R.id.nav_settings:
//////                    selectedFragment = new SettingsFragment();
//////                    break;
//////            }
////
//////            if (selectedFragment != null) {
//////                getSupportFragmentManager()
//////                        .beginTransaction()
//////                        .replace(R.id.fragment_container, selectedFragment)
//////                        .commit();
//////            }
////            bottomNav.setOnItemSelectedListener(item -> {
////
////                Fragment selectedFragment = null;
////
////                if (item.getItemId() == R.id.nav_home) {
////                    selectedFragment = new HomeFragment();
////                }
////                else if (item.getItemId() == R.id.nav_calendar) {
////                    selectedFragment = new CalendarFragment();
////                }
////                else if (item.getItemId() == R.id.nav_account) {
////                    selectedFragment = new AccountFragment();
////                }
////                else if (item.getItemId() == R.id.nav_settings) {
////                    selectedFragment = new SettingsFragment();
////                }
////
////                return loadFragment(selectedFragment);
////            });
////            return loadFragment(selectedFragment);
////        });
////    }
////
////    /**
////     * Replaces the fragment_container with the provided fragment.
////     */
////    private boolean loadFragment(Fragment fragment) {
////        if (fragment != null) {
////            getSupportFragmentManager()
////                    .beginTransaction()
////                    .replace(R.id.fragment_container, fragment)
////                    .commit();
////            return true;
////        }
////        return false;
////    }
////
////    /**
////     * Optional: Logout function if you add a logout button somewhere
////     */
////    private void logoutUser() {
////        mAuth.signOut();
////        startActivity(new Intent(this, LoginActivity.class));
////        finish();
////    }
////}
//
//package com.example.ruleoftheday333;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//
//import com.example.ruleoftheday333.R;
//import com.example.ruleoftheday333.fragments.AccountFragment;
//import com.example.ruleoftheday333.fragments.CalendarFragment;
//import com.example.ruleoftheday333.fragments.HomeFragment;
//import com.example.ruleoftheday333.fragments.SettingsFragment;
//import com.example.ruleoftheday333.ui.login.LoginActivity;
//
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.firebase.auth.FirebaseAuth;
//
//public class MainActivity extends AppCompatActivity {
//
//    private BottomNavigationView bottomNav;
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Initialize Firebase Auth
//        mAuth = FirebaseAuth.getInstance();
//
//        // If no user logged in, redirect to LoginActivity
//        if (mAuth.getCurrentUser() == null) {
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//            return;
//        }
//
//        // Setup BottomNavigationView
//        bottomNav = findViewById(R.id.bottom_nav);
//
//        // Load HomeFragment by default
//        loadFragment(new HomeFragment());
//
//        // Handle navigation item selection (FIXED: no switch-case)
//        bottomNav.setOnItemSelectedListener(item -> {
//
//            Fragment selectedFragment = null;
//
//            if (item.getItemId() == R.id.nav_home) {
//                selectedFragment = new HomeFragment();
//            } else if (item.getItemId() == R.id.nav_calendar) {
//                selectedFragment = new CalendarFragment();
//            } else if (item.getItemId() == R.id.nav_account) {
//                selectedFragment = new AccountFragment();
//            } else if (item.getItemId() == R.id.nav_settings) {
//                selectedFragment = new SettingsFragment();
//            }
//
//            return loadFragment(selectedFragment);
//        });
//    }
//
//    /**
//     * Replaces the fragment_container with the provided fragment.
//     */
//    private boolean loadFragment(Fragment fragment) {
//        if (fragment != null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, fragment)
//                    .commit();
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Optional: Logout function if you add a logout button somewhere
//     */
//    private void logoutUser() {
//        mAuth.signOut();
//        startActivity(new Intent(this, LoginActivity.class));
//        finish();
//    }
//}
//
//



package com.example.ruleoftheday333;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.ruleoftheday333.R;
import com.example.ruleoftheday333.fragments.AccountFragment;
import com.example.ruleoftheday333.fragments.CalendarFragment;
import com.example.ruleoftheday333.fragments.HomeFragment;
import com.example.ruleoftheday333.fragments.SettingsFragment;
import com.example.ruleoftheday333.ui.login.LoginActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // --- 1️⃣ Apply saved theme BEFORE UI ---
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // If no user logged in, redirect to LoginActivity
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Setup BottomNavigationView
        bottomNav = findViewById(R.id.bottom_nav);

        // Load HomeFragment by default
        loadFragment(new HomeFragment());

        // Handle navigation item selection
        bottomNav.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (item.getItemId() == R.id.nav_account) {
                selectedFragment = new AccountFragment();
            } else if (item.getItemId() == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    /**
     * Replaces the fragment_container with the provided fragment.
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Optional: Logout function if you add a logout button somewhere
     */
    private void logoutUser() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}