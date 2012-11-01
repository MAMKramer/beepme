package com.glanznig.beeper;

import com.glanznig.beeper.data.PreferenceHandler;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	
	private boolean vibrateAtBeep;
	private boolean warnNoWifi;
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceHandler prefs = ((BeeperApp)getApplication()).getPreferences();
		vibrateAtBeep = prefs.isVibrateAtBeep();
		warnNoWifi = prefs.isWarnNoWifi();
        
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
	}
}
