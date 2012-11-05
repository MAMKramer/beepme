package com.glanznig.beeper;

import com.glanznig.beeper.data.PreferenceHandler;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	
	private boolean vibrateAtBeep;
	private boolean warnNoWifi;
	private String timerProfile;
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceHandler prefs = ((BeeperApp)getApplication()).getPreferences();
		vibrateAtBeep = prefs.isVibrateAtBeep();
		warnNoWifi = prefs.isWarnNoWifi();
		timerProfile = prefs.getTimerProfile();
        
        populateFields();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		populateFields();
	}
	
	private void populateFields() {
		CheckBoxPreference boxVibrateAtBeep = (CheckBoxPreference)findPreference(PreferenceHandler.KEY_VIBRATE_AT_BEEP);
        boxVibrateAtBeep.setChecked(vibrateAtBeep);
        CheckBoxPreference boxWarnNoWifi = (CheckBoxPreference)findPreference(PreferenceHandler.KEY_WARN_NO_WIFI);
        boxWarnNoWifi.setChecked(warnNoWifi);
        ListPreference formTimerProfile = (ListPreference)findPreference(PreferenceHandler.KEY_TIMER_PROFILE);
        formTimerProfile.setEntries(new String[] { "HCI", getResources().getString(R.string.general_profile) });
        formTimerProfile.setEntryValues(new String[] { "hci", "general" });
	}
}
