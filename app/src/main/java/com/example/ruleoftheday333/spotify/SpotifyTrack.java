package com.example.ruleoftheday333.spotify;

public class SpotifyTrack {
    public String trackName;
    public String artistName;
    public String albumArtUrl;   // Spotify's album cover image
    public String spotifyUrl;    // "Open in Spotify" deep link

    public SpotifyTrack(String trackName, String artistName,
                        String albumArtUrl, String spotifyUrl) {
        this.trackName   = trackName;
        this.artistName  = artistName;
        this.albumArtUrl = albumArtUrl;
        this.spotifyUrl  = spotifyUrl;
    }
}