package com.example.ruleoftheday333.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.ruleoftheday333.R;
import com.example.ruleoftheday333.ui.login.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import androidx.core.app.ActivityCompat;
import android.widget.Button;
import android.widget.ProgressBar;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountFragment extends Fragment {

    EditText goalInput, habitInput, hoursInput;
    TextView saveButton, tvDifficultyLabel;
    SeekBar sliderDifficulty;
    RadioGroup radioAge;
    SwitchCompat switchKids, switchPets, switchCity, switchWFH;
    FirebaseAuth mAuth;

    // Location
    private double userLat = 0, userLng = 0;
    private static final int LOCATION_PERMISSION_REQUEST = 101;

    // AI Advice UI
    private EditText advicePreferenceInput;
    private Button   btnGetAdvice;
    private TextView tvAdviceResult;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL   = "llama-3.1-8b-instant";

    // History UI
    private LinearLayout historyContainer;
    private TextView tvHistoryToggle;
    private boolean historyExpanded = false;
    private boolean historyLoaded   = false;

    private final String[] difficultyLabels = {
            "🌱 Beginner — give me easy rules",
            "🚶 Getting started — keep it simple",
            "🏃 Intermediate — I can handle more",
            "💪 Advanced — push me harder",
            "🏆 Pro — give me professional rules"
    };

    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        goalInput         = view.findViewById(R.id.goalInput);
        habitInput        = view.findViewById(R.id.habitInput);
        hoursInput        = view.findViewById(R.id.hoursInput);
        saveButton        = view.findViewById(R.id.saveButton);
        tvDifficultyLabel = view.findViewById(R.id.tvDifficultyLabel);
        sliderDifficulty  = view.findViewById(R.id.sliderDifficulty);
        radioAge          = view.findViewById(R.id.radioAge);
        switchKids        = view.findViewById(R.id.switchKids);
        switchPets        = view.findViewById(R.id.switchPets);
        switchCity        = view.findViewById(R.id.switchCity);
        switchWFH         = view.findViewById(R.id.switchWFH);

        historyContainer      = view.findViewById(R.id.historyContainer);
        tvHistoryToggle       = view.findViewById(R.id.tvHistoryToggle);
        advicePreferenceInput = view.findViewById(R.id.advicePreferenceInput);
        btnGetAdvice          = view.findViewById(R.id.btnGetAdvice);
        tvAdviceResult        = view.findViewById(R.id.tvAdviceResult);

        btnGetAdvice.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
            } else {
                fetchLocationThenAdvice();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        sliderDifficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean b) {
                tvDifficultyLabel.setText(difficultyLabels[p]);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        saveButton.setOnClickListener(v -> saveUserData());

        // History dropdown toggle
        tvHistoryToggle.setOnClickListener(v -> {
            if (historyExpanded) {
                historyContainer.setVisibility(View.GONE);
                tvHistoryToggle.setText("📖 Rule History  ▼");
                historyExpanded = false;
            } else {
                historyContainer.setVisibility(View.VISIBLE);
                tvHistoryToggle.setText("📖 Rule History  ▲");
                historyExpanded = true;
                if (!historyLoaded) {
                    loadHistory();
                    historyLoaded = true;
                }
            }
        });

        loadUserData();




        ThemeManager.apply(requireContext(), view);
        return view;
    }

    // ─── Location & Nearby Places ────────────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationThenAdvice();
        } else {
            // No location — proceed without nearby places
            fetchAndGenerateAdvice("");
        }
    }

    private void fetchLocationThenAdvice() {
        try {
            LocationManager lm = (LocationManager)
                    requireContext().getSystemService(Context.LOCATION_SERVICE);
            Location loc = null;
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (loc != null) {
                userLat = loc.getLatitude();
                userLng = loc.getLongitude();
            }
        } catch (Exception e) {
            // proceed without location
        }

        if (userLat != 0 && userLng != 0) {
            // Fetch nearby places on background thread
            String goal = goalInput.getText().toString().trim().toLowerCase();
            String overpassCategory = goalToOverpassCategory(goal);
            tvAdviceResult.setText("📍 Finding nearby places...");
            tvAdviceResult.setVisibility(View.VISIBLE);
            btnGetAdvice.setEnabled(false);
            executor.execute(() -> {
                String nearby = fetchNearbyPlaces(userLat, userLng, overpassCategory);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> fetchAndGenerateAdvice(nearby));
            });
        } else {
            fetchAndGenerateAdvice("");
        }
    }

    private String goalToOverpassCategory(String goal) {
        if (goal.contains("gym") || goal.contains("workout") || goal.contains("fit")
                || goal.contains("sport") || goal.contains("run") || goal.contains("exercise"))
            return "leisure=fitness_centre";
        if (goal.contains("read") || goal.contains("book"))
            return "shop=books";
        if (goal.contains("yoga") || goal.contains("meditat"))
            return "leisure=yoga";
        if (goal.contains("swim") || goal.contains("pool"))
            return "leisure=swimming_pool";
        if (goal.contains("park") || goal.contains("walk") || goal.contains("nature"))
            return "leisure=park";
        if (goal.contains("cook") || goal.contains("food") || goal.contains("eat"))
            return "shop=supermarket";
        // Default — return empty so AI skips nearby section
        return "";
    }

    private String fetchNearbyPlaces(double lat, double lng, String category) {
        if (category.isEmpty()) return "";
        try {
            String query = "[out:json];node[\"" + category + "\"](around:2000," +
                    lat + "," + lng + ");out 5;";
            String encodedQuery = java.net.URLEncoder.encode(
                    "[out:json];node[\"" + category + "\"](around:2000," + lat + "," + lng + ");out 5;",
                    "UTF-8");
            URL url = new URL("https://overpass-api.de/api/interpreter?data=" + encodedQuery);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            org.json.JSONObject root = new org.json.JSONObject(sb.toString());
            org.json.JSONArray elements = root.getJSONArray("elements");

            if (elements.length() == 0) return "";

            StringBuilder places = new StringBuilder();
            for (int i = 0; i < Math.min(3, elements.length()); i++) {
                org.json.JSONObject el   = elements.getJSONObject(i);
                org.json.JSONObject tags = el.optJSONObject("tags");
                if (tags == null) continue;
                String name = tags.optString("name", "");
                String addr = tags.optString("addr:street", "");
                if (!name.isEmpty()) {
                    places.append("• ").append(name);
                    if (!addr.isEmpty()) places.append(" (").append(addr).append(")");
                    places.append("\n");
                }
            }
            return places.toString().trim();

        } catch (Exception e) {
            return "";
        }
    }

    // ─── Load History ─────────────────────────────────────────────────────────

    private void loadHistory() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference base = FirebaseDatabase.getInstance(
                        "https://ruleoftheday333-default-rtdb.firebaseio.com")
                .getReference("users").child(uid);

        // We need rules, ratings, moods, songs — fetch all four in parallel
        // and merge by date key
        Map<String, String>              rulesMap   = new HashMap<>();
        Map<String, Integer>             ratingsMap = new HashMap<>();
        Map<String, String>              moodsMap   = new HashMap<>();
        Map<String, List<String>>        songsMap   = new HashMap<>();
        int[] pending = {4}; // countdown — render when all 4 loaded

        Runnable tryRender = () -> {
            pending[0]--;
            if (pending[0] > 0) return;

            // Collect all dates, sort newest first
            List<String> dates = new ArrayList<>(rulesMap.keySet());
            Collections.sort(dates, Collections.reverseOrder());

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                historyContainer.removeAllViews();

                if (dates.isEmpty()) {
                    TextView empty = new TextView(getContext());
                    empty.setText("No rule history yet — start generating! 🌸");
                    empty.setTextColor(0xFFAD1457);
                    empty.setPadding(16, 16, 16, 16);
                    historyContainer.addView(empty);
                    return;
                }

                for (String date : dates) {
                    addHistoryCard(date,
                            rulesMap.get(date),
                            moodsMap.get(date),
                            ratingsMap.get(date),
                            songsMap.get(date));
                }
            });
        };

        // Load rules
        base.child("rules").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot d : s.getChildren())
                    if (d.getKey() != null && d.getValue(String.class) != null)
                        rulesMap.put(d.getKey(), d.getValue(String.class));
                tryRender.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryRender.run(); }
        });

        // Load ratings
        base.child("ratings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot d : s.getChildren()) {
                    Integer r = d.getValue(Integer.class);
                    if (d.getKey() != null && r != null) ratingsMap.put(d.getKey(), r);
                }
                tryRender.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryRender.run(); }
        });

        // Load moods
        base.child("moods").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot d : s.getChildren())
                    if (d.getKey() != null && d.getValue(String.class) != null)
                        moodsMap.put(d.getKey(), d.getValue(String.class));
                tryRender.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryRender.run(); }
        });

        // Load songs
        base.child("songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot daySnap : s.getChildren()) {
                    if (daySnap.getKey() == null) continue;
                    List<String> songList = new ArrayList<>();
                    for (DataSnapshot song : daySnap.getChildren()) {
                        String val = song.getValue(String.class);
                        if (val != null) songList.add(val);
                    }
                    songsMap.put(daySnap.getKey(), songList);
                }
                tryRender.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryRender.run(); }
        });
    }

    // ─── History Card UI ──────────────────────────────────────────────────────

    private void addHistoryCard(String date, String rule, String mood,
                                Integer rating, List<String> songs) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24, 20, 24, 20);
        card.setBackgroundResource(R.drawable.song_card_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        // Date header
        TextView tvDate = new TextView(getContext());
        tvDate.setText("📅 " + date);
        tvDate.setTextSize(13);
        tvDate.setTextColor(0xFF880E4F);
        tvDate.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvDate);

        // Rule
        if (rule != null) {
            TextView tvRule = new TextView(getContext());
            tvRule.setText("📌 " + rule);
            tvRule.setTextSize(14);
            tvRule.setTextColor(0xFF111111);
            tvRule.setPadding(0, 8, 0, 4);
            card.addView(tvRule);
        }

        // Mood + rating on same row
        LinearLayout metaRow = new LinearLayout(getContext());
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        metaRow.setPadding(0, 4, 0, 4);

        if (mood != null && !mood.isEmpty()) {
            String moodEmoji = mood.equals("Tired") ? "😴"
                    : mood.equals("Okay")  ? "😐"
                    : mood.equals("Great") ? "😊" : "🔥";
            TextView tvMood = new TextView(getContext());
            tvMood.setText(moodEmoji + " " + mood);
            tvMood.setTextSize(13);
            tvMood.setTextColor(0xFFAD1457);
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvMood.setLayoutParams(mp);
            metaRow.addView(tvMood);
        }

        if (rating != null && rating > 0) {
            TextView tvRating = new TextView(getContext());
            tvRating.setText("⭐".repeat(rating));
            tvRating.setTextSize(13);
            metaRow.addView(tvRating);
        }

        card.addView(metaRow);

        // Songs
        if (songs != null && !songs.isEmpty()) {
            TextView tvSongsLabel = new TextView(getContext());
            tvSongsLabel.setText("🎵 Matched songs:");
            tvSongsLabel.setTextSize(12);
            tvSongsLabel.setTextColor(0xFFAD1457);
            tvSongsLabel.setPadding(0, 6, 0, 2);
            card.addView(tvSongsLabel);

            for (String song : songs) {
                TextView tvSong = new TextView(getContext());
                tvSong.setText("  • " + song);
                tvSong.setTextSize(12);
                tvSong.setTextColor(0xFF555555);
                card.addView(tvSong);
            }
        }

        historyContainer.addView(card);
    }

    // ─── AI Advice ────────────────────────────────────────────────────────────

    private void fetchAndGenerateAdvice(String nearbyPlaces) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please log in first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String preference = advicePreferenceInput.getText().toString().trim();
        btnGetAdvice.setEnabled(false);
        tvAdviceResult.setText("🤔 Analyzing your profile...");
        tvAdviceResult.setVisibility(View.VISIBLE);

        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference base = FirebaseDatabase.getInstance(
                        "https://ruleoftheday333-default-rtdb.firebaseio.com")
                .getReference("users").child(uid);

        // Collect all data in parallel then generate advice
        Map<String, Object> collected = new HashMap<>();
        int[] pending = {3};

        Runnable tryGenerate = () -> {
            pending[0]--;
            if (pending[0] > 0) return;
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() ->
                    generateAdviceWithGroq(collected, preference, nearbyPlaces));
        };

        // Profile
        base.child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                collected.put("goal",         s.child("goal").getValue(String.class));
                collected.put("habit",        s.child("habit").getValue(String.class));
                collected.put("difficulty",   s.child("difficulty").getValue(Long.class));
                collected.put("ageRange",     s.child("ageRange").getValue(String.class));
                collected.put("hoursPerDay",  s.child("hoursPerDay").getValue(String.class));
                collected.put("hasKids",      s.child("hasKids").getValue(Boolean.class));
                collected.put("hasPets",      s.child("hasPets").getValue(Boolean.class));
                collected.put("inCity",       s.child("inCity").getValue(Boolean.class));
                collected.put("workFromHome", s.child("workFromHome").getValue(Boolean.class));
                tryGenerate.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryGenerate.run(); }
        });

        // Ratings — last 7
        base.child("ratings").orderByKey().limitToLast(7)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s) {
                        List<Integer> ratings = new ArrayList<>();
                        for (DataSnapshot d : s.getChildren()) {
                            Integer r = d.getValue(Integer.class);
                            if (r != null) ratings.add(r);
                        }
                        collected.put("ratings", ratings);
                        tryGenerate.run();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { tryGenerate.run(); }
                });

        // Calendar — count green/red + calculate streak
        base.child("calendar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                int green = 0, red = 0, streak = 0;
                List<String> dates = new ArrayList<>();
                for (DataSnapshot d : s.getChildren()) {
                    if (d.getKey() != null) dates.add(d.getKey());
                }
                Collections.sort(dates, Collections.reverseOrder());

                java.util.Calendar cal = java.util.Calendar.getInstance();
                for (String date : dates) {
                    String expected = String.format("%d-%02d-%02d",
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH));
                    String status = s.child(date).getValue(String.class);
                    if (status == null) break;
                    if ("green".equals(status)) green++;
                    else red++;
                    if (date.equals(expected) && "green".equals(status)) {
                        streak++;
                        cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
                    } else if (streak == 0) {
                        cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
                    }
                }
                collected.put("greenDays", green);
                collected.put("redDays",   red);
                collected.put("streak",    streak);
                tryGenerate.run();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { tryGenerate.run(); }
        });
    }

    private void generateAdviceWithGroq(Map<String, Object> data, String preference, String nearbyPlaces) {
        executor.execute(() -> {
            try {
                // Build rich context string
                String goal       = data.get("goal")     != null ? data.get("goal").toString()     : "not set";
                String habit      = data.get("habit")    != null ? data.get("habit").toString()     : "not set";
                String ageRange   = data.get("ageRange") != null ? data.get("ageRange").toString()  : "unknown";
                String hours      = data.get("hoursPerDay") != null ? data.get("hoursPerDay").toString() : "0";
                Long   diffLong   = data.get("difficulty") instanceof Long ? (Long) data.get("difficulty") : 0L;
                String difficulty = difficultyLabels[diffLong.intValue()];
                boolean hasKids   = Boolean.TRUE.equals(data.get("hasKids"));
                boolean hasPets   = Boolean.TRUE.equals(data.get("hasPets"));
                boolean inCity    = Boolean.TRUE.equals(data.get("inCity"));
                boolean wfh       = Boolean.TRUE.equals(data.get("workFromHome"));
                int     green     = data.get("greenDays") instanceof Integer ? (int) data.get("greenDays") : 0;
                int     red       = data.get("redDays")   instanceof Integer ? (int) data.get("redDays")   : 0;
                int     streak    = data.get("streak")    instanceof Integer ? (int) data.get("streak")    : 0;

                @SuppressWarnings("unchecked")
                List<Integer> ratings = data.get("ratings") instanceof List
                        ? (List<Integer>) data.get("ratings") : new ArrayList<>();
                double avgRating = ratings.isEmpty() ? 0 :
                        ratings.stream().mapToInt(Integer::intValue).average().orElse(0);

                String prompt =
                        "You are a personal habit coach. Analyze this user's profile and give them personalized advice.\n\n" +
                                "USER PROFILE:\n" +
                                "- Goal: " + goal + "\n" +
                                "- Current bad habit: " + habit + "\n" +
                                "- Age range: " + ageRange + "\n" +
                                "- Hours per day available: " + hours + "\n" +
                                "- Difficulty level: " + difficulty + "\n" +
                                "- Has kids: " + hasKids + ", Has pets: " + hasPets +
                                ", Lives in city: " + inCity + ", Works from home: " + wfh + "\n\n" +
                                "PROGRESS DATA:\n" +
                                "- Days rule followed: " + green + ", Days missed: " + red + "\n" +
                                "- Current streak: " + streak + " days\n" +
                                "- Average rule rating: " + String.format("%.1f", avgRating) + "/5" +
                                (ratings.isEmpty() ? " (no ratings yet)" : " over last " + ratings.size() + " rules") + "\n\n" +
                                "USER PREFERENCE FOR ADVICE FORMAT: " +
                                (preference.isEmpty() ? "give a helpful, friendly advice" : preference) + "\n\n" +
                                "Keep the advice concise and direct — no more than 3-4 sentences. " +
                                "Then add a RESOURCES section with 2-3 specific recommendations " +
                                "(books, apps, websites, videos, or podcasts) that directly help with their goal. " +
                                (!nearbyPlaces.isEmpty() ? "NEARBY PLACES (real places near user, mention them by name):\n" + nearbyPlaces + "\n" : "") +
                                "Format as:\nAdvice: <your advice>\nResources:\n• <resource 1>\n• <resource 2>\n• <resource 3>\n" +
                                (!nearbyPlaces.isEmpty() ? "Nearby: <mention the actual nearby places above>\n" : "") +
                                "Be like a smart friend, not a generic coach. Reference their actual goal.";

                org.json.JSONObject message = new org.json.JSONObject();
                message.put("role", "user");
                message.put("content", prompt);

                org.json.JSONArray messages = new org.json.JSONArray();
                messages.put(message);

                org.json.JSONObject body = new org.json.JSONObject();
                body.put("model", GROQ_MODEL);
                body.put("messages", messages);
                body.put("max_tokens", 250);
                body.put("temperature", 0.7);

                URL url = new URL(GROQ_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + getGroqKey());
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getResponseCode() == 200
                                ? conn.getInputStream()
                                : conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                org.json.JSONObject json = new org.json.JSONObject(sb.toString());
                String advice = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    tvAdviceResult.setText(advice);
                    btnGetAdvice.setEnabled(true);
                });

            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    tvAdviceResult.setText("Failed to get advice: " + e.getMessage());
                    btnGetAdvice.setEnabled(true);
                });
            }
        });
    }

    private String getGroqKey() {
        try {
            java.util.Properties props = new java.util.Properties();
            // Try to read from assets or return placeholder
            return "YOUR_GROQ_KEY_HERE";
        } catch (Exception e) {
            return "";
        }
    }

    // ─── Load Profile ─────────────────────────────────────────────────────────

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance("https://ruleoftheday333-default-rtdb.firebaseio.com")
                .getReference("users").child(uid).child("profile")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.child("goal").getValue() != null)
                            goalInput.setText(snap.child("goal").getValue(String.class));
                        if (snap.child("habit").getValue() != null)
                            habitInput.setText(snap.child("habit").getValue(String.class));
                        if (snap.child("hoursPerDay").getValue() != null)
                            hoursInput.setText(snap.child("hoursPerDay").getValue(String.class));

                        Long diff = snap.child("difficulty").getValue(Long.class);
                        if (diff != null) {
                            sliderDifficulty.setProgress(diff.intValue());
                            tvDifficultyLabel.setText(difficultyLabels[diff.intValue()]);
                        }

                        String age = snap.child("ageRange").getValue(String.class);
                        if (age != null) {
                            switch (age) {
                                case "<18":   radioAge.check(R.id.ageUnder18); break;
                                case "18-25": radioAge.check(R.id.age18to25);  break;
                                case "26-35": radioAge.check(R.id.age26to35);  break;
                                case "36+":   radioAge.check(R.id.age36plus);  break;
                            }
                        }

                        Boolean kids = snap.child("hasKids").getValue(Boolean.class);
                        Boolean pets = snap.child("hasPets").getValue(Boolean.class);
                        Boolean city = snap.child("inCity").getValue(Boolean.class);
                        Boolean wfh  = snap.child("workFromHome").getValue(Boolean.class);
                        if (kids != null) switchKids.setChecked(kids);
                        if (pets != null) switchPets.setChecked(pets);
                        if (city != null) switchCity.setChecked(city);
                        if (wfh  != null) switchWFH.setChecked(wfh);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // ─── Save Profile ─────────────────────────────────────────────────────────

    private void saveUserData() {
        String goal  = goalInput.getText().toString().trim();
        String habit = habitInput.getText().toString().trim();
        String hours = hoursInput.getText().toString().trim();

        if (goal.isEmpty() || habit.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), getString(R.string.toast_user_not_logged_in), Toast.LENGTH_SHORT).show();
            return;
        }

        String ageRange = "";
        int ageId = radioAge.getCheckedRadioButtonId();
        if      (ageId == R.id.ageUnder18) ageRange = "<18";
        else if (ageId == R.id.age18to25)  ageRange = "18-25";
        else if (ageId == R.id.age26to35)  ageRange = "26-35";
        else if (ageId == R.id.age36plus)  ageRange = "36+";

        Map<String, Object> profile = new HashMap<>();
        profile.put("goal",         goal);
        profile.put("habit",        habit);
        profile.put("hoursPerDay",  hours.isEmpty() ? "0" : hours);
        profile.put("difficulty",   sliderDifficulty.getProgress());
        profile.put("ageRange",     ageRange);
        profile.put("hasKids",      switchKids.isChecked());
        profile.put("hasPets",      switchPets.isChecked());
        profile.put("inCity",       switchCity.isChecked());
        profile.put("workFromHome", switchWFH.isChecked());

        String uid = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance("https://ruleoftheday333-default-rtdb.firebaseio.com")
                .getReference("users").child(uid).child("profile")
                .updateChildren(profile)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), getString(R.string.toast_saved), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), getString(R.string.toast_error) + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}