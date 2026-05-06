package com.example.ruleoftheday333.itunes;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ItunesHelper {

    // =========================
    // MAIN JSON FETCH (GSON)
    // =========================
    public static ItunesResponse loadItunesJson(Context context, String searchTerm) {
        HttpURLConnection conn = null;

        try {
            String encoded = URLEncoder.encode(searchTerm, "UTF-8");
            String urlStr = "https://itunes.apple.com/search?term=" + encoded + "&limit=10";

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            is.close();

            return new Gson().fromJson(sb.toString(), ItunesResponse.class);

        } catch (Exception e) {
            Log.e("ItunesHelper", "loadItunesJson error", e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // =========================
    // RESULT MODEL
    // =========================
    public static class Result {
        public String trackName;
        public String artistName;
        public String artworkUrl100;
        public String previewUrl;

        public Result(String trackName, String artistName, String artworkUrl100, String previewUrl) {
            this.trackName = trackName;
            this.artistName = artistName;
            this.artworkUrl100 = artworkUrl100;
            this.previewUrl = previewUrl;
        }
    }

    // =========================
    // FETCH PREVIEW (SAFE)
    // =========================
    public static List<Result> fetchPreviews(String query) {
        List<Result> resultsList = new ArrayList<>();
        HttpURLConnection connection = null;

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = "https://itunes.apple.com/search?term=" + encodedQuery + "&entity=song&limit=3";

            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            inputStream.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONArray results = json.optJSONArray("results");

            if (results == null) return resultsList;

            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);

                String trackName   = obj.optString("trackName", "Unknown");
                String artistName  = obj.optString("artistName", "Unknown");
                String artworkUrl  = obj.optString("artworkUrl100", "");
                String previewUrl  = obj.optString("previewUrl", null);

                resultsList.add(new Result(trackName, artistName, artworkUrl, previewUrl));
            }

        } catch (Exception e) {
            Log.e("ItunesHelper", "Error fetching preview", e);
        } finally {
            if (connection != null) connection.disconnect();
        }

        return resultsList;
    }
}