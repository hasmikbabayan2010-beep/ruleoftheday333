package com.example.ruleoftheday333.itunes;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ItunesPreviewHelper {

    // ──────────────────────────────────────────────────────────
    // Searches iTunes for "trackName artistName", returns the
    // previewUrl of the best match (or null if nothing found).
    // This is called AFTER Spotify gives us the song name.
    // ──────────────────────────────────────────────────────────
    public static String fetchPreviewUrl(String trackName, String artistName) {
        try {
            // Combine track + artist for a precise iTunes search
            String query = URLEncoder.encode(trackName + " " + artistName, "UTF-8");
            URL url = new URL(
                    "https://itunes.apple.com/search?term=" + query
                            + "&media=music&limit=5");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject root    = new JSONObject(sb.toString());
            JSONArray  results = root.getJSONArray("results");

            if (results.length() == 0) return null;

            // Pick the result whose artist name most closely matches
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String     artist = result.optString("artistName", "");
                String     preview = result.optString("previewUrl", "");

                if (!preview.isEmpty() &&
                        artist.toLowerCase().contains(artistName.toLowerCase())) {
                    return preview;  // ✅ Good match with a preview
                }
            }

            // Fallback: just return first result's preview if it has one
            String fallback = results.getJSONObject(0).optString("previewUrl", "");
            return fallback.isEmpty() ? null : fallback;

        } catch (Exception e) {
            Log.e("ItunesPreview", "Failed: " + e.getMessage(), e);
            return null;
        }
    }
}