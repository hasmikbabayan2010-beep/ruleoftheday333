package com.example.ruleoftheday333.spotify;

import java.util.List;

public class SpotifyResponse {
    public Tracks tracks;

    public static class Tracks {
        public List<Item> items;
    }

    public static class Item {
        public String name;
        public List<Artist> artists;
    }

    public static class Artist {
        public String name;
    }
}