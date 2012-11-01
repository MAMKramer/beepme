package com.glanznig.beeper;

import com.glanznig.beeper.data.PreferenceHandler;
import com.glanznig.beeper.data.StorageHandler;

import android.app.Application;
import android.content.Intent;

public class BeeperApp extends Application {
	
	private StorageHandler dataStore;
	private PreferenceHandler preferences;
	
	public StorageHandler getDataStore() {
		return dataStore;
	}
	
	public PreferenceHandler getPreferences() {
		return preferences;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		dataStore = new StorageHandler(this.getApplicationContext());
		preferences = new PreferenceHandler(this.getApplicationContext());
	}
	
	public void beep() {
		if (preferences.isBeeperActive()) {
			Intent beep = new Intent(BeeperApp.this, BeepActivity.class);
			beep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(beep);
		}
	}

}

