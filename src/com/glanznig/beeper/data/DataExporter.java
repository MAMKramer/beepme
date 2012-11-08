package com.glanznig.beeper.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class DataExporter {
	
	private static final String EXPORT_PREFIX = "beeper_data_";
	private static final String TAG = "DataExporter";
	private static final int BUFFER = 2048;
	Context ctx;
	
	public DataExporter(Context context) {
		ctx = context;
	}
	
	public String exportToZipFile() {
		//external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
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
			String exportFilename = EXPORT_PREFIX + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".zip";
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
			fileList.add(ctx.getDatabasePath(StorageHandler.DB_NAME));
			
			return zipFiles(exportFile, fileList);
		}
		
		return null;
	}
	
	private String zipFiles(File zipFile, List<File> fileList) {
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
				
				return zipFile.getAbsolutePath();
				
			} catch(Exception e) {
				Log.e(TAG, "error while zipping.", e);
			}
		}
		return null;
	}

}
