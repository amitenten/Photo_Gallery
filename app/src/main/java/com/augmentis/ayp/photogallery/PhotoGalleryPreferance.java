package com.augmentis.ayp.photogallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Amita on 8/19/2016.
 */
public class PhotoGalleryPreferance {
    private static final String TAG = "PhotoGalleryPreferance";
    private static final String PREF_SEARCH_KEY = "SEARCH_KEY";

    public static String getStoredSearchKey(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_SEARCH_KEY, null);
    }

    public static void setStoredSearchKey(Context context, String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(PREF_SEARCH_KEY, key).apply();

    }
}