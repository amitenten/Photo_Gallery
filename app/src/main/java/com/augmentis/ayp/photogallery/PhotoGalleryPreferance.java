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
    private static final String PREF_LAST_ID = "LAST_ID";
    private static final String PREF_IS_ALARM_ON = "PREF_ALARM_ON";
    private static final String PREF_USE_GPS = "use_gps";

    public static SharedPreferences mySharedPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setUseGPS(Context context, Boolean use_gps) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(PREF_USE_GPS, use_gps).apply();
    }

    public static Boolean getUseGPS(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_USE_GPS, false);
    }

    public static void setStoredIsAlarmOn(Context context, Boolean isOn) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(PREF_IS_ALARM_ON, isOn).apply();
    }

    public static Boolean getStoredIsAlarmOn(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_IS_ALARM_ON, false);
    }

    public static String getStoredSearchKey(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_SEARCH_KEY, null);
    }

    public static void setStoredSearchKey(Context context, String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(PREF_SEARCH_KEY, key).apply();

    }
    public static String getStoredLastId(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_LAST_ID, null);
    }

    public static void setStoredLastId(Context context, String lastId) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(PREF_LAST_ID, lastId).apply();

    }
}
