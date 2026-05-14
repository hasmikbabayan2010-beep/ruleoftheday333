package com.example.ruleoftheday333.share;

import android.content.Context;
import android.graphics.*;
import android.util.Log;

import com.bumptech.glide.Glide;

public class ShareCardRenderer {

    private static final int CARD_WIDTH  = 1080;
    private static final int CARD_HEIGHT = 1920; // 9:16 — Instagram stories ratio

    /**
     * Renders the share card as a Bitmap.
     * Must be called from a background thread (Glide.get() blocks).
     */
    public static Bitmap render(Context context,
                                String appName,
                                String ruleText,
                                String songName,
                                String artistName,
                                String albumArtUrl,
                                int streakCount,
                                String userNote) {

        Bitmap bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // ── Background gradient ───────────────────────────────────────────────
        LinearGradient gradient = new LinearGradient(
                0, 0, CARD_WIDTH, CARD_HEIGHT,
                new int[]{0xFFFF6B9D, 0xFFC44FD6, 0xFF7B5EF8, 0xFF4FACFE},
                null,
                Shader.TileMode.CLAMP
        );
        Paint bgPaint = new Paint();
        bgPaint.setShader(gradient);
        canvas.drawRect(0, 0, CARD_WIDTH, CARD_HEIGHT, bgPaint);

        // ── Frosted white overlay panel ───────────────────────────────────────
        Paint panelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelPaint.setColor(0x33FFFFFF); // semi-transparent white
        RectF panel = new RectF(60, 200, CARD_WIDTH - 60, CARD_HEIGHT - 200);
        canvas.drawRoundRect(panel, 60, 60, panelPaint);

        // ── App name ──────────────────────────────────────────────────────────
        Paint appNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        appNamePaint.setColor(Color.WHITE);
        appNamePaint.setTextSize(72);
        appNamePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        appNamePaint.setTextAlign(Paint.Align.CENTER);
        appNamePaint.setShadowLayer(8, 0, 4, 0x88000000);
        canvas.drawText(appName, CARD_WIDTH / 2f, 160, appNamePaint);

        // ── Streak badge ──────────────────────────────────────────────────────
        String streakLabel = "🔥 " + streakCount + " day streak";
        Paint streakPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        streakPaint.setColor(0xFFFFD700);
        streakPaint.setTextSize(52);
        streakPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        streakPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(streakLabel, CARD_WIDTH / 2f, 320, streakPaint);

        // ── "Today's Rule" label ──────────────────────────────────────────────
        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(0xFFFFFFFF);
        labelPaint.setAlpha(180);
        labelPaint.setTextSize(40);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("TODAY'S RULE", CARD_WIDTH / 2f, 420, labelPaint);

        // ── Rule text (wrapped) ───────────────────────────────────────────────
        Paint rulePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rulePaint.setColor(Color.WHITE);
        rulePaint.setTextSize(58);
        rulePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        rulePaint.setTextAlign(Paint.Align.CENTER);
        rulePaint.setShadowLayer(6, 0, 3, 0x88000000);
        drawWrappedText(canvas, ruleText, rulePaint, 120, 500, CARD_WIDTH - 120, 75);

        // ── Album art ─────────────────────────────────────────────────────────
        int artSize = 300;
        int artLeft = (CARD_WIDTH - artSize) / 2;
        int artTop  = 900;

        if (albumArtUrl != null && !albumArtUrl.isEmpty()) {
            try {
                Bitmap albumArt = Glide.with(context)
                        .asBitmap()
                        .load(albumArtUrl)
                        .submit(artSize, artSize)
                        .get();

                // Draw with rounded corners
                Bitmap rounded = getRoundedBitmap(albumArt, 30);
                canvas.drawBitmap(rounded, artLeft, artTop, null);

                // Drop shadow under album art
                Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                shadowPaint.setColor(0x44000000);
                shadowPaint.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.NORMAL));
                canvas.drawRoundRect(
                        new RectF(artLeft + 10, artTop + 10, artLeft + artSize + 10, artTop + artSize + 10),
                        30, 30, shadowPaint);

            } catch (Exception e) {
                Log.e("ShareCard", "Album art load failed: " + e.getMessage());
                // Draw placeholder circle
                Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                circlePaint.setColor(0x44FFFFFF);
                canvas.drawCircle(CARD_WIDTH / 2f, artTop + artSize / 2f, artSize / 2f, circlePaint);
            }
        }

        // ── Song name + artist ────────────────────────────────────────────────
        int songTop = artTop + artSize + 40;

        Paint songPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        songPaint.setColor(Color.WHITE);
        songPaint.setTextSize(48);
        songPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        songPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(truncate(songName, 30), CARD_WIDTH / 2f, songTop, songPaint);

        Paint artistPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        artistPaint.setColor(0xCCFFFFFF);
        artistPaint.setTextSize(38);
        artistPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(truncate(artistName, 35), CARD_WIDTH / 2f, songTop + 55, artistPaint);

        // ── User note ─────────────────────────────────────────────────────────
        if (userNote != null && !userNote.trim().isEmpty()) {
            // Note background pill
            Paint noteBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            noteBgPaint.setColor(0x44FFFFFF);
            int noteTop = songTop + 120;
            RectF noteBg = new RectF(120, noteTop - 50, CARD_WIDTH - 120, noteTop + 160);
            canvas.drawRoundRect(noteBg, 40, 40, noteBgPaint);

            // Note label
            Paint noteLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            noteLabelPaint.setColor(0xCCFFFFFF);
            noteLabelPaint.setTextSize(34);
            noteLabelPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("✏️ My note", CARD_WIDTH / 2f, noteTop, noteLabelPaint);

            // Note text
            Paint noteTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            noteTextPaint.setColor(Color.WHITE);
            noteTextPaint.setTextSize(42);
            noteTextPaint.setTextAlign(Paint.Align.CENTER);
            drawWrappedText(canvas, userNote, noteTextPaint, 140, noteTop + 60, CARD_WIDTH - 140, 55);
        }

        // ── Bottom branding ───────────────────────────────────────────────────
        Paint brandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        brandPaint.setColor(0xAAFFFFFF);
        brandPaint.setTextSize(34);
        brandPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Rule of the Day", CARD_WIDTH / 2f, CARD_HEIGHT - 120, brandPaint);

        return bitmap;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void drawWrappedText(Canvas canvas, String text, Paint paint,
                                        float x, float y, float maxX, float lineHeight) {
        if (text == null || text.isEmpty()) return;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float currentY = y;

        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (paint.measureText(test) > (maxX - x)) {
                canvas.drawText(line.toString(), (x + maxX) / 2f, currentY, paint);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            canvas.drawText(line.toString(), (x + maxX) / 2f, currentY, paint);
        }
    }

    private static Bitmap getRoundedBitmap(Bitmap src, int radius) {
        Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawRoundRect(new RectF(0, 0, src.getWidth(), src.getHeight()),
                radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, paint);
        return output;
    }

    private static String truncate(String text, int maxChars) {
        if (text == null) return "";
        return text.length() > maxChars ? text.substring(0, maxChars - 1) + "…" : text;
    }
}