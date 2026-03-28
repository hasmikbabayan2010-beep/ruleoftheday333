////////package com.example.ruleoftheday333.ui.login;
////////
////////import android.app.Activity;
////////
////////import androidx.lifecycle.Observer;
////////import androidx.lifecycle.ViewModelProvider;
////////
////////import android.os.Bundle;
////////
////////import androidx.annotation.Nullable;
////////import androidx.annotation.StringRes;
////////import androidx.appcompat.app.AppCompatActivity;
////////
////////import android.text.Editable;
////////import android.text.TextWatcher;
////////import android.view.KeyEvent;
////////import android.view.View;
////////import android.view.inputmethod.EditorInfo;
////////import android.widget.Button;
////////import android.widget.EditText;
////////import android.widget.ProgressBar;
////////import android.widget.TextView;
////////import android.widget.Toast;
////////
////////import com.example.ruleoftheday333.R;
////////import com.example.ruleoftheday333.ui.login.LoginViewModel;
////////import com.example.ruleoftheday333.ui.login.LoginViewModelFactory;
////////import com.example.ruleoftheday333.databinding.ActivityLoginBinding;
////////
////////public class LoginActivity extends AppCompatActivity {
////////
////////    private LoginViewModel loginViewModel;
////////    private ActivityLoginBinding binding;
////////
////////    @Override
////////    public void onCreate(Bundle savedInstanceState) {
////////        super.onCreate(savedInstanceState);
////////
////////        binding = ActivityLoginBinding.inflate(getLayoutInflater());
////////        setContentView(binding.getRoot());
////////
////////        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
////////                .get(LoginViewModel.class);
////////
////////        final EditText usernameEditText = binding.username;
////////        final EditText passwordEditText = binding.password;
////////        final Button loginButton = binding.login;
////////        final ProgressBar loadingProgressBar = binding.loading;
////////
////////        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
////////            @Override
////////            public void onChanged(@Nullable LoginFormState loginFormState) {
////////                if (loginFormState == null) {
////////                    return;
////////                }
////////                loginButton.setEnabled(loginFormState.isDataValid());
////////                if (loginFormState.getUsernameError() != null) {
////////                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
////////                }
////////                if (loginFormState.getPasswordError() != null) {
////////                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
////////                }
////////            }
////////        });
////////
////////        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
////////            @Override
////////            public void onChanged(@Nullable LoginResult loginResult) {
////////                if (loginResult == null) {
////////                    return;
////////                }
////////                loadingProgressBar.setVisibility(View.GONE);
////////                if (loginResult.getError() != null) {
////////                    showLoginFailed(loginResult.getError());
////////                }
////////                if (loginResult.getSuccess() != null) {
////////                    updateUiWithUser(loginResult.getSuccess());
////////                }
////////                setResult(Activity.RESULT_OK);
////////
////////                //Complete and destroy login activity once successful
////////                finish();
////////            }
////////        });
////////
////////        TextWatcher afterTextChangedListener = new TextWatcher() {
////////            @Override
////////            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
////////                // ignore
////////            }
////////
////////            @Override
////////            public void onTextChanged(CharSequence s, int start, int before, int count) {
////////                // ignore
////////            }
////////
////////            @Override
////////            public void afterTextChanged(Editable s) {
////////                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
////////                        passwordEditText.getText().toString());
////////            }
////////        };
////////        usernameEditText.addTextChangedListener(afterTextChangedListener);
////////        passwordEditText.addTextChangedListener(afterTextChangedListener);
////////        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
////////
////////            @Override
////////            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
////////                if (actionId == EditorInfo.IME_ACTION_DONE) {
////////                    loginViewModel.login(usernameEditText.getText().toString(),
////////                            passwordEditText.getText().toString());
////////                }
////////                return false;
////////            }
////////        });
////////
////////        loginButton.setOnClickListener(new View.OnClickListener() {
////////            @Override
////////            public void onClick(View v) {
////////                loadingProgressBar.setVisibility(View.VISIBLE);
////////                loginViewModel.login(usernameEditText.getText().toString(),
////////                        passwordEditText.getText().toString());
////////            }
////////        });
////////    }
////////
////////    private void updateUiWithUser(LoggedInUserView model) {
////////        String welcome = getString(R.string.welcome) + model.getDisplayName();
////////        // TODO : initiate successful logged in experience
////////        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
////////    }
////////
////////    private void showLoginFailed(@StringRes Integer errorString) {
////////        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
////////    }
////////}
//////package com.example.ruleoftheday333.ui.login;
//////
//////import android.content.Intent;
//////import android.os.Bundle;
//////import android.widget.Button;
//////import android.widget.EditText;
//////import android.widget.Toast;
//////import com.example.ruleoftheday333.R;
//////import androidx.activity.result.ActivityResultLauncher;
//////import androidx.activity.result.contract.ActivityResultContracts;
//////import androidx.appcompat.app.AppCompatActivity;
//////
//////import com.example.ruleoftheday333.MainActivity;
//////import com.example.ruleoftheday333.RegisterActivity;
//////import com.google.android.gms.auth.api.signin.*;
//////import com.google.android.gms.common.api.ApiException;
//////import com.google.firebase.auth.*;
//////import com.google.firebase.analytics.ktx.analytics
//////import com.google.firebase.ktx.Firebase
//////
//////public class LoginActivity extends AppCompatActivity {
//////
//////    private FirebaseAuth mAuth;
//////
//////    private EditText emailInput, passwordInput;
//////    private Button loginButton, registerButton, googleButton;
//////
//////    private GoogleSignInClient googleSignInClient;
//////
//////    @Override
//////    protected void onCreate(Bundle savedInstanceState) {
//////        emailInput = findViewById(R.id.username);
//////        passwordInput = findViewById(R.id.password);
//////        loginButton = findViewById(R.id.login);
//////        googleButton = findViewById(R.id.googleButton);
//////
//////        super.onCreate(savedInstanceState);
//////        setContentView(R.layout.activity_login);
//////
//////        mAuth = FirebaseAuth.getInstance();
//////
//////        emailInput = findViewById(R.id.username);
//////        passwordInput = findViewById(R.id.password);
//////        loginButton = findViewById(R.id.login);
//////        registerButton = findViewById(R.id.registerButton);
//////        googleButton = findViewById(R.id.googleButton);
//////
//////        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//////                .requestIdToken(getString(R.string.default_web_client_id))
//////                .requestEmail()
//////                .build();
//////
//////        googleSignInClient = GoogleSignIn.getClient(this, gso);
//////
//////        loginButton.setOnClickListener(v -> loginUser());
//////
//////        registerButton.setOnClickListener(v -> {
//////            startActivity(new Intent(this, RegisterActivity.class));
//////        });
//////
//////        googleButton.setOnClickListener(v -> signInWithGoogle());
//////    }
//////
//////    private void loginUser() {
//////
//////        String email = emailInput.getText().toString();
//////        String password = passwordInput.getText().toString();
//////
//////        mAuth.signInWithEmailAndPassword(email, password)
//////                .addOnCompleteListener(task -> {
//////
//////                    if (task.isSuccessful()) {
//////
//////                        startActivity(new Intent(this, MainActivity.class));
//////                        finish();
//////
//////                    } else {
//////
//////                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
//////
//////                    }
//////                });
//////    }
//////
//////    private void signInWithGoogle() {
//////
//////        Intent signInIntent = googleSignInClient.getSignInIntent();
//////        googleLauncher.launch(signInIntent);
//////
//////    }
//////
//////    private final ActivityResultLauncher<Intent> googleLauncher =
//////            registerForActivityResult(
//////                    new ActivityResultContracts.StartActivityForResult(),
//////                    result -> {
//////
//////                        if (result.getResultCode() == RESULT_OK) {
//////
//////                            try {
//////
//////                                GoogleSignInAccount account =
//////                                        GoogleSignIn.getSignedInAccountFromIntent(result.getData())
//////                                                .getResult(ApiException.class);
//////
//////                                firebaseAuthWithGoogle(account.getIdToken());
//////
//////                            } catch (ApiException e) {
//////
//////                                Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();
//////
//////                            }
//////
//////                        }
//////
//////                    });
//////
//////    private void firebaseAuthWithGoogle(String idToken) {
//////
//////        AuthCredential credential =
//////                GoogleAuthProvider.getCredential(idToken, null);
//////
//////        mAuth.signInWithCredential(credential)
//////                .addOnCompleteListener(this, task -> {
//////
//////                    if (task.isSuccessful()) {
//////
//////                        startActivity(new Intent(this, MainActivity.class));
//////                        finish();
//////
//////                    } else {
//////
//////                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
//////
//////                    }
//////
//////                });
//////    }
//////
//////    @Override
//////    protected void onStart() {
//////        super.onStart();
//////
//////        FirebaseUser user = mAuth.getCurrentUser();
//////
//////        if (user != null) {
//////
//////            startActivity(new Intent(this, MainActivity.class));
//////            finish();
//////
//////        }
//////    }
//////}
////package com.example.ruleoftheday333.ui.login;
////import static android.os.Build.VERSION_CODES.R;
////
////import com.google.android.gms.auth.api.signin.GoogleSignInClient;
////import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
////import android.content.Intent;
////import android.os.Bundle;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.Toast;
////
////import androidx.activity.result.ActivityResultLauncher;
////import androidx.activity.result.contract.ActivityResultContracts;
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.example.ruleoftheday333.MainActivity;
////import com.example.ruleoftheday333.R;
////import com.example.ruleoftheday333.RegisterActivity;
////
////import com.google.firebase.database.FirebaseDatabase;
////import com.google.firebase.database.DatabaseReference;
////import com.google.firebase.database.ValueEventListener;
////import com.google.firebase.database.DataSnapshot;
////import com.google.firebase.database.DatabaseError;
////
////import com.google.android.gms.auth.api.signin.*;
////import com.google.android.gms.common.api.ApiException;
////
////import com.google.firebase.auth.*;
////
////
////public class LoginActivity extends AppCompatActivity {
////
////    private FirebaseAuth mAuth;
////
////    private EditText emailInput, passwordInput;
////    private Button loginButton, registerButton, googleButton;
////
////    private GoogleSignInClient googleSignInClient;
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_login);
////
////        mAuth = FirebaseAuth.getInstance();
////
//////        EditText emailInput = findViewById(R.id.username);
//////        String email = emailInput.getText().toString();
//////
//////        EditText passwordInput = findViewById(R.id.password);
//////        String password = emailInput.getText().toString();
////////        emailInput = findViewById(R.id.username);
////////        passwordInput = findViewById(R.id.password);
//////        EditText loginButton = findViewById(R.id.buttonLogin);
//////        String login = emailInput.getText().toString();
////////        loginButton = findViewById(R.id.buttonLogin);
//////
//////        EditText registerButton = findViewById(R.id.buttonRegister);
//////        String register = emailInput.getText().toString();
////////        registerButton = findViewById(R.id.buttonRegister);
//////
//////        EditText passwordInput = findViewById(R.id.password);
//////        String password = emailInput.getText().toString();
//////        googleButton = findViewById(R.id.buttonGoogle);
////
////        // Declare your views at the top of the class
//////        private EditText emailInput, passwordInput;
//////        private Button loginButton, registerButton, googleButton;
////
////// Inside onCreate or your method:
////
////        emailInput = findViewById(R.id.username);
////        passwordInput = findViewById(R.id.password);
////
////        loginButton = findViewById(R.id.buttonLogin);
////        registerButton = findViewById(R.id.buttonRegister);
////        googleButton = findViewById(R.id.buttonGoogle);
////
////// To get the text values from EditTexts when needed:
////        String email = emailInput.getText().toString();
////        String password = passwordInput.getText().toString();
////        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
////                GoogleSignInOptions.DEFAULT_SIGN_IN)
////                .requestIdToken(getString(R.string.default_web_client_id))
////                .requestEmail()
////                .build();
////
////        googleSignInClient = GoogleSignIn.getClient(this, gso);
////
////        loginButton.setOnClickListener(v -> loginUser());
////
////        registerButton.setOnClickListener(v ->
////                startActivity(new Intent(this, RegisterActivity.class))
////        );
////
////        googleButton.setOnClickListener(v -> signInWithGoogle());
////    }
////
////    private void loginUser() {
////
////        String email = emailInput.getText().toString();
////        String password = passwordInput.getText().toString();
////
////        mAuth.signInWithEmailAndPassword(email, password)
////                .addOnCompleteListener(task -> {
////
////                    if (task.isSuccessful()) {
////
////                        startActivity(new Intent(this, MainActivity.class));
////                        finish();
////
////                    } else {
////
////                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
////
////                    }
////                });
////    }
////
////    private void signInWithGoogle() {
////
////        Intent signInIntent = googleSignInClient.getSignInIntent();
////        googleLauncher.launch(signInIntent);
////
////    }
////
////    private final ActivityResultLauncher<Intent> googleLauncher =
////            registerForActivityResult(
////                    new ActivityResultContracts.StartActivityForResult(),
////                    result -> {
////
////                        if (result.getResultCode() == RESULT_OK) {
////
////                            try {
////
////                                GoogleSignInAccount account =
////                                        GoogleSignIn.getSignedInAccountFromIntent(result.getData())
////                                                .getResult(ApiException.class);
////
////                                firebaseAuthWithGoogle(account.getIdToken());
////
////                            } catch (ApiException e) {
////
////                                Toast.makeText(this,
////                                        "Google Sign In Failed",
////                                        Toast.LENGTH_SHORT).show();
////
////                            }
////
////                        }
////
////                    });
////
////    private void firebaseAuthWithGoogle(String idToken) {
////
////        AuthCredential credential =
////                GoogleAuthProvider.getCredential(idToken, null);
////
////        mAuth.signInWithCredential(credential)
////                .addOnCompleteListener(this, task -> {
////
////                    if (task.isSuccessful()) {
////
////                        startActivity(new Intent(this, MainActivity.class));
////                        finish();
////
////                    } else {
////
////                        Toast.makeText(this,
////                                "Authentication Failed",
////                                Toast.LENGTH_SHORT).show();
////
////                    }
////
////                });
////    }
////
////    @Override
////    protected void onStart() {
////        super.onStart();
////
////        FirebaseUser user = mAuth.getCurrentUser();
////
////        if (user != null) {
////
////            startActivity(new Intent(this, MainActivity.class));
////            finish();
////
////        }
////    }
////}
//
//
//
//package com.example.ruleoftheday333.ui.login;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.ruleoftheday333.MainActivity;
//import com.example.ruleoftheday333.R;
//import com.example.ruleoftheday333.RegisterActivity;
//
//import com.google.android.gms.auth.api.signin.*;
//import com.google.android.gms.common.api.ApiException;
//
//import com.google.firebase.auth.*;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private FirebaseAuth mAuth;
//
//    private EditText emailInput, passwordInput;
//    private Button loginButton, registerButton, googleButton;
//    private ProgressBar loadingProgressBar;
//
//    private GoogleSignInClient googleSignInClient;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login); // MUST be first!
//
//        // Initialize Firebase Auth
//        mAuth = FirebaseAuth.getInstance();
//
//        // Initialize views
//        emailInput = findViewById(R.id.username);
//        passwordInput = findViewById(R.id.password);
//        loginButton = findViewById(R.id.buttonLogin);
//        registerButton = findViewById(R.id.buttonRegister);
//        googleButton = findViewById(R.id.buttonGoogle);
//        loadingProgressBar = findViewById(R.id.loading);
//
//        // Setup Google Sign-In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        googleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        // Click listeners
//        loginButton.setOnClickListener(v -> loginUser());
//        registerButton.setOnClickListener(v ->
//                startActivity(new Intent(this, RegisterActivity.class))
//        );
//        googleButton.setOnClickListener(v -> signInWithGoogle());
//    }
//
//    private void loginUser() {
//        String email = emailInput.getText().toString().trim();
//        String password = passwordInput.getText().toString().trim();
//
//        if (email.isEmpty() || password.isEmpty()) {
//            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        loadingProgressBar.setVisibility(ProgressBar.VISIBLE);
//
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    loadingProgressBar.setVisibility(ProgressBar.GONE);
//
//                    if (task.isSuccessful()) {
//                        startActivity(new Intent(this, MainActivity.class));
//                        finish();
//                    } else {
//                        Toast.makeText(this, "Login Failed: " + task.getException().getMessage(),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void signInWithGoogle() {
//        Intent signInIntent = googleSignInClient.getSignInIntent();
//        googleLauncher.launch(signInIntent);
//    }
//
//    private final ActivityResultLauncher<Intent> googleLauncher =
//            registerForActivityResult(
//                    new ActivityResultContracts.StartActivityForResult(),
//                    result -> {
//                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                            try {
//                                GoogleSignInAccount account = GoogleSignIn
//                                        .getSignedInAccountFromIntent(result.getData())
//                                        .getResult(ApiException.class);
//
//                                if (account != null) {
//                                    firebaseAuthWithGoogle(account.getIdToken());
//                                }
//
//                            } catch (ApiException e) {
//                                Toast.makeText(this,
//                                        "Google Sign In Failed: " + e.getMessage(),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//
//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        loadingProgressBar.setVisibility(ProgressBar.VISIBLE);
//
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, task -> {
//                    loadingProgressBar.setVisibility(ProgressBar.GONE);
//
//                    if (task.isSuccessful()) {
//                        startActivity(new Intent(this, MainActivity.class));
//                        finish();
//                    } else {
//                        Toast.makeText(this,
//                                "Authentication Failed: " + task.getException().getMessage(),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        FirebaseUser user = mAuth.getCurrentUser();
//
//        if (user != null) {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }
//    }
//}

package com.example.ruleoftheday333.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ruleoftheday333.MainActivity;
import com.example.ruleoftheday333.R;
import com.example.ruleoftheday333.RegisterActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText emailInput, passwordInput;
    private Button loginButton, registerButton, googleButton;
    private ProgressBar loadingProgressBar;

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);
        googleButton = findViewById(R.id.buttonGoogle);
        loadingProgressBar = findViewById(R.id.loading);

        // Setup Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Click listeners
        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
        googleButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.login_failed_generic);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            try {
                                GoogleSignInAccount account = GoogleSignIn
                                        .getSignedInAccountFromIntent(result.getData())
                                        .getResult(ApiException.class);

                                String idToken = account != null ? account.getIdToken() : null;
                                if (idToken != null) firebaseAuthWithGoogle(idToken);

                            } catch (ApiException e) {
                                Toast.makeText(this,
                                        "Google Sign In Failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        setLoading(true);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.login_failed_generic);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void setLoading(boolean isLoading) {
        loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        registerButton.setEnabled(!isLoading);
        googleButton.setEnabled(!isLoading);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            navigateToMain();
        }
    }
}