//package com.example.ruleoftheday333;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//
//public class RegisterActivity extends AppCompatActivity {
//
//    private FirebaseAuth mAuth;
//    private static final String TAG = "RegisterActivity";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
//
//        // Initialize Firebase
//        mAuth = FirebaseAuth.getInstance();
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
//    }
//
//    private void firebaseAuthWithGoogle(String idToken) {
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, task -> {
//
//                    if (task.isSuccessful()) {
//
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        updateUI(user);
//
//                    } else {
//
//                        Log.w(TAG, "signInWithCredential:failure", task.getException());
//                        updateUI(null);
//
//                    }
//
//                });
//    }
//
//    private void updateUI(FirebaseUser user) {
//
//        if (user != null) {
//
//            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//
//        }
//
//    }
//}
package com.example.ruleoftheday333;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText emailInput, passwordInput;
    private Button registerButton, backLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailRegister);
        passwordInput = findViewById(R.id.passwordRegister);
        registerButton = findViewById(R.id.buttonCreateAccount);
        backLoginButton = findViewById(R.id.buttonBackLogin);

        registerButton.setOnClickListener(v -> registerUser());

        backLoginButton.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.ruleoftheday333.ui.login.LoginActivity.class))
        );
    }

    private void registerUser() {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if(email.isEmpty() || password.isEmpty()){

            Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show();
            return;

        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()) {

                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this,
                                com.example.ruleoftheday333.ui.login.LoginActivity.class));

                        finish();

//                    }else{
//
//                        Toast.makeText(this,
//                                "Registration Failed",
//                                Toast.LENGTH_SHORT).show();
//
//                    }
                    } else {
                            Exception e = task.getException();
                            Toast.makeText(this,
                                    "Registration Failed: " + (e != null ? e.getMessage() : "Unknown error"),
                                    Toast.LENGTH_LONG).show();
                        }

                });

    }
}