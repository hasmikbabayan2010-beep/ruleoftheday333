//////package com.example.ruleoftheday333.fragments;
////////
////////import android.os.Bundle;
////////
////////import androidx.fragment.app.Fragment;
////////
////////import android.view.LayoutInflater;
////////import android.view.View;
////////import android.view.ViewGroup;
////////import android.widget.Button;
////////import android.widget.TextView;
////////
////////import com.example.ruleoftheday333.R;
////////
////////import java.util.Random;
////////
////////public class HomeFragment extends Fragment {
////////
////////    TextView ruleText;
////////    Button generateRule;
////////
////////    String[] rules = {
////////            "Drink 2 liters of water today",
////////            "Go for a 20 minute walk",
////////            "Read 10 pages of a book",
////////            "Do 20 pushups",
////////            "Compliment someone",
////////            "Clean your workspace",
////////            "Avoid sugar today",
////////            "Meditate for 5 minutes"
////////    };
////////
////////    public HomeFragment() {
////////        // Required empty constructor
////////    }
////////
////////    @Override
////////    public View onCreateView(LayoutInflater inflater, ViewGroup container,
////////                             Bundle savedInstanceState) {
////////
////////        View view = inflater.inflate(R.layout.fragment_home, container, false);
////////
////////        ruleText = view.findViewById(R.id.ruleText);
////////        generateRule = view.findViewById(R.id.generateRule);
////////
////////        generateRule.setOnClickListener(v -> {
////////
////////            Random random = new Random();
////////            int index = random.nextInt(rules.length);
////////
////////            ruleText.setText(rules[index]);
////////        });
////////
////////        return view;
////////    }
////////}
////////package com.example.ruleoftheday333.fragments;
////////
////////import android.os.Bundle;
////////
////////import androidx.fragment.app.Fragment;
////////
////////import android.view.LayoutInflater;
////////import android.view.View;
////////import android.view.ViewGroup;
////////
////////import com.example.ruleoftheday333.R;
////////
/////////**
//////// * A simple {@link Fragment} subclass.
//////// * Use the {@link HomeFragment#newInstance} factory method to
//////// * create an instance of this fragment.
//////// */
////////public class HomeFragment extends Fragment {
////////
////////    // TODO: Rename parameter arguments, choose names that match
////////    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
////////    private static final String ARG_PARAM1 = "param1";
////////    private static final String ARG_PARAM2 = "param2";
////////
////////    // TODO: Rename and change types of parameters
////////    private String mParam1;
////////    private String mParam2;
////////
////////    public HomeFragment() {
////////        // Required empty public constructor
////////    }
////////
////////    /**
////////     * Use this factory method to create a new instance of
////////     * this fragment using the provided parameters.
////////     *
////////     * @param param1 Parameter 1.
////////     * @param param2 Parameter 2.
////////     * @return A new instance of fragment HomeFragment.
////////     */
////////    // TODO: Rename and change types and number of parameters
////////    public static HomeFragment newInstance(String param1, String param2) {
////////        HomeFragment fragment = new HomeFragment();
////////        Bundle args = new Bundle();
////////        args.putString(ARG_PARAM1, param1);
////////        args.putString(ARG_PARAM2, param2);
////////        fragment.setArguments(args);
////////        return fragment;
////////    }
////////
////////    @Override
////////    public void onCreate(Bundle savedInstanceState) {
////////        super.onCreate(savedInstanceState);
////////        if (getArguments() != null) {
////////            mParam1 = getArguments().getString(ARG_PARAM1);
////////            mParam2 = getArguments().getString(ARG_PARAM2);
////////        }
////////    }
////////
////////    @Override
////////    public View onCreateView(LayoutInflater inflater, ViewGroup container,
////////                             Bundle savedInstanceState) {
////////        // Inflate the layout for this fragment
////////        return inflater.inflate(R.layout.fragment_home, container, false);
////////    }
////////}
//////
//////package com.example.ruleoftheday333.fragments;
//////
//////import android.app.DownloadManager;
//////import android.os.Bundle;
//////
//////import androidx.fragment.app.Fragment;
//////
//////import android.telecom.Call;
//////import android.view.LayoutInflater;
//////import android.view.PixelCopy;
//////import android.view.View;
//////import android.view.ViewGroup;
//////import android.widget.Button;
//////import android.widget.TextView;
//////
//////import com.example.ruleoftheday333.R;
//////import com.example.ruleoftheday333.UserProfile;
//////import com.google.android.gms.common.api.Response;
//////import com.google.common.net.MediaType;
//////import com.google.firebase.auth.FirebaseAuth;
//////import com.google.firebase.database.*;
//////import com.google.firebase.database.FirebaseDatabase;
//////import com.google.firebase.database.DatabaseReference;
//////import com.google.firebase.database.ValueEventListener;
//////import com.google.firebase.database.DataSnapshot;
//////import com.google.firebase.database.DatabaseError;
//////
//////import okhttp3.*;
//////import org.json.JSONObject;
//////
//////import java.io.IOException;
//////
//////import javax.security.auth.callback.Callback;
//////
//////public class HomeFragment extends Fragment {
//////
//////    TextView ruleText;
//////    Button generateRule;
//////
//////    public HomeFragment() {}
//////
//////    @Override
//////    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//////                             Bundle savedInstanceState) {
//////
//////        View view = inflater.inflate(R.layout.fragment_home, container, false);
//////
//////        ruleText = view.findViewById(R.id.ruleText);
//////        generateRule = view.findViewById(R.id.generateRule);
//////
//////        generateRule.setOnClickListener(v -> loadUserAndGenerateRule());
//////
//////        return view;
//////    }
//////
//////    private void loadUserAndGenerateRule(){
//////
//////        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//////
//////        DatabaseReference ref = FirebaseDatabase.getInstance()
//////                .getReference("users")
//////                .child(userId)
//////                .child("profile");
//////
//////        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//////            @Override
//////            public void onDataChange(DataSnapshot snapshot) {
//////
//////                UserProfile profile = snapshot.getValue(UserProfile.class);
//////
//////                if(profile != null){
//////
//////                    String prompt = "User goal: " + profile.goal +
//////                            ". Habits: " + profile.habit +
//////                            ". Give ONE short, aesthetic, self-improvement rule.";
//////
//////                    callGeminiAPI(prompt);
//////
//////                } else {
//////                    ruleText.setText("Please fill your profile first.");
//////                }
//////            }
//////
//////            @Override
//////            public void onCancelled(DatabaseError error) {}
//////        });
//////    }
//////
//////    private void callGeminiAPI(String prompt) {
//////
//////        OkHttpClient client = new OkHttpClient();
//////
//////        String apiKey = "YOUR_API_KEY_HERE";
//////
//////        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + apiKey;
//////
//////        try {
//////            JSONObject json = new JSONObject();
//////
//////            JSONObject part = new JSONObject();
//////            part.put("text", prompt);
//////
//////            JSONObject content = new JSONObject();
//////            content.put("parts", new org.json.JSONArray().put(part));
//////
//////            json.put("contents", new org.json.JSONArray().put(content));
//////
//////            RequestBody body = RequestBody.create(
//////                    json.toString(),
//////                    MediaType.parse("application/json")
//////            );
//////
//////            DownloadManager.Request request = new PixelCopy.Request.Builder()
//////                    .url(url)
//////                    .post(body)
//////                    .build();
//////
//////            client.newCall(request).enqueue(new Callback() {
//////
//////                @Override
//////                public void onFailure(Call call, IOException e) {
//////                    e.printStackTrace();
//////                }
//////
//////                @Override
//////                public void onResponse(Call call, Response response) throws IOException {
//////
//////                    if (response.isSuccessful()) {
//////
//////                        String res = response.body().string();
//////
//////                        try {
//////                            JSONObject obj = new JSONObject(res);
//////
//////                            String text = obj
//////                                    .getJSONArray("candidates")
//////                                    .getJSONObject(0)
//////                                    .getJSONObject("content")
//////                                    .getJSONArray("parts")
//////                                    .getJSONObject(0)
//////                                    .getString("text");
//////
//////                            requireActivity().runOnUiThread(() -> {
//////                                ruleText.setText(text);
//////                            });
//////
//////                        } catch (Exception e) {
//////                            e.printStackTrace();
//////                        }
//////                    }
//////                }
//////            });
//////
//////        } catch (Exception e) {
//////            e.printStackTrace();
//////        }
//////    }
//////}
//
//package com.example.ruleoftheday333.fragments;
//
//import android.os.Bundle;
//import androidx.fragment.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import com.example.ruleoftheday333.R;
//import com.example.ruleoftheday333.UserProfile;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.*;
//
//import okhttp3.*;
//import org.json.JSONObject;
//
//import java.io.IOException;
//
//public class HomeFragment extends Fragment {
//
//    TextView ruleText;
//    Button generateRule;
//
//    public HomeFragment() {}
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_home, container, false);
//
//        ruleText = view.findViewById(R.id.ruleText);
//        generateRule = view.findViewById(R.id.generateRule);
//
//        generateRule.setOnClickListener(v -> loadUserAndGenerateRule());
//
//        return view;
//    }
//
//    private void loadUserAndGenerateRule() {
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            ruleText.setText("Please log in first!");
//            return;
//        }
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(userId)
//                .child("profile");
//
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                UserProfile profile = snapshot.getValue(UserProfile.class);
//
//                if (profile != null) {
//                    String prompt = "User goal: " + profile.goal +
//                            ". Habits: " + profile.habit +
//                            ". Give ONE short, aesthetic, self-improvement rule.";
//                    callGeminiAPI(prompt);
//                } else {
//                    ruleText.setText("Please fill your profile first.");
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {}
//        });
//    }
//
//    private void callGeminiAPI(String prompt) {
//
//        OkHttpClient client = new OkHttpClient();
//        String apiKey = "YOUR_API_KEY_HERE";
//        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + apiKey;
//
//        try {
//            JSONObject json = new JSONObject();
//            JSONObject part = new JSONObject();
//            part.put("text", prompt);
//
//            JSONObject content = new JSONObject();
//            content.put("parts", new org.json.JSONArray().put(part));
//
//            json.put("contents", new org.json.JSONArray().put(content));
//
//            MediaType JSON = MediaType.get("application/json; charset=utf-8");
//            RequestBody body = RequestBody.create(json.toString(), JSON);
//
//            Request request = new Request.Builder()
//                    .url(url)
//                    .post(body)
//                    .build();
//
//            client.newCall(request).enqueue(new okhttp3.Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response.isSuccessful()) {
//                        String res = response.body().string();
//                        try {
//                            JSONObject obj = new JSONObject(res);
//                            String text = obj
//                                    .getJSONArray("candidates")
//                                    .getJSONObject(0)
//                                    .getJSONObject("content")
//                                    .getJSONArray("parts")
//                                    .getJSONObject(0)
//                                    .getString("text");
//
//                            requireActivity().runOnUiThread(() -> ruleText.setText(text));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
package com.example.ruleoftheday333.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.ruleoftheday333.R;
import com.example.ruleoftheday333.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class HomeFragment extends Fragment {

    TextView ruleText;
    Button generateRule;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ruleText = view.findViewById(R.id.ruleText);
        generateRule = view.findViewById(R.id.generateRule);

        generateRule.setOnClickListener(v -> loadUserAndGenerateRule());

        return view;
    }

    private void loadUserAndGenerateRule() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            ruleText.setText("Please log in first!");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("profile");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserProfile profile = snapshot.getValue(UserProfile.class);
                    if (profile != null) {
                        String prompt = "User goal: " + profile.getGoal() +
                                ". Habits: " + profile.getHabit() +
                                ". Give ONE short, aesthetic, self-improvement rule.";
                        callGeminiAPI(prompt);
                    } else {
                        ruleText.setText("Profile data missing. Please update your account.");
                    }
                } else {
                    ruleText.setText("Profile not found. Please fill your account info.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                ruleText.setText("Error loading profile: " + error.getMessage());
            }
        });
    }

    private void callGeminiAPI(String prompt) {
        OkHttpClient client = new OkHttpClient();
        String apiKey = "AIzaSyDZZJfYnshwt6ZmUkL-SHgluWbeuQj2v2Q";
        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        try {
            JSONObject json = new JSONObject();
            JSONObject part = new JSONObject();
            part.put("text", prompt);

            JSONObject content = new JSONObject();
            content.put("parts", new org.json.JSONArray().put(part));

            json.put("contents", new org.json.JSONArray().put(content));

            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            ruleText.setText("Failed to generate rule: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String res = response.body().string();
                        try {
                            JSONObject obj = new JSONObject(res);
                            String text = obj.getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            requireActivity().runOnUiThread(() -> ruleText.setText(text));
                        } catch (Exception e) {
                            e.printStackTrace();
                            requireActivity().runOnUiThread(() ->
                                    ruleText.setText("Failed to parse AI response."));
                        }
                    } else {
                        requireActivity().runOnUiThread(() ->
                                ruleText.setText("AI request failed: " + response.code()));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            ruleText.setText("Error creating AI request: " + e.getMessage());
        }
    }
}
//
//
//package com.example.ruleoftheday333.fragments;
//
//import android.os.Bundle;
//import androidx.fragment.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import com.example.ruleoftheday333.R;
//import com.example.ruleoftheday333.UserProfile;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.*;
//        import okhttp3.*;
//        import org.json.JSONObject;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//public class HomeFragment extends Fragment {
//
//    TextView ruleText;
//    Button generateRule;
//
//    public HomeFragment() {}
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_home, container, false);
//
//        ruleText = view.findViewById(R.id.ruleText);
//        generateRule = view.findViewById(R.id.generateRule);
//
//        generateRule.setOnClickListener(v -> loadRule());
//
//        return view;
//    }
//
//    private void loadRule() {
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            ruleText.setText("Please log in first!");
//            return;
//        }
//
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        String todayKey = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//
//        // Reference to today's rule
//        DatabaseReference ruleRef = FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(userId)
//                .child("todayRule")
//                .child(todayKey);
//
//        // Check if today's rule already exists
//        ruleRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    String savedRule = snapshot.getValue(String.class);
//                    ruleText.setText(savedRule);
//                } else {
//                    // Generate new rule from AI
//                    loadUserAndGenerateRule(userId, ruleRef);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                ruleText.setText("Error loading rule: " + error.getMessage());
//            }
//        });
//    }
//
//    private void loadUserAndGenerateRule(String userId, DatabaseReference ruleRef) {
//        DatabaseReference profileRef = FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(userId)
//                .child("profile");
//
//        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    UserProfile profile = snapshot.getValue(UserProfile.class);
//                    if (profile != null) {
//                        String prompt = "User goal: " + profile.getGoal() +
//                                ". Habits: " + profile.getHabit() +
//                                ". Give ONE short, aesthetic, self-improvement rule.";
//                        callGeminiAPI(prompt, ruleRef);
//                    } else {
//                        ruleText.setText("Profile data missing. Please update your account.");
//                    }
//                } else {
//                    ruleText.setText("Profile not found. Please fill your account info.");
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                ruleText.setText("Error loading profile: " + error.getMessage());
//            }
//        });
//    }
//
//    private void callGeminiAPI(String prompt, DatabaseReference ruleRef) {
//        OkHttpClient client = new OkHttpClient();
//        String apiKey = "YOUR_API_KEY_HERE"; // <-- replace with your Gemini key
//        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + apiKey;
//
//        try {
//            JSONObject json = new JSONObject();
//            JSONObject part = new JSONObject();
//            part.put("text", prompt);
//
//            JSONObject content = new JSONObject();
//            content.put("parts", new org.json.JSONArray().put(part));
//
//            json.put("contents", new org.json.JSONArray().put(content));
//
//            MediaType JSON = MediaType.get("application/json; charset=utf-8");
//            RequestBody body = RequestBody.create(json.toString(), JSON);
//
//            Request request = new Request.Builder()
//                    .url(url)
//                    .post(body)
//                    .build();
//
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                    requireActivity().runOnUiThread(() ->
//                            ruleText.setText("Failed to generate rule: " + e.getMessage()));
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response.isSuccessful()) {
//                        String res = response.body().string();
//                        try {
//                            JSONObject obj = new JSONObject(res);
//                            String text = obj.getJSONArray("candidates")
//                                    .getJSONObject(0)
//                                    .getJSONObject("content")
//                                    .getJSONArray("parts")
//                                    .getJSONObject(0)
//                                    .getString("text");
//
//                            // Save today's rule in Firebase
//                            ruleRef.setValue(text);
//
//                            // Update UI
//                            requireActivity().runOnUiThread(() -> ruleText.setText(text));
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            requireActivity().runOnUiThread(() ->
//                                    ruleText.setText("Failed to parse AI response."));
//                        }
//                    } else {
//                        requireActivity().runOnUiThread(() ->
//                                ruleText.setText("AI request failed: " + response.code()));
//                    }
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            requireActivity().runOnUiThread(() ->
//                    ruleText.setText("Error creating AI request: " + e.getMessage()));
//        }
//    }
//}