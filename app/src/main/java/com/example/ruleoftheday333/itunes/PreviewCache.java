package com.example.ruleoftheday333.itunes;

import android.content.Context;
import android.content.SharedPreferences;

public class PreviewCache {

    private static final String PREF_NAME = "preview_cache";

    public static void savePreview(Context context, String key, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(key, url).apply();
    }

    public static String getPreview(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }
}
