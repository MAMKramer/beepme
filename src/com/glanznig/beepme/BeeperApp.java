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

package com.glanznig.beepme;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;

import com.glanznig.beepme.data.PreferenceHandler;
import com.glanznig.beepme.data.ScheduledBeepTable;
import com.glanznig.beepme.data.TimerProfile;
import com.glanznig.beepme.data.TimerProfileTable;
import com.glanznig.beepme.data.UptimeTable;
import com.glanznig.beepme.view.BeepActivity;
import com.glanznig.beepme.view.MainActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class BeeperApp extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private PreferenceHandler preferences = null;
	private TimerProfile timerProfile;
	private BeeperApp.CallStateListener callStateListener;
	
	private static final int ALARM_INTENT_ID = 5332;
	private static final int NOTIFICATION_ID = 1283;
	private static final String TAG = "BeeperApp";
	
	public static final int BEEPER_INACTIVE = 0;
	public static final int BEEPER_ACTIVE = 1;
	public static final int BEEPER_INACTIVE_AFTER_CALL = 2;
	
	public PreferenceHandler getPreferences() {
		if (preferences == null) {
			preferences = new PreferenceHandler(this.getApplicationContext());
			preferences.registerOnPreferenceChangeListener(BeeperApp.this);
		}
		
		return preferences;
	}
	
	public boolean isBeeperActive() {
		int active = getPreferences().getBeeperActive();
		if (active == BEEPER_ACTIVE) {
			return true;
		}
		
		return false;
	}
	
	public void setBeeperActive(int active) {
		UptimeTable uptimeTbl = new UptimeTable(this.getApplicationContext(), timerProfile);
		getPreferences().setBeeperActive(active);
		
		if (active == BEEPER_ACTIVE) {
			getPreferences().setUptimeId(uptimeTbl.startUptime(Calendar.getInstance().getTime()));
			createNotification();
		}
		else {
			long uptimeId = getPreferences().getUptimeId();
			
			if (uptimeId != 0L) {
				uptimeTbl.endUptime(uptimeId, Calendar.getInstance().getTime());
				getPreferences().setUptimeId(0L);
				NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				manager.cancel(TAG, NOTIFICATION_ID);
			}
		}
	}
	
	private void createNotification() {
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
		
		if (this.getPreferences().isTestMode()) {
			notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify_testmode);
			notificationBuilder.setContentTitle(getString(R.string.notify_title_testmode));
		}
		else {
			notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify);
			notificationBuilder.setContentTitle(getString(R.string.notify_title));	
		}
		notificationBuilder.setContentText(getString(R.string.notify_content));
		//set as ongoing, so it cannot be cleared
		notificationBuilder.setOngoing(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
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
		
		// listen to call events
		if (callStateListener == null) {
			callStateListener = new CallStateListener(BeeperApp.this);
		}
		TelephonyManager telManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		//set export running to false
		getPreferences().setExportRunningSince(0L);
		
		UptimeTable uptimeTbl = new UptimeTable(this.getApplicationContext(), timerProfile);
		
		if (isBeeperActive()) {
			long scheduledBeepId = getPreferences().getScheduledBeepId();
			//is there a scheduled beep, if no, create one, if yes and it is expired, create a new one
			if (scheduledBeepId != 0L) {
				ScheduledBeepTable sbt = new ScheduledBeepTable(this.getApplicationContext());
				if (sbt.getStatus(scheduledBeepId) != 3 && sbt.isExpired(scheduledBeepId)) {
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
				getPreferences().setUptimeId(uptimeTbl.startUptime(Calendar.getInstance().getTime()));
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
				uptimeTbl.endUptime(uptimeId, Calendar.getInstance().getTime());
				getPreferences().setUptimeId(0L);
			}
		}
	}
	
	public void setTimerProfile() {
		long profileId = preferences.getTimerProfileId();
		timerProfile = new TimerProfileTable(this.getApplicationContext()).getTimerProfile(profileId);
	}
	
	public TimerProfile getTimerProfile() {
		return timerProfile;
	}
	
	public void setTimer() {
		if (isBeeperActive()) {
			Calendar alarmTime = Calendar.getInstance();
			Calendar alarmTimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			
			if (timerProfile == null) {
				setTimerProfile();
			}
			
			long timer = timerProfile.getTimer(this.getApplicationContext());
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
	
	public void declineTimer() {
		updateTimer(4);
	}
	
	public void acceptTimer() {
		updateTimer(3);
	}
	
	public void expireTimer() {
		updateTimer(2);
	}
	
	public void cancelTimer() {
		updateTimer(1);
	}
	
	public void updateTimer(int status) {
		if (status != 3 || status != 4) {
			Intent intent = new Intent(this, BeepActivity.class);
	        PendingIntent alarmIntent = PendingIntent.getActivity(this, ALARM_INTENT_ID, intent,
	        		PendingIntent.FLAG_CANCEL_CURRENT);
			alarmIntent.cancel();
		}
		new ScheduledBeepTable(this.getApplicationContext()).updateStatus(getPreferences().getScheduledBeepId(), status);
		if (status != 3) {
			getPreferences().setScheduledBeepId(0L);
		}
	}
	
	public void beep() {
		if (isBeeperActive()) {
			Intent beep = new Intent(BeeperApp.this, BeepActivity.class);
			beep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(beep);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PreferenceHandler.KEY_TIMER_PROFILE_ID)) {
			if (isBeeperActive()) {
				setBeeperActive(BEEPER_INACTIVE);
				setTimerProfile();
				setBeeperActive(BEEPER_ACTIVE);
			}
			else {
				setTimerProfile();
			}
		}
	}
	
	private static class CallStateListener extends PhoneStateListener {
		
		private WeakReference<BeeperApp> appRef;
		
		public CallStateListener(BeeperApp app) {
			appRef = new WeakReference<BeeperApp>(app);
		}
		
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (appRef != null && appRef.get() != null) {
				BeeperApp app = appRef.get();
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					app.getPreferences().setCall(false);
					// beeper was paused by call, reactivate it
					if (app.getPreferences().getBeeperActive() == BeeperApp.BEEPER_INACTIVE_AFTER_CALL) {
						app.setBeeperActive(BeeperApp.BEEPER_ACTIVE);
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					app.getPreferences().setCall(true);
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					app.getPreferences().setCall(true);
					break;
				}
			}
		}
	}
}

