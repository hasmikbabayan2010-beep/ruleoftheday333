package com.example.ruleoftheday333.spotify;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpotifyClient {

    private static final String BASE_URL = "https://api.spotify.com/";

    public static SpotifyApi getApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(SpotifyApi.class);
    }
}