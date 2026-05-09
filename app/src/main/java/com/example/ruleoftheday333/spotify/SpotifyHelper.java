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

    private static String cachedToken = null;
    private static long   tokenExpiryMs = 0; // epoch ms when the token expires

    // ── Auth ─────────────────────────────────────────────────────────────────
    private static String getAccessToken() throws Exception {
        // Refresh if missing or within 60 seconds of expiry
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiryMs - 60_000) {
            return cachedToken;
        }

        URL url = new URL("https://accounts.spotify.com/api/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

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

        JSONObject json  = new JSONObject(sb.toString());
        cachedToken      = json.getString("access_token");
        int expiresIn    = json.optInt("expires_in", 3600); // Spotify gives 3600s
        tokenExpiryMs    = System.currentTimeMillis() + expiresIn * 1000L;
        return cachedToken;
    }

    // ── Mood-matched search (returns 10 results, caller picks indices 0,2,4) ─
    public static List<SpotifyTrack> searchTracks(String moodQuery, int limit) {
        try {
            String token = getAccessToken();
            // year:2023-2025 biases toward recent music
            String fullQuery = URLEncoder.encode(moodQuery + " year:2023-2025", "UTF-8");

            URL url = new URL("https://api.spotify.com/v1/search?q="
                    + fullQuery + "&type=track&limit=" + limit + "&market=US");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            return parseTracks(new JSONObject(sb.toString()));

        } catch (Exception e) {
            Log.e("SpotifyHelper", "searchTracks failed: " + e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("401")) { cachedToken = null; tokenExpiryMs = 0; }
            return new ArrayList<>();
        }
    }

    // ── Most-recent track: uses tag:new to force brand-new releases ───────────
    // Guaranteed to be from the last few weeks regardless of mood match quality.
    public static SpotifyTrack fetchMostRecentTrack(String moodQuery) {
        try {
            String token = getAccessToken();

            // tag:new is Spotify's filter for albums released in the last 2 weeks
            String fullQuery = URLEncoder.encode(moodQuery + " tag:new", "UTF-8");

            URL url = new URL("https://api.spotify.com/v1/search?q="
                    + fullQuery + "&type=track&limit=10&market=US");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            List<SpotifyTrack> tracks = parseTracks(new JSONObject(sb.toString()));
            if (tracks.isEmpty()) return null;

            // Sort by release date descending — pick the absolute newest
            tracks.sort((a, b) -> {
                String da = a.releaseDate != null ? a.releaseDate : "";
                String db = b.releaseDate != null ? b.releaseDate : "";
                return db.compareTo(da);
            });

            return tracks.get(0);

        } catch (Exception e) {
            Log.e("SpotifyHelper", "fetchMostRecentTrack failed: " + e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("401")) { cachedToken = null; tokenExpiryMs = 0; }
            return null;
        }
    }

    // ── Shared JSON parser ────────────────────────────────────────────────────
    private static List<SpotifyTrack> parseTracks(JSONObject root) throws Exception {
        List<SpotifyTrack> tracks = new ArrayList<>();
        JSONArray items = root.getJSONObject("tracks").getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject track = items.getJSONObject(i);

            String trackName  = track.getString("name");
            String artistName = track.getJSONArray("artists")
                    .getJSONObject(0).getString("name");
            String albumArt   = track.getJSONObject("album")
                    .getJSONArray("images").getJSONObject(0).getString("url");
            String spotifyUrl = track.getJSONObject("external_urls")
                    .getString("spotify");

            // Spotify preview_url — null for many tracks, iTunes is fallback
            String previewUrl = track.isNull("preview_url")
                    ? null : track.optString("preview_url", null);

            String releaseDate = track.getJSONObject("album")
                    .optString("release_date", "");

            tracks.add(new SpotifyTrack(
                    trackName, artistName, albumArt,
                    spotifyUrl, previewUrl, releaseDate));
        }
        return tracks;
    }
}