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
import android.content.Context;
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
	
	private boolean deleteDataRunning;
	
	private ProgressDialog progress;
	private DeleteDataHandler handler;
	
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
		
		if (savedState != null) {
			deleteDataRunning = savedState.getBoolean("deleteDataRunning");
		}
		else {
			deleteDataRunning = false;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		populateFields();
		
		handler = (DeleteDataHandler)getLastNonConfigurationInstance();
        if (handler != null) {
        	if (deleteDataRunning) {
        		progress = new ProgressDialog(Preferences.this);
                progress.setMessage(getString(R.string.data_delete_progress));
                progress.setCancelable(false);
                progress.show();
        		handler.updateActivity(Preferences.this);
        	}
        }
        else {
        	deleteDataRunning = false;
        }
	}
	
	@Override
    protected void onPause() {
        if (progress != null) {
            progress.cancel();
            progress = null;
        }
        super.onPause();
    }
	
	public void deleteDataEnded() {
		if (progress != null) {
			progress.cancel();
			progress = null;
		}
		deleteDataRunning = false;
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
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putBoolean("deleteDataRunning", deleteDataRunning);
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
        return handler;
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (!deleteDataRunning && key.equals(PreferenceHandler.KEY_TEST_MODE)) {
			progress = new ProgressDialog(Preferences.this);
            progress.setMessage(getString(R.string.data_delete_progress));
            progress.setCancelable(false);
            
    		handler = new DeleteDataHandler(Preferences.this);
			progress.show();
			deleteDataRunning = true;
			new Thread(new DeleteDataRunnable(Preferences.this, handler)).start();
		}
	}
	
	private static class DeleteDataHandler extends Handler {
		WeakReference<Preferences> activity;
		
		DeleteDataHandler(Preferences activity) {
			updateActivity(activity);
		}
		
		public void updateActivity(Preferences activity) {
			this.activity = new WeakReference<Preferences>(activity);
		}
		
		@Override
		public void handleMessage(Message message) {
			if (activity.get() != null) {
				try {
					activity.get().deleteDataEnded();
				}
				// would rather use isChangingConfigurations() or similar but not available in API Level 8
				catch(Exception e) {}
			}
		}
	}
	
	private static class DeleteDataRunnable implements Runnable {
		WeakReference<Context> ctx;
		DeleteDataHandler handler;
		File picDir;
		
		DeleteDataRunnable(Preferences activity, DeleteDataHandler handler) {
			this.ctx = new WeakReference<Context>(activity.getApplicationContext());
			picDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			this.handler = handler; 
		}
		@Override
	    public void run() {
			if (ctx.get() != null) {
				//delete data in database
				new StorageHandler(ctx.get()).truncateTables();
			}
				
			if (picDir != null) {
				//delete pictures
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					File[] allFiles = picDir.listFiles();
					for (int i = 0; i < allFiles.length; i++) {
						allFiles[i].delete();
					}
				}
				
				if (handler != null) {
					Message msg = new Message();
					handler.sendMessage(msg);
				}
			}
	    }
	}
}
