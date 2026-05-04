package com.example.ruleoftheday333.fragments.itunes;

import java.util.List;

// This class matches the JSON structure of your iTunes API / 1.json
public class ItunesResponse {
    public List<Result> results;

    public static class Result {
        public String trackName;       // Song title
        public String artistName;      // Artist name
        public String artworkUrl100;   // Album cover URL
        public String previewUrl;      // 30-sec preview URL
    }
}