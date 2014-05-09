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

package com.glanznig.beepme;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.glanznig.beepme.data.Beep;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.Project;
import com.glanznig.beepme.data.Timer;
import com.glanznig.beepme.data.Uptime;
import com.glanznig.beepme.data.db.ProjectTable;
import com.glanznig.beepme.data.util.PreferenceHandler;
import com.glanznig.beepme.data.db.BeepTable;
import com.glanznig.beepme.data.db.MomentTable;
import com.glanznig.beepme.data.db.StorageHandler;
import com.glanznig.beepme.data.db.UptimeTable;
import com.glanznig.beepme.helper.PhotoUtils;
import com.glanznig.beepme.view.BeepActivity;
import com.glanznig.beepme.view.ExportActivity;
import com.glanznig.beepme.view.MainActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

public class BeepMeApp extends Application { //implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private PreferenceHandler preferences = null;
	private BeepMeApp.CallStateListener callStateListener;
	
	private static final int ALARM_INTENT_ID = 5332;
	private static final int NOTIFICATION_ID = 1283; // unique id to identify the beeper notificatoin
	private static final String TAG = "BeepMeApp";

    /**
     * Gets a reference to the preference handling class that provides setter and getter methods.
     * @return reference to preference handler
     */
	public PreferenceHandler getPreferences() {
		if (preferences == null) {
			preferences = new PreferenceHandler(this.getApplicationContext());
			//preferences.registerOnPreferenceChangeListener(BeeperApp.this);
		}
		
		return preferences;
	}

    /**
     * Gets the current project.
     * @return current project, or null if none
     */
    public Project getCurrentProject() {
        long projectId = getPreferences().getProjectId();
        if (projectId != 0L) {
            return new ProjectTable(this.getApplicationContext()).getProject(projectId);
        }

        return null;
    }

    /**
     * Gets the current uptime.
     * @return current uptime, or null if none
     */
    public Uptime getCurrentUptime() {
        long uptimeId = getPreferences().getUptimeId();
        if (uptimeId != 0L) {
            return new UptimeTable(this.getApplicationContext()).getUptime(uptimeId);
        }

        return null;
    }

    /**
     * Starts a new uptime (creates a new object, sets start time and sets uptime uid in preferences)
     */
    public void startUptime() {
        if (getCurrentUptime() == null) {
            Uptime uptime = new Uptime();
            uptime.setStart(Calendar.getInstance().getTime());
            uptime = new UptimeTable(this.getApplicationContext()).addUptime(uptime);
            getPreferences().setUptimeId(uptime.getUid());
        }
    }

    /**
     * Ends the current uptime (retrieves current uptime object, sets end time and
     * clears uptime uid in preferences)
     */
    public void endUptime() {
        Uptime uptime = getCurrentUptime();
        if (uptime != null) {
            uptime.setEnd(Calendar.getInstance().getTime());
            new UptimeTable(this.getApplicationContext()).updateUptime(uptime);
            getPreferences().setUptimeId(0L);
        }
    }

    /**
     * Gets whether the beeper is active and hence new beeps can be scheduled.
     * @return true if beeper status is ACTIVE, and false if status is INACTIVE or INACTIVE_AFTER_CALL
     */
    public boolean isBeeperActive() {
        PreferenceHandler.BeeperStatus active = getPreferences().getBeeperStatus();
        if (active == PreferenceHandler.BeeperStatus.ACTIVE) {
            return true;
        }

        return false;
    }

    /**
     * Sets the beeper status to either ACTIVE, INACTIVE, or INACTIVE_AFTER_CALL, starts or
     * ends the corresponding uptime, creates or removes the corresponding notification and schedules
     * or cancels the current beep.
     * @param status the beeper status
     */
	public void setBeeperStatus(PreferenceHandler.BeeperStatus status) {
		getPreferences().setBeeperStatus(status);
		
		if (status == PreferenceHandler.BeeperStatus.ACTIVE) {
            startUptime();
			createBeeperNotification();
            scheduleBeep();
		}
		else {
            if (getCurrentUptime() != null) {
                removeBeeperNotification();
            }
            updateBeep(Beep.BeepStatus.CANCELLED);
            endUptime();
		}
	}

    /**
     * Creates a entry in the Android notification drawer to notify the user that the beeper is active.
     */
	private void createBeeperNotification() {
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
		Intent resultIntent = new Intent(this, MainActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setContentIntent(resultPendingIntent);

		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(TAG, NOTIFICATION_ID, notificationBuilder.build());
	}

    /**
     * Removes the entry in the Android notification drawer (if present).
     */
    private void removeBeeperNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(TAG, NOTIFICATION_ID);
    }
	
	@Override
	public void onCreate() {
		super.onCreate();
		getPreferences();

        onAppUpdate(getPreferences().getAppVersion());

        // save thumbnail sizes
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int screenDpWidth = (int)(metrics.widthPixels / metrics.density + 0.5f);
        int[] sizes = {48, 64, screenDpWidth};
        getPreferences().setThumbnailSizes(sizes);
		
		// listen to call events
		if (callStateListener == null) {
			callStateListener = new CallStateListener(BeepMeApp.this);
		}
		TelephonyManager telManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		//set export running to false
		getPreferences().setExportRunningSince(0L);
		
		if (isBeeperActive()) {
			long scheduledBeepId = getPreferences().getScheduledBeepId();
			//is there a scheduled beep, if no, create one, if yes and it is expired, create a new one
			if (scheduledBeepId != 0L) {
				BeepTable beepTable = new BeepTable(this.getApplicationContext());
                Beep beep = beepTable.getBeep(scheduledBeepId);
				if (beep.getStatus() != Beep.BeepStatus.RECEIVED && beep.isOverdue()) {
					updateBeep(Beep.BeepStatus.EXPIRED);
					scheduleBeep();
				}
			}
			else {
				scheduleBeep();
			}
			
			//is there a notification, if no, create one
			//cannot check if there is a notification or not, so call create, it will be replaced
			createBeeperNotification();
			
			//is there a open uptime interval, if no, create one
			startUptime();
		}
		else {
			long scheduledBeepId = getPreferences().getScheduledBeepId();
			//is there a scheduled beep, if yes, cancel it
			if (scheduledBeepId != 0L) {
				updateBeep(Beep.BeepStatus.CANCELLED);
			}
			
			//cancel notifications
            removeBeeperNotification();
			NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(TAG, ExportActivity.EXPORT_RUNNING_NOTIFICATION);
			
			//is there a open uptime interval, if yes, end it
            endUptime();
		}
	}

    /**
     * Gets the current scheduled beep object.
     * @return current scheduled beep object, or null if not set
     */
    public Beep getCurrentScheduledBeep() {
        if (getPreferences().getScheduledBeepId() != 0L) {
            return new BeepTable(this.getApplicationContext()).getBeep(getPreferences().getScheduledBeepId());
        }
        return null;
    }

    /**
     * Schedules the next beep according to timer strategy. Uses the timer class referenced by the
     * current project to get the next timestamp.
     */
	public void scheduleBeep() {
		if (isBeeperActive()) {
			Calendar alarmTime = Calendar.getInstance();
			Calendar alarmTimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            Project current = getCurrentProject();
            Timer timer = current.getTimer();
            long nextBeep = 0;
            do {
                nextBeep = timer.getNext();
            }
            while (nextBeep <= 0L); // make sure that timer class delivers only positive offsets

	        alarmTime.add(Calendar.MILLISECOND, (int)nextBeep);
	        alarmTimeUTC.add(Calendar.MILLISECOND, (int)nextBeep);

            Beep beep = new Beep();
            beep.setCreated(Calendar.getInstance().getTime());
            beep.setStatus(Beep.BeepStatus.ACTIVE);
            beep.setUptimeUid(getCurrentUptime().getUid());
            beep.setTimestamp(alarmTime.getTime());
            beep = new BeepTable(this.getApplicationContext()).addBeep(beep);
            getPreferences().setScheduledBeepId(beep.getUid());
	        
	        Intent intent = new Intent(this, BeepActivity.class);
	        PendingIntent alarmIntent = PendingIntent.getActivity(this, ALARM_INTENT_ID, intent,
	        		PendingIntent.FLAG_CANCEL_CURRENT);
	        AlarmManager manager = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
	        manager.set(AlarmManager.RTC_WAKEUP, alarmTimeUTC.getTimeInMillis(), alarmIntent);
		}
	}

    /**
     * Updates the beep object with a new status and update timestamp.
     * @param status one of ACTIVE, RECEIVED, CANCELLED or EXPIRED
     */
	public void updateBeep(Beep.BeepStatus status) {
        Date time = Calendar.getInstance().getTime();

		if (status != Beep.BeepStatus.ACTIVE || status != Beep.BeepStatus.RECEIVED) {
			Intent intent = new Intent(this, BeepActivity.class);
	        PendingIntent alarmIntent = PendingIntent.getActivity(this, ALARM_INTENT_ID, intent,
	        		PendingIntent.FLAG_CANCEL_CURRENT);
			alarmIntent.cancel();
		}

        Beep beep = new Beep(getPreferences().getScheduledBeepId());
        beep.setStatus(status);
        beep.setUpdated(time);

        if (status == Beep.BeepStatus.RECEIVED) {
            beep.setReceived(time);
        }

        new BeepTable(this.getApplicationContext()).updateBeep(beep);

		if (status != Beep.BeepStatus.ACTIVE) {
			getPreferences().setScheduledBeepId(0L);
		}
	}

    /**
     * Start a test beep.
     */
	public void beep() {
		if (isBeeperActive()) {
			Intent beep = new Intent(BeepMeApp.this, BeepActivity.class);
			beep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(beep);
		}
	}

    private void onAppUpdate(int oldVersion) {
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            int newVersion = packageInfo.versionCode;

            //if (oldVersion > 0 && oldVersion != newVersion) {
            // if should be above line, change in future versions
            if (oldVersion < newVersion) {
                for (int mVers = oldVersion; mVers < newVersion; mVers++) {
                    switch (mVers) {
                        case 16:
                            /*String dbName;
                            String picDirName;
                            if (getPreferences().isTestMode()) {
                                dbName = StorageHandler.getTestModeDatabaseName();
                                picDirName = PhotoUtils.TEST_MODE_DIR;
                            }
                            else {
                                dbName = StorageHandler.getProductionDatabaseName();
                                picDirName = PhotoUtils.NORMAL_MODE_DIR;
                            }

                            // rename the database
                            if (!dbName.equals(StorageHandler.DB_OLD_NAME)) {
                                File oldDb = getDatabasePath(StorageHandler.DB_OLD_NAME);
                                File newDb = new File(oldDb.getParentFile(), dbName);
                                oldDb.renameTo(newDb);
                            }

                            // move pictures
                            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                                File oldDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                                File newDir = new File(oldDir.getAbsolutePath(), picDirName);

                                if (!newDir.exists()) {
                                    newDir.mkdirs();
                                }

                                File[] picFiles = oldDir.listFiles(new FilenameFilter() {
                                    public boolean accept(File dir, String name) {
                                        return name.toLowerCase().endsWith(".jpg");
                                    }
                                });

                                for (int i = 0; i < picFiles.length; i++) {
                                    picFiles[i].renameTo(new File(newDir, picFiles[i].getName()));
                                }

                                MomentTable st = new MomentTable(this);
                                List<Moment> list = st.getMoments();
                                for (int i = 0; i < list.size(); i++) {
                                    Moment s = list.get(i);
                                    String uri = s.getPhotoUri();

                                    if (uri != null) {
                                        File pic = new File(uri);
                                        s.setPhotoUri(pic.getParent() + File.separator + picDirName + File.separator + pic.getName());
                                        st.editSample(s);
                                    }
                                }
                            }*/

                            break;
                    }
                }
            } // else we would have a new install or the data had been deleted

            getPreferences().setAppVersion(newVersion);
        }
        catch(PackageManager.NameNotFoundException nnfe) {}
    }
	
	private static class CallStateListener extends PhoneStateListener {
		
		private WeakReference<BeepMeApp> appRef;
		
		public CallStateListener(BeepMeApp app) {
			appRef = new WeakReference<BeepMeApp>(app);
		}
		
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (appRef != null && appRef.get() != null) {
				BeepMeApp app = appRef.get();
				switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        app.getPreferences().setCall(false);
                        // beeper was paused by call, reactivate it
                        if (app.getPreferences().getBeeperStatus() == PreferenceHandler.BeeperStatus.INACTIVE_AFTER_CALL) {
                            app.setBeeperStatus(PreferenceHandler.BeeperStatus.ACTIVE);
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

