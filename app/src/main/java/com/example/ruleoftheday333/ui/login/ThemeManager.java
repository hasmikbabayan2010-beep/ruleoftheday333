package com.example.ruleoftheday333.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

public class ThemeManager {

    private static final String PREFS     = "app_settings";
    private static final String KEY_THEME = "color_theme";

    // Theme names
    public static final String PINK           = "pink";
    public static final String PURPLE         = "purple";
    public static final String BLUE           = "blue";
    public static final String GREEN          = "green";
    public static final String ORANGE         = "orange";
    public static final String RED            = "red";
    public static final String PASTEL_PEACH   = "pastel_peach";
    public static final String PASTEL_MINT    = "pastel_mint";
    public static final String GRADIENT       = "gradient";
    public static final String VIVID          = "vivid";
    public static final String YELLOW         = "yellow";

    public static String getSavedTheme(Context ctx) {
        return ctx.getSharedPreferences(PREFS, 0).getString(KEY_THEME, PINK);
    }

    public static void saveTheme(Context ctx, String theme) {
        ctx.getSharedPreferences(PREFS, 0).edit().putString(KEY_THEME, theme).apply();
    }

    /** Apply the theme background to any root view */
    public static void apply(Context ctx, View rootView) {
        String theme = getSavedTheme(ctx);
        applyTheme(theme, rootView);
    }

    public static void applyTheme(String theme, View rootView) {
        switch (theme) {
            case PURPLE:
                rootView.setBackgroundColor(Color.parseColor("#F3E5F5")); break;
            case BLUE:
                rootView.setBackgroundColor(Color.parseColor("#E3F2FD")); break;
            case GREEN:
                rootView.setBackgroundColor(Color.parseColor("#E8F5E9")); break;
            case ORANGE:
                rootView.setBackgroundColor(Color.parseColor("#FFF3E0")); break;
            case RED:
                rootView.setBackgroundColor(Color.parseColor("#FFEBEE")); break;
            case PASTEL_PEACH:
                rootView.setBackgroundColor(Color.parseColor("#FFF8E7")); break;
            case PASTEL_MINT:
                rootView.setBackgroundColor(Color.parseColor("#E0F7FA")); break;
            case GRADIENT:
                GradientDrawable grad = new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                                Color.parseColor("#FFB3C6"),
                                Color.parseColor("#C3B1E1"),
                                Color.parseColor("#B5EAD7")
                        });
                rootView.setBackground(grad);
                break;
            case VIVID:
                GradientDrawable vivid = new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                                Color.parseColor("#FF6B6B"),
                                Color.parseColor("#FFE66D"),
                                Color.parseColor("#4ECDC4")
                        });
                rootView.setBackground(vivid);
                break;
            case YELLOW:
                rootView.setBackgroundColor(Color.parseColor("#FFFDE7")); break;
            default: // PINK
                rootView.setBackgroundColor(Color.parseColor("#FFF0F5")); break;
        }
    }

    public static String getLabel(String theme) {
        switch (theme) {
            case PURPLE:       return "💜 Purple";
            case BLUE:         return "💙 Blue";
            case GREEN:        return "💚 Green";
            case ORANGE:       return "🧡 Orange";
            case RED:          return "❤️ Red";
            case PASTEL_PEACH: return "🍑 Pastel Peach";
            case PASTEL_MINT:  return "🌿 Pastel Mint";
            case GRADIENT:     return "🌈 Colorful Gradient";
            case VIVID:        return "⚡ Vivid";
            case YELLOW:       return "💛 Yellow";
            default:           return "🌸 Pink";
        }
    }

    public static String[] allThemes() {
        return new String[]{
                PINK, PURPLE, BLUE, GREEN, ORANGE,
                RED, YELLOW, PASTEL_PEACH, PASTEL_MINT, GRADIENT, VIVID
        };
    }
}