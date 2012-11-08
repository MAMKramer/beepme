/*
This file is part of BeepMe.

BeepMe is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BeepMe is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BeepMe. If not, see <http://www.gnu.org/licenses/>.

Copyright since 2012 Michael Glanznig
http://beepme.glanznig.com
*/

package com.glanznig.beepme;

import com.glanznig.beepme.data.PreferenceHandler;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	
	private boolean vibrateAtBeep;
	//private boolean warnNoWifi;
	private String timerProfile;
	private boolean testMode;
	
	private static final String TAG = "beeper";
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceHandler prefs = ((BeeperApp)getApplication()).getPreferences();
		vibrateAtBeep = prefs.isVibrateAtBeep();
		//warnNoWifi = prefs.isWarnNoWifi();
		timerProfile = prefs.getTimerProfile();
		testMode = prefs.isTestMode();
        
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
        //CheckBoxPreference boxWarnNoWifi = (CheckBoxPreference)findPreference(PreferenceHandler.KEY_WARN_NO_WIFI);
        //boxWarnNoWifi.setChecked(warnNoWifi);
        ConfirmCheckBoxPreference boxTestMode = (ConfirmCheckBoxPreference)findPreference(PreferenceHandler.KEY_TEST_MODE);
        boxTestMode.setChecked(testMode);
        
        ListPreference formTimerProfile = (ListPreference)findPreference(PreferenceHandler.KEY_TIMER_PROFILE);
        formTimerProfile.setEntries(new String[] { "HCI", getResources().getString(R.string.general_profile) });
        formTimerProfile.setEntryValues(new String[] { "hci", "general" });
	}
}
