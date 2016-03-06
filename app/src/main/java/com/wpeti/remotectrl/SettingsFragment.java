package com.wpeti.remotectrl;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by wpeti on 2016.03.06..
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
