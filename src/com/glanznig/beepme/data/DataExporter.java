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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.glanznig.beepme.R;
import com.glanznig.beepme.view.MainMenu;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class DataExporter {
	
	private static final String EXPORT_PREFIX = "beepme_data_";
	private static final String TAG = "DataExporter";
	private static final int BUFFER = 2048;
	private static final int NOTIFICATION_ID = 1438;
	Context ctx;
	
	public DataExporter(Context context) {
		ctx = context;
	}
	
	public String exportToZipFile() {
		//external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			createNotification();
			
			File exportDir = ctx.getExternalFilesDir("export");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			else {
				//delete existing export files
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File directory, String fileName) {
					    return fileName.endsWith(".zip");
					}
				};
				File[] exports = exportDir.listFiles(filter);
				for (int i = 0; i < exports.length; i++) {
					exports[i].delete();
				}
			}
			String exportFilename = EXPORT_PREFIX + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + ".zip";
			File exportFile = new File(exportDir, exportFilename);
			
			ArrayList<File> fileList = new ArrayList<File>();
			File picDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			if (picDir.exists()) {
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File directory, String fileName) {
					    return fileName.endsWith(".jpg");
					}
				};
				
				File[] picFiles = picDir.listFiles(filter);
				for (int i = 0; i < picFiles.length; i++) {
					fileList.add(picFiles[i]);
				}
			}
			fileList.add(ctx.getDatabasePath(StorageHandler.getDatabaseName()));
			
			return zipFiles(exportFile, fileList);
		}
		
		return null;
	}
	
	private String zipFiles(File zipFile, List<File> fileList) {
		String path = null;
		
		if (zipFile != null && fileList != null) {
			try {
				BufferedInputStream bufIn = null;
				FileOutputStream zipFOutStream = new FileOutputStream(zipFile);
				ZipOutputStream outStream = new ZipOutputStream(new BufferedOutputStream(zipFOutStream));
				
				byte data[] = new byte[BUFFER];
				Iterator<File> i = fileList.iterator();
				
				while (i.hasNext()) {
					File f = i.next();
					FileInputStream fIn = new FileInputStream(f);
					bufIn = new BufferedInputStream(fIn, BUFFER);
					ZipEntry entry = new ZipEntry(f.getName());
					outStream.putNextEntry(entry);
					int count;
					while ((count = bufIn.read(data, 0, BUFFER)) != -1) {
						outStream.write(data, 0, count);
					}
					bufIn.close();
				}
				outStream.close();
				
				path = zipFile.getAbsolutePath();
				
			} catch(Exception e) {
				Log.e(TAG, "error while zipping.", e);
			}
		}
		
		NotificationManager manager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(TAG, NOTIFICATION_ID);
		
		return path;
	}
	
	private void createNotification() {
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx.getApplicationContext());
		
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			notificationBuilder.setSmallIcon(R.drawable.notification_icon);
		}
		else {
			notificationBuilder.setSmallIcon(R.drawable.notification_icon_legacy);
		}
		PackageManager pm = ctx.getApplicationContext().getPackageManager();
		try {
			notificationBuilder.setContentTitle(pm.getApplicationLabel(pm.getApplicationInfo(ctx.getApplicationContext().getPackageName(), 0)));
		}
		catch (NameNotFoundException ne) {
			notificationBuilder.setContentTitle("Beeper");
		}
		notificationBuilder.setContentText(ctx.getString(R.string.export_active));
		//set as ongoing, so it cannot be cleared
		notificationBuilder.setOngoing(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(ctx.getApplicationContext(), MainMenu.class);
		
		//add progress bar (indeterminate)
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			notificationBuilder.setProgress(0, 0, true);
		}

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx.getApplicationContext());
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainMenu.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setContentIntent(resultPendingIntent);
		NotificationManager manager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		// notification_id allows you to update the notification later on.
		manager.notify(TAG, NOTIFICATION_ID, notificationBuilder.build());
	}

}
