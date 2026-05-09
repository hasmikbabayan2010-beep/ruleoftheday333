package com.example.ruleoftheday333.spotify;

public class SpotifyTrack {
    public String trackName;
    public String artistName;
    public String albumArtUrl;   // Spotify album cover
    public String spotifyUrl;    // "Open in Spotify" deep link
    public String previewUrl;    // Spotify's own 30s preview (may be null)
    public String releaseDate;   // e.g. "2024-03-15" — used to guarantee 1 modern song

    public SpotifyTrack(String trackName, String artistName,
                        String albumArtUrl, String spotifyUrl,
                        String previewUrl, String releaseDate) {
        this.trackName   = trackName;
        this.artistName  = artistName;
        this.albumArtUrl = albumArtUrl;
        this.spotifyUrl  = spotifyUrl;
        this.previewUrl  = previewUrl;
        this.releaseDate = releaseDate;
    }
}