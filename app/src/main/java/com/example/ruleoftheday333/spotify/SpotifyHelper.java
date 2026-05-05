package com.example.ruleoftheday333.spotify;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SpotifyHelper {
    private static final String CLIENT_ID     = "9eed91a4238d4e798eedb25aa6d790ee";
    private static final String CLIENT_SECRET = "f3c173c58d6b4c59843723bba80a0bb1";

    // Holds a cached access token so we don't re-auth every request
    private static String cachedToken = null;

    // ──────────────────────────────────────────────────────────
    // Step A: Get an access token using Client Credentials flow.
    // Spotify's free "app-only" auth — no user login needed.
    // ──────────────────────────────────────────────────────────
    private static String getAccessToken() throws Exception {
        if (cachedToken != null) return cachedToken; // reuse if we already have one

        URL url = new URL("https://accounts.spotify.com/api/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);  // 8 seconds to connect
        conn.setReadTimeout(8000);     // 8 seconds to read response
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        // Basic Auth header: Base64(clientId:clientSecret)
        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String encoded = android.util.Base64.encodeToString(
                credentials.getBytes(), android.util.Base64.NO_WRAP);
        conn.setRequestProperty("Authorization", "Basic " + encoded);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        OutputStream os = conn.getOutputStream();
        os.write("grant_type=client_credentials".getBytes());
        os.flush();
        os.close();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        JSONObject json = new JSONObject(sb.toString());
        cachedToken = json.getString("access_token");
        return cachedToken;
    }

    // ──────────────────────────────────────────────────────────
    // Step B: Search Spotify for tracks matching the mood term.
    // Returns a list of SpotifyTrack objects (name + artist).
    // ──────────────────────────────────────────────────────────
    public static List<SpotifyTrack> searchTracks(String moodQuery, int limit) {
        List<SpotifyTrack> tracks = new ArrayList<>();
        try {
            String token = getAccessToken();
            String encodedQuery = URLEncoder.encode(moodQuery, "UTF-8");

            // We add "year:2023-2025" to bias toward fresh releases
            String fullQuery = URLEncoder.encode(moodQuery + " year:2023-2025", "UTF-8");

            URL url = new URL("https://api.spotify.com/v1/search?q="
                    + fullQuery + "&type=track&limit=" + limit + "&market=US");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);  // 8 seconds to connect
            conn.setReadTimeout(8000);     // 8 seconds to read response
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject root    = new JSONObject(sb.toString());
            JSONArray  items   = root.getJSONObject("tracks").getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject track      = items.getJSONObject(i);
                String     trackName  = track.getString("name");
                String     artistName = track.getJSONArray("artists")
                        .getJSONObject(0)
                        .getString("name");
                String     albumArt   = track.getJSONObject("album")
                        .getJSONArray("images")
                        .getJSONObject(0)   // first = largest
                        .getString("url");
                String     spotifyUrl = track.getJSONObject("external_urls")
                        .getString("spotify");

                tracks.add(new SpotifyTrack(trackName, artistName, albumArt, spotifyUrl));
            }

        } catch (Exception e) {
            Log.e("SpotifyHelper", "Search failed: " + e.getMessage(), e);
            // If token expired (401), clear cache so next call re-authenticates
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                cachedToken = null;
            }
        }
        return tracks;
    }
}