package com.example.ruleoftheday333.fragments.itunes;

import android.content.Context;
import java.io.InputStream;
import java.io.IOException;

import com.example.ruleoftheday333.fragments.itunes.ItunesResponse;
import com.google.gson.Gson;

public class ItunesHelper {

    // Reads 1.json from assets and returns an ItunesResponse object
    public static com.example.ruleoftheday333.itunes.ItunesResponse loadItunesJson(Context context) {
        try {
            InputStream is = context.getAssets().open("1.json"); // <- make sure 1.json is in assets/
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");

            // Parse JSON into Java object
            return new Gson().fromJson(json, ItunesResponse.class);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}