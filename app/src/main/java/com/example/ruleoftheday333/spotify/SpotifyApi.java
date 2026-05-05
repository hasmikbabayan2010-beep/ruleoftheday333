package com.example.ruleoftheday333.spotify;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SpotifyApi {

    @GET("v1/search")
    Call<SpotifyResponse> searchTracks(
            @Header("Authorization") String token,
            @Query("q") String query,
            @Query("type") String type,
            @Query("limit") int limit
    );
}