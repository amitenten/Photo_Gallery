package com.augmentis.ayp.photogallery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

/**
 * Created by Amita on 9/5/2016.
 */
public class PhotoGallerySetting extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "PhotoGallerySetting";

    public static PhotoGallerySetting newInstance() {

        Bundle args = new Bundle();

        PhotoGallerySetting fragment = new PhotoGallerySetting();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.photo_setting_pref);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        onSharedPreferenceChanged(sharedPreferences, "use_gps");
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        Boolean useGPS = PhotoGalleryPreferance.getUseGPS(getActivity());
        Log.d(TAG, "Use GPS" + useGPS);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "On shared preference changed");

        Preference p = findPreference(key);

        if (p instanceof SwitchPreferenceCompat) {
            boolean value = sharedPreferences.getBoolean(key,false);
            p.setSummary( value ? "ON" : "OFF");
        } else {
            p.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}
