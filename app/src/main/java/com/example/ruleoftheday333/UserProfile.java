package com.example.ruleoftheday333;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class UserProfile {

    public String goal;
    public String habit;

    public UserProfile() {
        // Required for Firebase
    }

    public UserProfile(String goal, String habit) {
        this.goal = goal;
        this.habit = habit;
    }
}