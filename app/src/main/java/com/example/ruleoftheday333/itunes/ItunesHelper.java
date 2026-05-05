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

    public static ItunesResponse loadItunesJson(Context context, String searchTerm) {
        try {
            searchTerm = URLEncoder.encode(searchTerm, "UTF-8");
            String urlStr = "https://itunes.apple.com/search?term=" + searchTerm + "&limit=10";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

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
            e.printStackTrace();
            return null;
        }
    }

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

    public static List<Result> fetchPreviews(String query) {
        List<Result> resultsList = new ArrayList<>();
        HttpURLConnection connection = null;

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = "https://itunes.apple.com/search?term=" + encodedQuery + "&entity=song&limit=1";

            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            StringBuilder sb = new StringBuilder();
            int data = reader.read();
            while (data != -1) {
                sb.append((char) data);
                data = reader.read();
            }

            JSONObject json = new JSONObject(sb.toString());
            JSONArray results = json.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                resultsList.add(new Result(
                        obj.getString("trackName"),
                        obj.getString("artistName"),
                        obj.getString("artworkUrl100"),
                        obj.getString("previewUrl")
                ));
            }
        } catch (Exception e) {
            Log.e("ItunesHelper", "Error fetching preview", e);
        } finally {
            if (connection != null) connection.disconnect();
        }

        return resultsList;
    }
}