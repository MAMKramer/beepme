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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.glanznig.beepme.data.PreferenceHandler;
import com.glanznig.beepme.data.StorageHandler;
import com.glanznig.beepme.helper.GeneralTimerProfile;
import com.glanznig.beepme.helper.HciTimerProfile;
import com.glanznig.beepme.helper.TimerProfile;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class BeeperApp extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private StorageHandler dataStore;
	private PreferenceHandler preferences;
	private PendingIntent alarmIntent;
	private long currentUptimeId = 0L;
	private long scheduledBeepId = 0L;
	private TimerProfile timerProfile;
	
	private static final int ALARM_INTENT_ID = 5332;
	private static final int NOTIFICATION_ID = 1283;
	private static final String TAG = "BeeperApp";
	
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
			createNotification();
		}
		else if (currentUptimeId != 0L) {
			getDataStore().endUptime(currentUptimeId, new Date());
			NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(NOTIFICATION_ID);
		}
	}
	
	private void createNotification() {
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
		
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			notificationBuilder.setSmallIcon(R.drawable.notification_icon);
		}
		else {
			notificationBuilder.setSmallIcon(R.drawable.notification_icon_legacy);
		}
		PackageManager pm = getApplicationContext().getPackageManager();
		try {
			notificationBuilder.setContentTitle(pm.getApplicationLabel(pm.getApplicationInfo(this.getPackageName(), 0)));
		}
		catch (NameNotFoundException ne) {
			notificationBuilder.setContentTitle("Beeper");
		}
		notificationBuilder.setContentText(getString(R.string.beeper_active));
		//set as ongoing, so it cannot be cleared
		notificationBuilder.setOngoing(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainMenu.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainMenu.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setContentIntent(resultPendingIntent);
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		// notification_id allows you to update the notification later on.
		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
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
		else {
			timerProfile = new GeneralTimerProfile(dataStore);
		}
	}
	
	public void setTimer() {
		if (preferences.isBeeperActive()) {
			Calendar alarmTime = Calendar.getInstance();
			Calendar alarmTimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			long timer = timerProfile.getTimer();
	        alarmTime.add(Calendar.SECOND, (int)timer);
	        alarmTimeUTC.add(Calendar.SECOND, (int)timer);
	        scheduledBeepId = getDataStore().addScheduledBeep(alarmTime.getTimeInMillis(), currentUptimeId);
	        
	        Intent intent = new Intent(this, BeepActivity.class);
	        alarmIntent = PendingIntent.getActivity(this, ALARM_INTENT_ID, intent,
	        		PendingIntent.FLAG_CANCEL_CURRENT);
	        AlarmManager manager = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
	        manager.set(AlarmManager.RTC_WAKEUP, alarmTimeUTC.getTimeInMillis(), alarmIntent);
		}
	}
	
	public void clearTimer() {
		if (alarmIntent != null) {
			alarmIntent.cancel();
			cancelCurrentScheduledBeep();
			alarmIntent = null;
		}
	}
	
	public void cancelCurrentScheduledBeep() {
		getDataStore().cancelScheduledBeep(scheduledBeepId);
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
		
		if (key.equals(PreferenceHandler.KEY_TEST_MODE)) {
			//delete data in database
			getDataStore().truncateTables();
			
			//delete pictures
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				File picDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
				File[] allFiles = picDir.listFiles();
				for (int i = 0; i < allFiles.length; i++) {
					allFiles[i].delete();
				}
			}
		}
	}

}

