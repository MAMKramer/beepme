package com.glanznig.beeper;

import java.util.Calendar;
import java.util.Date;

import com.glanznig.beeper.data.PreferenceHandler;
import com.glanznig.beeper.data.StorageHandler;
import com.glanznig.beeper.helper.GeneralTimerProfile;
import com.glanznig.beeper.helper.HciTimerProfile;
import com.glanznig.beeper.helper.TimerProfile;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class BeeperApp extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String TAG = "beeper";
	
	private StorageHandler dataStore;
	private PreferenceHandler preferences;
	private PendingIntent alarmIntent;
	private long currentUptimeId = 0L;
	private TimerProfile timerProfile;
	
	private static final int ALARM_INTENT_ID = 5332;
	
	public StorageHandler getDataStore() {
		return dataStore;
	}
	
	public PreferenceHandler getPreferences() {
		return preferences;
	}
	
	public boolean isBeeperActive() {
		return getPreferences().isBeeperActive();
	}
	
	public void setBeeperActive(boolean active) {
		getPreferences().setBeeperActive(active);
		if (active) {
			currentUptimeId = getDataStore().startUptime(new Date());
		}
		else if (currentUptimeId != 0L) {
			getDataStore().endUptime(currentUptimeId, new Date());
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		dataStore = new StorageHandler(this.getApplicationContext());
		preferences = new PreferenceHandler(this.getApplicationContext());
		preferences.registerOnPreferenceChangeListener(BeeperApp.this);
		
		setTimerProfile();
	}
	
	public void setTimerProfile() {
		String profile = preferences.getTimerProfile();
		
		if (profile.equals("hci")) {
			timerProfile =  new HciTimerProfile(dataStore);
		}
		
		timerProfile = new GeneralTimerProfile(dataStore);
	}
	
	public void setTimer() {
		if (preferences.isBeeperActive()) {
			Calendar alarmTime = Calendar.getInstance();
	        alarmTime.add(Calendar.SECOND, timerProfile.getTimer());
	        
	        Intent intent = new Intent(this, BeepActivity.class);
	        alarmIntent = PendingIntent.getActivity(this, ALARM_INTENT_ID, intent,
	        		PendingIntent.FLAG_CANCEL_CURRENT);
	        AlarmManager manager = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
	        manager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), alarmIntent);
		}
	}
	
	public void clearTimer() {
		if (alarmIntent != null) {
			alarmIntent.cancel();
			alarmIntent = null;
		}
	}
	
	public void beep() {
		if (preferences.isBeeperActive()) {
			Intent beep = new Intent(BeeperApp.this, BeepActivity.class);
			beep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(beep);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PreferenceHandler.KEY_TIMER_PROFILE)) {
			setTimerProfile();
		}
	}

}

