package com.example.ruleoftheday333.itunes;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ItunesPreviewHelper {

    public static String fetchPreviewUrl(Context context, String trackName, String artistName) {
        HttpURLConnection conn = null;

        try {
            if (trackName == null || artistName == null) return null;

            String key = (trackName + "_" + artistName).toLowerCase().trim();

            // ✅ CACHE CHECK
            String cached = PreviewCache.getPreview(context, key);
            if (cached != null) {
                return cached;
            }

            String query = URLEncoder.encode(trackName + " " + artistName, "UTF-8");

            URL url = new URL(
                    "https://itunes.apple.com/search?term=" + query +
                            "&media=music&limit=5"
            );

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            JSONObject root = new JSONObject(sb.toString());
            JSONArray results = root.optJSONArray("results");

            if (results == null || results.length() == 0) return null;

            String targetArtist = artistName.toLowerCase().trim();

            // ✅ BEST MATCH FIRST
            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);

                String artist = obj.optString("artistName", "").toLowerCase();
                String preview = obj.optString("previewUrl", null);

                if (preview != null && !preview.isEmpty()) {
                    if (artist.contains(targetArtist) || targetArtist.contains(artist)) {
                        PreviewCache.savePreview(context, key, preview);
                        return preview;
                    }
                }
            }

            // ✅ FALLBACK
            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                String preview = obj.optString("previewUrl", null);

                if (preview != null && !preview.isEmpty()) {
                    PreviewCache.savePreview(context, key, preview);
                    return preview;
                }
            }

        } catch (Exception e) {
            Log.e("ItunesPreview", "Failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) conn.disconnect();
        }

        return null;
    }
}