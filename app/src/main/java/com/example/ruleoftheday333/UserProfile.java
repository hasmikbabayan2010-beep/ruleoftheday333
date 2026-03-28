//package com.example.ruleoftheday333;
//
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//
//public class UserProfile {
//
//    public String goal;
//    public String habit;
//
//    public UserProfile() {
//        // Required for Firebase
//    }
//
//    public UserProfile(String goal, String habit) {
//        this.goal = goal;
//        this.habit = habit;
//    }
//}

package com.example.ruleoftheday333;

public class UserProfile {

    private String goal;
    private String habit;

    // No-argument constructor required for Firebase
    public UserProfile() {}

    // Constructor to create a new profile
    public UserProfile(String goal, String habit) {
        this.goal = goal;
        this.habit = habit;
    }

    // Getters
    public String getGoal() {
        return goal;
    }

    public String getHabit() {
        return habit;
    }

    // Setters (optional, useful if you want to update fields later)
    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setHabit(String habit) {
        this.habit = habit;
    }
}