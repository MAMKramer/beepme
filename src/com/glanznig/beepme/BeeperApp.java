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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.glanznig.beepme.data.PreferenceHandler;
import com.glanznig.beepme.data.ScheduledBeepTable;
import com.glanznig.beepme.data.UptimeTable;
import com.glanznig.beepme.helper.GeneralTimerProfile;
import com.glanznig.beepme.helper.HciTimerProfile;
import com.glanznig.beepme.helper.TimerProfile;
import com.glanznig.beepme.view.BeepActivity;
import com.glanznig.beepme.view.MainMenu;

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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class BeeperApp extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private PreferenceHandler preferences = null;
	private TimerProfile timerProfile;
	
	private static final int ALARM_INTENT_ID = 5332;
	private static final int NOTIFICATION_ID = 1283;
	private static final String TAG = "BeeperApp";
	
	public PreferenceHandler getPreferences() {
		if (preferences == null) {
			preferences = new PreferenceHandler(this.getApplicationContext());
			preferences.registerOnPreferenceChangeListener(BeeperApp.this);
		}
		
		return preferences;
	}
	
	public boolean isBeeperActive() {
		return getPreferences().isBeeperActive();
	}
	
	public void setBeeperActive(boolean active) {
		UptimeTable uptimeTbl = new UptimeTable(this.getApplicationContext());
		getPreferences().setBeeperActive(active);
		if (active) {
			getPreferences().setUptimeId(uptimeTbl.startUptime(new Date()));
			createNotification();
		}
		else {
			long uptimeId = getPreferences().getUptimeId();
			
			if (uptimeId != 0L) {
				uptimeTbl.endUptime(uptimeId, new Date());
				getPreferences().setUptimeId(0L);
				NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				manager.cancel(TAG, NOTIFICATION_ID);
			}
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
		manager.notify(TAG, NOTIFICATION_ID, notificationBuilder.build());
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		getPreferences();
		setTimerProfile();
		
		UptimeTable uptimeTbl = new UptimeTable(this.getApplicationContext());
		
		if (isBeeperActive()) {
			long scheduledBeepId = getPreferences().getScheduledBeepId();
			//is there a scheduled beep, if no, create one, if yes and it is expired, create a new one
			if (scheduledBeepId != 0L) {
				if (new ScheduledBeepTable(this.getApplicationContext()).isExpired(scheduledBeepId)) {
					expireTimer();
					setTimer();
				}
			}
			else {
				setTimer();
			}
			
			//is there a notification, if no, create one
			//cannot check if there is a notification or not, so call create, it will be replaced
			createNotification();
			
			//is there a open uptime interval, if no, create one
			long uptimeId = getPreferences().getUptimeId();
			if (uptimeId == 0L) {
				getPreferences().setUptimeId(uptimeTbl.startUptime(new Date()));
			}
		}
		else {
			long scheduledBeepId = getPreferences().getScheduledBeepId();
			//is there a scheduled beep, if yes, cancel it
			if (scheduledBeepId != 0L) {
				cancelTimer();
			}
			
			//cancel notification
			NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(TAG, NOTIFICATION_ID);
			
			//is there a open uptime interval, if yes, end it
			long uptimeId = getPreferences().getUptimeId();
			
			if (uptimeId != 0L) {
				uptimeTbl.endUptime(uptimeId, new Date());
				getPreferences().setUptimeId(0L);
			}
		}
	}
	
	public void setTimerProfile() {
		String profile = preferences.getTimerProfile();
		
		if (profile.equals("hci")) {
			timerProfile =  new HciTimerProfile(this.getApplicationContext());
		}
		else {
			timerProfile = new GeneralTimerProfile(this.getApplicationContext());
		}
	}
	
	public void setTimer() {
		if (preferences.isBeeperActive()) {
			Calendar alarmTime = Calendar.getInstance();
			Calendar alarmTimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			
			if (timerProfile == null) {
				setTimerProfile();
			}
			
			long timer = timerProfile.getTimer();
	        alarmTime.add(Calendar.SECOND, (int)timer);
	        //Log.i(TAG, "alarm in " + timer + " seconds.");
	        alarmTimeUTC.add(Calendar.SECOND, (int)timer);
	        getPreferences().setScheduledBeepId(new ScheduledBeepTable(
	        		this.getApplicationContext()).addScheduledBeep(alarmTime.getTimeInMillis(),
	        		getPreferences().getUptimeId()));
	        
	        Intent intent = new Intent(this, BeepActivity.class);
	        PendingIntent alarmIntent = PendingIntent.getActivity(this, ALARM_INTENT_ID, intent,
	        		PendingIntent.FLAG_CANCEL_CURRENT);
	        AlarmManager manager = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
	        manager.set(AlarmManager.RTC_WAKEUP, alarmTimeUTC.getTimeInMillis(), alarmIntent);
		}
	}
	
	public void expireTimer() {
		clearTimer(2);
	}
	
	public void cancelTimer() {
		clearTimer(1);
	}
	
	public void clearTimer(int status) {
		Intent intent = new Intent(this, BeepActivity.class);
        PendingIntent alarmIntent = PendingIntent.getActivity(this, ALARM_INTENT_ID, intent,
        		PendingIntent.FLAG_CANCEL_CURRENT);
		alarmIntent.cancel();
		new ScheduledBeepTable(this.getApplicationContext()).updateStatus(getPreferences().getScheduledBeepId(), status);
		getPreferences().setScheduledBeepId(0L);
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

