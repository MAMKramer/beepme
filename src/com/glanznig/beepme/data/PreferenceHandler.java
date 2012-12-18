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

package com.glanznig.beepme.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHandler {
	
	public static final String KEY_BEEPER_ACTIVE = "beeperActive";
	public static final String KEY_VIBRATE_AT_BEEP = "vibrateAtBeep";
	public static final String KEY_WARN_NO_WIFI = "warnNoWifi";
	public static final String KEY_TIMER_PROFILE = "timerProfile";
	public static final String KEY_TEST_MODE = "testMode";
	public static final String KEY_UPTIME_ID = "uptimeId";
	public static final String KEY_SCHEDULED_BEEP_ID = "scheduledBeepId";
	public static final String KEY_EXPORT_RUNNING_SINCE = "exportIsRunningSince";
	
	private Context ctx;
	
	public PreferenceHandler(Context ctx) {
		this.ctx = ctx;
	}
	
	public void registerOnPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}
	
	public boolean isBeeperActive() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(KEY_BEEPER_ACTIVE, false);
	}
	
	public void setBeeperActive(boolean active) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_BEEPER_ACTIVE, active);
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
	
	public boolean isWarnNoWifi() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(KEY_WARN_NO_WIFI, true);
	}
	
	public void setWarnNoWifi(boolean warn) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_WARN_NO_WIFI, warn);
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
	
	public String getTimerProfile() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getString(KEY_TIMER_PROFILE, "general");
	}
	
	public void setTimerProfile(String profile) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(KEY_TIMER_PROFILE, profile);
		editor.commit();
	}
}
