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
http://beepme.yourexp.at
*/

package com.glanznig.beepme.data.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.glanznig.beepme.R;

import java.util.HashMap;

public class PreferenceHandler {
	
	public static final String KEY_BEEPER_STATUS = "beeperStatus";
	public static final String KEY_VIBRATE_AT_BEEP = "vibrateAtBeep";
	public static final String KEY_TEST_MODE = "testMode";
	public static final String KEY_UPTIME_ID = "uptimeId";
	public static final String KEY_SCHEDULED_BEEP_ID = "scheduledBeepId";
    public static final String KEY_PROJECT_ID = "projectId";
	public static final String KEY_EXPORT_RUNNING_SINCE = "exportIsRunningSince";
	public static final String KEY_IS_CALL = "isCall";
	public static final String KEY_PAUSE_BEEPER_DURING_CALL = "pauseBeeperDuringCall";
    public static final String KEY_APP_VERSION = "appVersion";
    public static final String KEY_THUMBNAIL_SIZES = "thumbnailSizes";
    public static final String KEY_BEEP_SOUND_ID = "beepSoundId";

    public enum BeeperStatus {
        ACTIVE, INACTIVE, INACTIVE_AFTER_CALL
    }

    private static HashMap<BeeperStatus, Integer> statusMap;
    private static HashMap<Integer, PreferenceHandler.BeeperStatus> invStatusMap;

    static {
        Integer one = new Integer(1);
        Integer two = new Integer(2);
        Integer zero = new Integer(0);

        PreferenceHandler.BeeperStatus active = PreferenceHandler.BeeperStatus.ACTIVE;
        PreferenceHandler.BeeperStatus inactive = PreferenceHandler.BeeperStatus.INACTIVE;
        PreferenceHandler.BeeperStatus inactive_after_call = PreferenceHandler.BeeperStatus.INACTIVE_AFTER_CALL;

        statusMap = new HashMap<PreferenceHandler.BeeperStatus, Integer>();
        invStatusMap = new HashMap<Integer, PreferenceHandler.BeeperStatus>();
        statusMap.put(active, one);
        statusMap.put(inactive, zero);
        statusMap.put(inactive_after_call, two);
        invStatusMap.put(one, active);
        invStatusMap.put(two, inactive_after_call);
        invStatusMap.put(zero, inactive);
    }
	
	private Context ctx;
	
	public PreferenceHandler(Context ctx) {
		this.ctx = ctx;
	}
	
	public void registerOnPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

    /**
     * Gets whether the beeper is active and hence if beeps can be scheduled. It depends on the
     * project type if this can be true.
     * @return status ACTIVE, INACTIVE, or INACTIVE_AFTER_CALL
     */
	public BeeperStatus getBeeperStatus() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return invStatusMap.get(prefs.getInt(KEY_BEEPER_STATUS, statusMap.get(BeeperStatus.INACTIVE)));
	}

    /**
     * Sets the beeper to ACTIVE, INACTIVE or INACTIVE_AFTER_CALL. Beeps can be scheduled only if the beeper is active.
     * @param status status ACTIVE, INACTIVE, INACTIVE_AFTER_CALL
     */
	public void setBeeperStatus(BeeperStatus status) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(KEY_BEEPER_STATUS, statusMap.get(status));
		editor.commit();
	}

    /**
     * Gets the current uptime uid. It is set to 0L if the beeper is not active.
     * @return current uptime uid, or 0L if not set
     */
	public long getUptimeId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getLong(KEY_UPTIME_ID, 0L);
	}

    /**
     * Sets the current uptime uid.
     * @param uptimeId current uptime uid
     */
	public void setUptimeId(long uptimeId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(KEY_UPTIME_ID, uptimeId);
		editor.commit();
	}

    /**
     * Gets the uid of the current scheduled beep.
     * @return uid of current scheduled beep
     */
	public long getScheduledBeepId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getLong(KEY_SCHEDULED_BEEP_ID, 0L);
	}

    /**
     * Sets the uid of the current scheduled beep.
     * @param beepId uid of current scheduled beep
     */
	public void setScheduledBeepId(long beepId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(KEY_SCHEDULED_BEEP_ID, beepId);
		editor.commit();
	}

    /**
     * Gets the uid of the current project.
     * @return uid of current project
     */
    public long getProjectId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getLong(KEY_PROJECT_ID, 0L);
    }

    /**
     * Sets the uid of the current project.
     * @param projectId uid of current project
     */
    public void setProjectId(long projectId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_PROJECT_ID, projectId);
        editor.commit();
    }

    /**
     * Gets the stored app version, which is necessary for post-upgrade tasks.
     * @return stored app version
     */
	public int getAppVersion() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getInt(KEY_APP_VERSION, 0);
	}

    /**
     * Sets the stored app version (after a upgrade).
     * @param version new app version
     */
	public void setAppVersion(int version) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(KEY_APP_VERSION, version);
		editor.commit();
	}

    /**
     * Gets the thumbnail sizes that are supported by this phone.
     * @return array of thumbnail sizes
     */
    public int[] getThumbnailSizes() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String sizesString =  prefs.getString(KEY_THUMBNAIL_SIZES, "");

        if (sizesString != null && !sizesString.isEmpty()) {
            String[] parts = sizesString.split(",");
            int[] sizes = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                sizes[i] = Integer.valueOf(parts[i]).intValue();
            }

            return sizes;
        }

        int[] empty = {};
        return empty;
    }

    /**
     * Sets the thumbnail sizes that are supported by this phone.
     * @param sizes array of thumbnail sizes
     */
    public void setThumbnailSizes(int[] sizes) {
        String sizesString = "";
        for (int i = 0; i < sizes.length - 1; i++) {
            sizesString += sizes[i] + ",";
        }
        sizesString += sizes[sizes.length - 1];

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_THUMBNAIL_SIZES, sizesString);
        editor.commit();
    }

    /**
     * Gets whether BeepMe is running in test mode.
     * @return true if test mode, false otherwise
     */
	public boolean isTestMode() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(KEY_TEST_MODE, false);
	}

    /**
     * Sets whether BeepMe is running in test mode.
     * @param test true if test mode, false otherwise
     */
	public void setTestMode(boolean test) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_TEST_MODE, test);
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

    public String getBeepSoundId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(KEY_BEEP_SOUND_ID, Integer.valueOf(R.raw.pling).toString());
    }

    public void setBeepSoundId(String beepId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BEEP_SOUND_ID, beepId);
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
