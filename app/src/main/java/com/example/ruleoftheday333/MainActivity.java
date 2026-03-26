package com.example.ruleoftheday333;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.example.ruleoftheday333.fragments.AccountFragment;
import com.example.ruleoftheday333.fragments.CalendarFragment;
import com.example.ruleoftheday333.fragments.HomeFragment;
import com.example.ruleoftheday333.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            if(item.getItemId()==R.id.nav_home)
                selectedFragment = new HomeFragment();

            else if(item.getItemId()==R.id.nav_calendar)
                selectedFragment = new CalendarFragment();

            else if(item.getItemId()==R.id.nav_account)
                selectedFragment = new AccountFragment();

            else if(item.getItemId()==R.id.nav_settings)
                selectedFragment = new SettingsFragment();

            return loadFragment(selectedFragment);
        });

    }

    private boolean loadFragment(Fragment fragment){

        if(fragment!=null){

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,fragment)
                    .commit();

            return true;
        }

        return false;
    }
}
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        EdgeToEdge.enable(this);
////        setContentView(R.layout.activity_main);
////        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
////            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
////            return insets;
////
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

//        Button logoutButton = findViewById(R.id.logoutButton);
//        logoutButton.setOnClickListener(v -> {
//            FirebaseAuth.getInstance().signOut();
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//        });
//    }
//    logoutButton.setOnClickListener(v -> {
//
//        FirebaseAuth.getInstance().signOut();
//
//        startActivity(new Intent(this, LoginActivity.class));
//        finish();
//
//    });


