package com.bx5a.minstrel.widget;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.bx5a.minstrel.R;

/**
 * Created by guillaume on 01/06/2016.
 */
public class GeneralPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.general_preferences);

    }
}
