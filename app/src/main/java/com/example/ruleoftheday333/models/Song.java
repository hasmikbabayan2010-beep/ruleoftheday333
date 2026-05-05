package com.example.ruleoftheday333.models;

public class Song {
    private String name;
    private String artist;
    private String albumCoverUrl;
    private String previewUrl;

    public Song(String name, String artist, String albumCoverUrl, String previewUrl) {
        this.name = name;
        this.artist = artist;
        this.albumCoverUrl = albumCoverUrl;
        this.previewUrl = previewUrl;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumCoverUrl() {
        return albumCoverUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }
}