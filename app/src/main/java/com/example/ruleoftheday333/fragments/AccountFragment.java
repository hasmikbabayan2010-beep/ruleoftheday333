//////package com.example.ruleoftheday333.fragments;
//////
//////import android.os.Bundle;
//////
//////import androidx.fragment.app.Fragment;
//////
//////import com.google.firebase.auth.FirebaseAuth;
//////import com.google.firebase.database.FirebaseDatabase;
//////
//////import android.view.LayoutInflater;
//////import android.view.View;
//////import android.view.ViewGroup;
//////import android.widget.Button;
//////import android.widget.EditText;
//////
//////import com.example.ruleoftheday333.R;
//////
///////**
////// * A simple {@link Fragment} subclass.
////// * Use the {@link AccountFragment#newInstance} factory method to
////// * create an instance of this fragment.
////// */
//////public class AccountFragment extends Fragment {
//////
//////    EditText goalInput, habitInput;
//////    Button saveButton;
//////
//////    FirebaseAuth mAuth;
//////
//////    // TODO: Rename parameter arguments, choose names that match
//////    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//////    private static final String ARG_PARAM1 = "param1";
//////    private static final String ARG_PARAM2 = "param2";
//////
//////    // TODO: Rename and change types of parameters
//////    private String mParam1;
//////    private String mParam2;
//////
//////    public AccountFragment() {
//////        // Required empty public constructor
//////    }
//////
//////    /**
//////     * Use this factory method to create a new instance of
//////     * this fragment using the provided parameters.
//////     *
//////     * @param param1 Parameter 1.
//////     * @param param2 Parameter 2.
//////     * @return A new instance of fragment AccountFragment.
//////     */
//////    // TODO: Rename and change types and number of parameters
//////    public static AccountFragment newInstance(String param1, String param2) {
//////        AccountFragment fragment = new AccountFragment();
//////        Bundle args = new Bundle();
//////        args.putString(ARG_PARAM1, param1);
//////        args.putString(ARG_PARAM2, param2);
//////        fragment.setArguments(args);
//////        return fragment;
//////    }
//////
//////    @Override
//////    public void onCreate(Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        if (getArguments() != null) {
//////            mParam1 = getArguments().getString(ARG_PARAM1);
//////            mParam2 = getArguments().getString(ARG_PARAM2);
//////        }
//////    }
//////
//////    @Override
//////    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//////                             Bundle savedInstanceState) {
//////
//////        goalInput = view.findViewById(R.id.goalInput);
//////        habitInput = view.findViewById(R.id.habitInput);
//////        saveButton = view.findViewById(R.id.saveButton);
//////
//////        mAuth = FirebaseAuth.getInstance();
//////        // Inflate the layout for this fragment
//////        return inflater.inflate(R.layout.fragment_account, container, false);
//////    }
//////}
////
////package com.example.ruleoftheday333.fragments;
////
////import android.os.Bundle;
////
////import androidx.fragment.app.Fragment;
////
////import android.view.LayoutInflater;
////import android.view.View;
////import android.view.ViewGroup;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.Toast;
////
////import com.example.ruleoftheday333.R;
////import com.example.ruleoftheday333.UserProfile;
////import com.google.firebase.auth.FirebaseAuth;
////import com.google.firebase.database.FirebaseDatabase;
////import com.google.firebase.database.DatabaseReference;
////import com.google.firebase.database.ValueEventListener;
////import com.google.firebase.database.DataSnapshot;
////import com.google.firebase.database.DatabaseError;
////
////public class AccountFragment extends Fragment {
////
////    EditText goalInput, habitInput;
////    Button saveButton;
////
////    FirebaseAuth mAuth;
////
////    public AccountFragment() {}
////
////    @Override
////    public View onCreateView(LayoutInflater inflater, ViewGroup container,
////                             Bundle savedInstanceState) {
////
////        View view = inflater.inflate(R.layout.fragment_account, container, false);
////
////        goalInput = view.findViewById(R.id.goalInput);
////        habitInput = view.findViewById(R.id.habitInput);
////        saveButton = view.findViewById(R.id.saveButton);
////
////        mAuth = FirebaseAuth.getInstance();
////
////        saveButton.setOnClickListener(v -> saveUserData());
////
////        return view;
////    }
////
////    private void saveUserData() {
////
////        String goal = goalInput.getText().toString();
////        String habit = habitInput.getText().toString();
////
////        if(goal.isEmpty() || habit.isEmpty()){
////            Toast.makeText(getContext(),"Fill all fields",Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        if(mAuth.getCurrentUser() == null){
////            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
////            return;
////        }
////        String userId = mAuth.getCurrentUser().getUid();
////
////        UserProfile profile = new UserProfile(goal, habit);
////
////        FirebaseDatabase.getInstance()
////                .getReference("users")
////                .child(userId)
////                .child("profile")
////                .setValue(profile)
////                .addOnSuccessListener(unused ->
////                        Toast.makeText(getContext(),"Saved!",Toast.LENGTH_SHORT).show()
////                );
////    }
////}
//
//package com.example.ruleoftheday333.fragments;
//
//import android.os.Bundle;
//import androidx.fragment.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//import com.example.ruleoftheday333.R;
//import com.example.ruleoftheday333.UserProfile;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.FirebaseDatabase;
//
//public class AccountFragment extends Fragment {
//
//    EditText goalInput, habitInput;
//    Button saveButton;
//    FirebaseAuth mAuth;
//
//    public AccountFragment() {}
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_account, container, false);
//
//        goalInput = view.findViewById(R.id.goalInput);
//        habitInput = view.findViewById(R.id.habitInput);
//        saveButton = view.findViewById(R.id.saveButton);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        saveButton.setOnClickListener(v -> saveUserData());
//
//        return view;
//    }
//
//    private void saveUserData() {
//        String goal = goalInput.getText().toString().trim();
//        String habit = habitInput.getText().toString().trim();
//
//        if (goal.isEmpty() || habit.isEmpty()) {
//            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (mAuth.getCurrentUser() == null) {
//            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String userId = mAuth.getCurrentUser().getUid();
//        UserProfile profile = new UserProfile(goal, habit);
//
//        FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(userId)
//                .child("profile")
//                .setValue(profile)
//                .addOnSuccessListener(unused ->
//                        Toast.makeText(getContext(), "Profile saved!", Toast.LENGTH_SHORT).show()
//                )
//                .addOnFailureListener(e ->
//                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                );
//    }
//}

package com.example.ruleoftheday333.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.ruleoftheday333.R;
import com.example.ruleoftheday333.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AccountFragment extends Fragment {

    EditText goalInput, habitInput;
    Button saveButton;
    FirebaseAuth mAuth;

    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        goalInput = view.findViewById(R.id.goalInput);
        habitInput = view.findViewById(R.id.habitInput);
        saveButton = view.findViewById(R.id.saveButton);

        mAuth = FirebaseAuth.getInstance();

        saveButton.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void saveUserData() {
        String goal = goalInput.getText().toString().trim();
        String habit = habitInput.getText().toString().trim();

        if (goal.isEmpty() || habit.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), getString(R.string.toast_user_not_logged_in), Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        UserProfile profile = new UserProfile(goal, habit);

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("profile")
                .setValue(profile)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), getString(R.string.toast_error) + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}