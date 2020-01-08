package com.example.hendrik.mianamalaga.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        SwitchPreferenceCompat useDefaultCloudPreference = findPreference("useDefaultCloud");
        SwitchPreferenceCompat useCustomCloudPreference = findPreference("useCustomCloud");
        EditTextPreference cloudUrlPreference = findPreference("cloudUrl");
        EditTextPreference cloudPasswordPreference = findPreference("cloudPassword");
        EditTextPreference cloudUserNamePreference = findPreference("cloudUsername");


        if ( useDefaultCloudPreference != null ){

            useDefaultCloudPreference.setOnPreferenceClickListener(preference -> {

                if( !useDefaultCloudPreference.isChecked() ){
                    useCustomCloudPreference.setVisible(true);
                } else {
                    useCustomCloudPreference.setVisible(false);
                    cloudUrlPreference.setVisible(false);
                    cloudPasswordPreference.setVisible(false);
                    cloudUserNamePreference.setVisible(false);
                }
                return false;
            });

            if( !useDefaultCloudPreference.isChecked() ){
                useCustomCloudPreference.setVisible(true);
            }

            if( useCustomCloudPreference != null ){
                if( useCustomCloudPreference.isChecked() ){

                    cloudUrlPreference.setVisible(true);
                    cloudPasswordPreference.setVisible(true);
                    cloudUserNamePreference.setVisible(true);

                    String cloudUrl = cloudUrlPreference.getText();
                    String cloudPassword = cloudPasswordPreference.getText();
                    String cloudUserName = cloudUserNamePreference.getText();

                }

                useCustomCloudPreference.setOnPreferenceClickListener(preference -> {
                    if( useCustomCloudPreference.isChecked() ) {
                        cloudUrlPreference.setVisible(true);
                        cloudPasswordPreference.setVisible(true);
                        cloudUserNamePreference.setVisible(true);
                    } else {
                        cloudUrlPreference.setVisible(false);
                        cloudPasswordPreference.setVisible(false);
                        cloudUserNamePreference.setVisible(false);
                    }
                    return false;
                });

                SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);

                if( prefs.getString(Constants.CloudAccesTokenName, null)  == null ){   // TODO only if text has changed in cloudUrl cloudpassword or username
                    // TODO store url password and username in sharedPreference
                }
            }

        }

    }
}
