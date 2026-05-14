package com.example.ruleoftheday333.share;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.OutputStream;

public class ShareHelper {

    /**
     * Saves bitmap to gallery and opens the share sheet.
     * Call from background thread — saving is synchronous.
     * Returns the Uri of the saved image (or null on failure).
     */
    public static Uri saveAndShare(Context context, Bitmap bitmap, String filename) {
        Uri imageUri = saveBitmapToGallery(context, bitmap, filename);
        if (imageUri != null) {
            openShareSheet(context, imageUri);
        }
        return imageUri;
    }

    public static Uri saveBitmapToGallery(Context context, Bitmap bitmap, String filename) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/RuleOfTheDay");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }

            Uri uri = context.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri == null) return null;

            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                context.getContentResolver().update(uri, values, null, null);
            }

            return uri;

        } catch (Exception e) {
            Log.e("ShareHelper", "Save failed: " + e.getMessage(), e);
            return null;
        }
    }

    public static void openShareSheet(Context context, Uri imageUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "My daily rule 🔥 — Rule of the Day app");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(shareIntent, "Share your rule");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }
}