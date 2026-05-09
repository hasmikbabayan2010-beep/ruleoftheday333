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
        try {
            if (trackName == null || artistName == null) return null;

            String key = (trackName + "_" + artistName).toLowerCase().trim();

            // Cache check — avoid hitting iTunes repeatedly for the same song
            String cached = PreviewCache.getPreview(context, key);
            if (cached != null) return cached;

            String targetArtist = artistName.toLowerCase().trim();
            String targetTrack  = trackName.toLowerCase().trim();

            // Search 1: "trackName artistName" — narrow and accurate
            String preview = searchItunes(trackName + " " + artistName, 10,
                    targetArtist, targetTrack, true);

            // Search 2: track name only with limit 25 — wider net, still artist-checked
            if (preview == null) {
                preview = searchItunes(trackName, 25,
                        targetArtist, targetTrack, false);
            }

            if (preview != null) {
                PreviewCache.savePreview(context, key, preview);
            }

            return preview;

        } catch (Exception e) {
            Log.e("ItunesPreview", "Failed: " + e.getMessage(), e);
            return null;
        }
    }

    private static String searchItunes(String query, int limit,
                                       String targetArtist, String targetTrack,
                                       boolean requireTrackMatch) {
        HttpURLConnection conn = null;
        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            URL url = new URL("https://itunes.apple.com/search?term=" + encoded
                    + "&media=music&entity=song&limit=" + limit);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject root    = new JSONObject(sb.toString());
            JSONArray  results = root.optJSONArray("results");
            if (results == null || results.length() == 0) return null;

            // Pass 1: artist + track both match
            for (int i = 0; i < results.length(); i++) {
                JSONObject obj  = results.getJSONObject(i);
                String artist   = obj.optString("artistName", "").toLowerCase();
                String track    = obj.optString("trackName",  "").toLowerCase();
                String preview  = obj.optString("previewUrl", null);

                if (preview == null || preview.isEmpty()) continue;

                boolean artistMatch = artist.contains(targetArtist)
                        || targetArtist.contains(artist);
                boolean trackMatch  = track.contains(targetTrack)
                        || targetTrack.contains(track);

                if (artistMatch && trackMatch) return preview;
            }

            // Pass 2: artist matches only (handles "feat." / title variants)
            if (!requireTrackMatch) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject obj = results.getJSONObject(i);
                    String artist  = obj.optString("artistName", "").toLowerCase();
                    String preview = obj.optString("previewUrl", null);

                    if (preview == null || preview.isEmpty()) continue;

                    boolean artistMatch = artist.contains(targetArtist)
                            || targetArtist.contains(artist);

                    if (artistMatch) return preview;
                }
            }

        } catch (Exception e) {
            Log.e("ItunesPreview", "searchItunes failed for: " + query + " — " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }
}