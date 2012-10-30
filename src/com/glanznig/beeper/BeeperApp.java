package com.glanznig.beeper;

import com.glanznig.beeper.data.StorageHandler;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

public class BeeperApp extends Application {
	
	private static final String PREFS_NAME = "beeperPrefs";
	
	private StorageHandler store;
	private boolean beeperActive;
	private boolean accompanyBeepWithVibrate;
	
	public StorageHandler getDataStore() {
		return store;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		store = new StorageHandler(this.getApplicationContext());
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		beeperActive = prefs.getBoolean("beeperActive", false);
		accompanyBeepWithVibrate = prefs.getBoolean("accompanyBeepWithVibrate", true);
	}
	
	public boolean isBeeperActive() {
		return beeperActive;
	}
	
	public void setBeeperActive(boolean state) {
		beeperActive = state;
		
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("beeperActive", beeperActive);
		editor.commit();
	}
	
	public boolean isAccompanyBeepWithVibrate() {
		return accompanyBeepWithVibrate;
	}
	
	public void beep() {
		if (beeperActive) {
			Intent beep = new Intent(BeeperApp.this, BeepActivity.class);
			beep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(beep);
		}
	}

}

