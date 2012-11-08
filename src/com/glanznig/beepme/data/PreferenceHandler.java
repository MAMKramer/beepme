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
	
	public boolean isVibrateAtBeep() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(KEY_VIBRATE_AT_BEEP, true);
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
