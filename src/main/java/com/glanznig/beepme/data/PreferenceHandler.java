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

package com.glanznig.beepme.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHandler {
	
	public static final String KEY_BEEPER_ACTIVE = "beeperActivated";
	public static final String KEY_VIBRATE_AT_BEEP = "vibrateAtBeep";
	public static final String KEY_TIMER_PROFILE_ID = "timerProfileId";
	public static final String KEY_TEST_MODE = "testMode";
	public static final String KEY_UPTIME_ID = "uptimeId";
	public static final String KEY_SCHEDULED_BEEP_ID = "scheduledBeepId";
	public static final String KEY_EXPORT_RUNNING_SINCE = "exportIsRunningSince";
	public static final String KEY_IS_CALL = "isCall";
	public static final String KEY_PAUSE_BEEPER_DURING_CALL = "pauseBeeperDuringCall";
    public static final String KEY_APP_VERSION = "appVersion";
	
	private Context ctx;
	
	public PreferenceHandler(Context ctx) {
		this.ctx = ctx;
	}
	
	public void registerOnPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}
	
	public int getBeeperActive() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getInt(KEY_BEEPER_ACTIVE, 0);
	}
	
	public void setBeeperActive(int active) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(KEY_BEEPER_ACTIVE, active);
		editor.commit();
	}
	
	public long getUptimeId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getLong(KEY_UPTIME_ID, 0L);
	}
	
	public void setUptimeId(long uptimeId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(KEY_UPTIME_ID, uptimeId);
		editor.commit();
	}
	
	public long getScheduledBeepId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getLong(KEY_SCHEDULED_BEEP_ID, 0L);
	}
	
	public void setScheduledBeepId(long beepId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(KEY_SCHEDULED_BEEP_ID, beepId);
		editor.commit();
	}
	
	public long exportRunningSince() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getLong(KEY_EXPORT_RUNNING_SINCE, 0L);
	}
	
	public void setExportRunningSince(long running) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(KEY_EXPORT_RUNNING_SINCE, running);
		editor.commit();
	}
	
	public boolean isVibrateAtBeep() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(KEY_VIBRATE_AT_BEEP, false);
	}
	
	public void setVibateAtBeep(boolean vibrate) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_VIBRATE_AT_BEEP, vibrate);
		editor.commit();
	}
	
	public int getAppVersion() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getInt(KEY_APP_VERSION, 0);
	}

	public void setAppVersion(int version) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(KEY_APP_VERSION, version);
		editor.commit();
	}
	
	public boolean isTestMode() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(KEY_TEST_MODE, false);
	}
	
	public void setTestMode(boolean test) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_TEST_MODE, test);
		editor.commit();
	}
	
	public long getTimerProfileId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Long.valueOf(prefs.getString(KEY_TIMER_PROFILE_ID, "1"));
	}
	
	public void setTimerProfileId(long profileId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(KEY_TIMER_PROFILE_ID, String.valueOf(profileId));
		editor.commit();
	}
	
	public boolean isCall() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(KEY_IS_CALL, false);
	}
	
	public void setCall(boolean call) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_IS_CALL, call);
		editor.commit();
	}
	
	public boolean getPauseBeeperDuringCall() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(KEY_PAUSE_BEEPER_DURING_CALL, true);
	}
	
	public void setPauseBeeperDuringCall(boolean pause) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_PAUSE_BEEPER_DURING_CALL, pause);
		editor.commit();
	}
}
