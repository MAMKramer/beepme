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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.db.StorageHandler;
import com.glanznig.beepme.helper.PhotoUtils;
import com.glanznig.beepme.view.MainActivity;

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
    private static final String EXPORT_DIR = "export";
	private static final String TAG = "DataExporter";
	private static final int BUFFER = 2048;
	private static final int NOTIFICATION_ID = 1438;
	Context ctx;
	
	public DataExporter(Context context) {
		ctx = context;
	}
	
	public String exportToZipFile(boolean exportPhotos) {
		//external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			File exportDir = ctx.getExternalFilesDir(EXPORT_DIR);
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
            BeeperApp app = (BeeperApp)ctx.getApplicationContext();

			String exportFilename = EXPORT_PREFIX;
            if (app.getPreferences().isTestMode()) {
                exportFilename += "testmode_";
            }
            exportFilename += new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + ".zip";
			File exportFile = new File(exportDir, exportFilename);
			ArrayList<File> fileList = new ArrayList<File>();

            String dbName;
            File picDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (app.getPreferences().isTestMode()) {
                dbName = StorageHandler.getTestModeDatabaseName();
                picDir = new File(picDir, PhotoUtils.TEST_MODE_DIR);
            }
            else {
                dbName = StorageHandler.getProductionDatabaseName();
                picDir = new File(picDir, PhotoUtils.NORMAL_MODE_DIR);
            }
            fileList.add(ctx.getDatabasePath(dbName));

			if (picDir.exists() && exportPhotos) {
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

		return path;
	}

    public int getArchiveSize(boolean exportPhotos) {
        BeeperApp app = (BeeperApp)ctx.getApplicationContext();
        File db;
        int archiveSize = 0;

        if (!app.getPreferences().isTestMode()) {
            db = app.getDatabasePath(StorageHandler.getTestModeDatabaseName());
        }
        else {
            db = app.getDatabasePath(StorageHandler.getProductionDatabaseName());
        }

        if (db != null) {
            archiveSize += db.length();
        }

        if (exportPhotos) {
            File[] photoList = PhotoUtils.getPhotos(ctx);
            if (photoList != null) {
                for (int i = 0; i < photoList.length; i++) {
                    archiveSize += photoList[i].length();
                }
            }
        }

        return archiveSize;
    }

    public String getReadableArchiveSize(boolean exportPhotos) {
        int size = getArchiveSize(exportPhotos);
        return getReadableFileSize(size, 0);
    }

    public String getReadableFileSize(int size, int decimals) {
        if(size <= 0) {
            return "0 KB";
        }

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        NumberFormat numFormat = DecimalFormat.getInstance();
        numFormat.setMaximumFractionDigits(decimals);

        return numFormat.format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

}
