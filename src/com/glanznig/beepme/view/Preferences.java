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

Copyright 2012-2014 Michael Glanznig
http://beepme.glanznig.com
*/

package com.glanznig.beepme.view;

import java.util.ArrayList;
import java.util.Iterator;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.PreferenceHandler;
import com.glanznig.beepme.data.TimerProfile;
import com.glanznig.beepme.db.TimerProfileTable;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String TAG = "Preferences";
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
        
        getFragmentManager().beginTransaction().replace(android.R.id.content, new BasePreferencesFragment()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
		PreferenceHandler prefs = ((BeeperApp)getApplication()).getPreferences();
		prefs.registerOnPreferenceChangeListener(Preferences.this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PreferenceHandler.KEY_TEST_MODE)) {
			BeeperApp app = (BeeperApp)getApplication();
			if (app.isBeeperActive()) {
				app.setBeeperActive(BeeperApp.BEEPER_INACTIVE);
			}
		}
	}
	
	public static class BasePreferencesFragment extends PreferenceFragment {
		
		@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.preferences);
	    }
	    
	    @Override
		public void onResume() {
			super.onResume();
			populateFields();
		}
		
		private void populateFields() {
	        /*ListPreference formTimerProfile = (ListPreference)findPreference(PreferenceHandler.KEY_TIMER_PROFILE_ID);
	        Iterator<TimerProfile> profileList = new TimerProfileTable(getActivity().getApplicationContext()).getTimerProfiles().iterator();
	        ArrayList<String> profileValues = new ArrayList<String>();
	        ArrayList<String> profileNames = new ArrayList<String>();
	        while (profileList.hasNext()) {
	        	TimerProfile tp = profileList.next();
	        	profileNames.add(tp.getName());
	        	profileValues.add(String.valueOf(tp.getId()));
	        }
	        
	        formTimerProfile.setEntries(profileNames.toArray(new String[profileNames.size()]));
	        formTimerProfile.setEntryValues(profileValues.toArray(new String[profileValues.size()]));*/
		}
	}
}
