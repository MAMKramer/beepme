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

package com.glanznig.beepme.view;

import java.io.File;
import java.lang.ref.WeakReference;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.PreferenceHandler;
import com.glanznig.beepme.data.StorageHandler;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private boolean vibrateAtBeep;
	//private boolean warnNoWifi;
	private String timerProfile;
	private boolean testMode;
	
	private static final String TAG = "Preferences";
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceHandler prefs = ((BeeperApp)getApplication()).getPreferences();
		prefs.registerOnPreferenceChangeListener(Preferences.this);
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
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PreferenceHandler.KEY_TEST_MODE)) {
			ProgressDialog progress = new ProgressDialog(Preferences.this);
            progress.setMessage(getString(R.string.data_delete_progress));
            progress.setCancelable(false);
            
    		DeleteDataHandler handler = new DeleteDataHandler(progress);
			progress.show();
			new Thread(new DeleteDataRunnable(Preferences.this, handler)).start();
		}
	}
	
	private static class DeleteDataHandler extends Handler {
		WeakReference<ProgressDialog> progress;
		
		DeleteDataHandler(ProgressDialog progress) {
			this.progress = new WeakReference<ProgressDialog>(progress);
		}
		
		@Override
		public void handleMessage(Message message) {
			if (progress.get() != null) {
				progress.get().cancel();
			}
		}
	}
	
	private static class DeleteDataRunnable implements Runnable {
		WeakReference<Preferences> activity;
		WeakReference<DeleteDataHandler> handler;
		
		DeleteDataRunnable(Preferences activity, DeleteDataHandler handler) {
			this.activity = new WeakReference<Preferences>(activity);
			this.handler = new WeakReference<DeleteDataHandler>(handler); 
		}
		@Override
	    public void run() {
			if (activity.get() != null) {
				//delete data in database
				new StorageHandler(activity.get().getApplicationContext()).truncateTables();
				
				//delete pictures
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					File picDir = activity.get().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
					File[] allFiles = picDir.listFiles();
					for (int i = 0; i < allFiles.length; i++) {
						allFiles[i].delete();
					}
				}
				
				if (handler.get() != null) {
					Message msg = new Message();
					handler.get().sendMessage(msg);
				}
			}
	    }
	}
}
