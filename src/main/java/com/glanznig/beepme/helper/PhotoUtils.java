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

package com.glanznig.beepme.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.glanznig.beepme.BeeperApp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import it.sephiroth.android.library.media.ExifInterfaceExtended;

public class PhotoUtils {
	
	private static final String TAG = "PhotoUtils";
	
	private static final String PHOTO_PREFIX = "beeper_img_";
	private static final String PHOTO_THUMB_SUFFIX = "_thumb_";
	private static final String CHANGE_PHOTO_NAME = "swap";
	
	public static final String NORMAL_MODE_DIR = "normal";
	public static final String TEST_MODE_DIR = "testmode";
	public static final String THUMB_DIR = "thumbs";
	
	public static final int TAKE_PHOTO_INTENT = 3648;
	public static final int CHANGE_PHOTO_INTENT = 3638;
	public static final int MSG_PHOTO_LOADED = 48;
	public static final int MSG_PHOTO_LOAD_ERROR = 49;
	public static final String EXTRA_KEY = MediaStore.EXTRA_OUTPUT;

    private static final int PHOTO_QUALITY = 90;
	
	public static Intent getTakePhotoIntent(Context ctx, Date timestamp) {
		return getPhotoIntent(ctx, timestamp);
	}
	
	public static Intent getChangePhotoIntent(Context ctx) {
		return getPhotoIntent(ctx, null);
	}
	
	private static Intent getPhotoIntent(Context ctx, Date timestamp) {
		BeeperApp app = (BeeperApp)ctx.getApplicationContext();
		
		// external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File picDir = null;
			
			// add a sub directory depending on whether we are in test mode 
			if (!app.getPreferences().isTestMode()) {
				picDir = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), NORMAL_MODE_DIR);
			}
			else {
				picDir = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TEST_MODE_DIR);
			}
			
			String picFilename = null;
			if (timestamp != null) {
				picFilename = PHOTO_PREFIX + new SimpleDateFormat("yyyyMMddHHmmss").format(timestamp) + ".jpg";
			}
			else {
				picFilename = PHOTO_PREFIX + CHANGE_PHOTO_NAME + ".jpg";
			}
			
			File pictureFile = new File(picDir, picFilename);
			try {
                if(pictureFile.exists() == false) {
                    pictureFile.getParentFile().mkdirs();
                    pictureFile.createNewFile();
                }
            } catch (IOException e) {
            	Log.e(TAG, "unable to create file.", e);
            	return null;
            }
			
			Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			takePic = takePic.putExtra(EXTRA_KEY, Uri.fromFile(pictureFile));
			
			return takePic;
		}
		
		return null;
	}

    public static File[] getPhotos(Context ctx) {
        BeeperApp app = (BeeperApp)ctx.getApplicationContext();

        // external storage is ready and writable - can be used
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File picDir = null;

            // add a sub directory depending on whether we are in test mode
            if (!app.getPreferences().isTestMode()) {
                picDir = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), NORMAL_MODE_DIR);
            } else {
                picDir = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TEST_MODE_DIR);
            }

            File[] picFiles = picDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jpg");
                }
            });

            return picFiles;
        }

        return null;
    }
	
	public static Bitmap getBitmap(Context ctx, String uri) {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
	    	try {
	    		return MediaStore.Images.Media.getBitmap(
	    				ctx.getContentResolver(), Uri.fromFile(new File(uri)));
	    	}
	    	catch(IOException ioe) {
	    		Log.e(TAG, ioe.getMessage());
	    	}
		}
		
		return null;
	}
	
	public static void getAsyncBitmap(Context ctx, final String uri, final Handler handler) {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			final ContentResolver resolver = ctx.getContentResolver();
			
			Thread bitmapLoader = new Thread() {
				public void run() {
					try {
			    		Bitmap photo = MediaStore.Images.Media.getBitmap(
			    				resolver, Uri.fromFile(new File(uri)));
			    		Bundle b = new Bundle();
			    		b.putString("uri", uri);
			    		Message msg = handler.obtainMessage(MSG_PHOTO_LOADED, photo);
			    		msg.setData(b);
			    		msg.sendToTarget();
			    	}
			    	catch(IOException ioe) {
			    		Log.e(TAG, ioe.getMessage());
			    		handler.obtainMessage(MSG_PHOTO_LOAD_ERROR).sendToTarget();
			    	}
				}
			};
			bitmapLoader.start();
		}
		else {
			handler.obtainMessage(MSG_PHOTO_LOAD_ERROR).sendToTarget();
		}
	}
	
	public static boolean swapPhoto(Context ctx, Date timestamp) {
		BeeperApp app = (BeeperApp)ctx.getApplicationContext();
		
		// external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File picDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			
			// add a sub directory depending on whether we are in test mode 
			if (!app.getPreferences().isTestMode()) {
				picDir = new File(picDir.getAbsolutePath() + File.separator + "normal");
			}
			else {
				picDir = new File(picDir.getAbsolutePath() + File.separator + "testmode");
			}
			
			String picFilename = PHOTO_PREFIX + new SimpleDateFormat("yyyyMMddHHmmss").format(timestamp) + ".jpg";
			String swapFilename = PHOTO_PREFIX + CHANGE_PHOTO_NAME + ".jpg";
			
			File pictureFile = new File(picDir, picFilename);
			File swapFile = new File(picDir, swapFilename);
			
			if (pictureFile.delete()) {
				return swapFile.renameTo(pictureFile);
			}
		}
		
		return false;
	}
	
	public static boolean deletePhoto(Context ctx, String uri) {
		return deletePhoto(ctx, uri, false);
	}
	
	public static boolean deleteThumbnails(Context ctx, String uri) {
		return deletePhoto(ctx, uri, true);
	}
	
	private static boolean deletePhoto(Context ctx, String uri, boolean thumbnailsOnly) {
		if (uri != null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			File photo = new File(uri);
			if (photo != null) {
				String path = photo.getParent();
				
				// delete thumbnails
                BeeperApp app = (BeeperApp)ctx.getApplicationContext();
                int[] thumbSizes = app.getPreferences().getThumbnailSizes();
				for (int i = 0; i < thumbSizes.length; i++) {
					// get name without .jpg extension
					String thumbPath = path + File.separator + THUMB_DIR;
					String name = photo.getName().substring(0, photo.getName().length() - 4);
					name = name + PHOTO_THUMB_SUFFIX + thumbSizes[i] + ".jpg";
					File thumb = new File(thumbPath, name);
					thumb.delete();
				}
				
				if (!thumbnailsOnly) {
					photo.delete();
				}
			}
			return true;
		}
		return false;
	}
	
	public static boolean deleteSwapPhoto(Context ctx) {
		BeeperApp app = (BeeperApp)ctx.getApplicationContext();
		
		// external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File picDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			
			// add a sub directory depending on whether we are in test mode 
			if (!app.getPreferences().isTestMode()) {
				picDir = new File(picDir.getAbsolutePath() + File.separator + "normal");
			}
			else {
				picDir = new File(picDir.getAbsolutePath() + File.separator + "testmode");
			}
			
			String picFilename = PHOTO_PREFIX + CHANGE_PHOTO_NAME + ".jpg";
			File pictureFile = new File(picDir, picFilename);
			if (pictureFile != null) {
				deletePhoto(ctx, pictureFile.getAbsolutePath());
				return true;
			}
		}
		
		return false;
	}
	
	public static void regenerateThumbnails(Context ctx, String uri, Handler handler) {
		deleteThumbnails(ctx, uri);
		final float scale = ctx.getResources().getDisplayMetrics().density;
        BeeperApp app = (BeeperApp)ctx.getApplicationContext();
        int[] thumbSizes = app.getPreferences().getThumbnailSizes();
		for (int i = 0; i < thumbSizes.length; i++) {
			generateThumbnail(uri, thumbSizes[i], (int)(thumbSizes[i] * scale + 0.5f), handler);
		}
	}
	
	public static void generateThumbnails(Context ctx, String uri, Handler handler) {
		final float scale = ctx.getResources().getDisplayMetrics().density;
        BeeperApp app = (BeeperApp)ctx.getApplicationContext();
        int[] thumbSizes = app.getPreferences().getThumbnailSizes();
		for (int i = 0; i < thumbSizes.length; i++) {
			generateThumbnail(uri, thumbSizes[i], (int)(thumbSizes[i] * scale + 0.5f), handler);
		}
	}
	
	public static void generateThumbnail(String uri, int thumbName, int size, Handler handler) {
		if (uri != null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File photo = new File(uri);
			if (photo != null) {
				String path = photo.getParent() + File.separator + THUMB_DIR;
				// get name without .jpg extension
				String name = photo.getName().substring(0, photo.getName().length() - 4);
				name = name + PHOTO_THUMB_SUFFIX + thumbName + ".jpg";
				photo = new File(path, name);
				
				// create thumbs dir if it not already exists
				if(!photo.getParentFile().exists()) {
                    photo.getParentFile().mkdirs();
				}
				
				if (photo != null && !photo.exists()) {
					AsyncImageScaler scaler = new AsyncImageScaler(uri, photo.getAbsolutePath(), thumbName, size, size, handler);
					scaler.start();
				}
			}
		}
	}
	
	public static String getThumbnailUri(String photoUri, int size) {
		if (photoUri != null) {
			File photo = new File(photoUri);
			String path = photo.getParent() + File.separator + THUMB_DIR;
			// get name without .jpg extension
			String name = photo.getName().substring(0, photo.getName().length() - 4);
			name = name + PHOTO_THUMB_SUFFIX + size + ".jpg";
			photo = new File(path, name);
			
			return photo.getAbsolutePath();
		}
		return null;
	}
	
	public static boolean isEnabled(Context ctx) {
		boolean enabled = true;
		
		//check if device has camera feature
        if (!ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
        	enabled = false;
        }
        else {
        	//check if device has app for taking images
        	List<ResolveInfo> availApps = ctx.getPackageManager().queryIntentActivities(
        			new Intent(MediaStore.ACTION_IMAGE_CAPTURE), PackageManager.MATCH_DEFAULT_ONLY);
        	if (availApps.size() == 0) {
        		enabled = false;
        	}
        	//check if image can be saved to external storage
        	else if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
        		enabled = false;
        	}
        }
		
		return enabled;
	}

    public static Bundle getPhotoDimensions(String uri) {
        Bundle dim = new Bundle();

        if (uri != null) {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                FileInputStream fileInput = new FileInputStream(uri);

                // decode only image size
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(fileInput, null, opts);
                fileInput.close();

                dim.putInt("width", opts.outWidth);
                dim.putInt("height", opts.outHeight);
            }
            catch(Exception e) {}
        }

        return dim;
    }

    public static Bitmap scalePhoto(String srcUri, String destUri, int destWidth, int destHeight) {
        if (srcUri != null && destWidth > 0 && destHeight > 0) {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                FileInputStream fileInput = new FileInputStream(srcUri);

                // first decode only image size to determine scale factor
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(fileInput, null, opts);
                fileInput.close();

                int srcWidth = opts.outWidth;
                int srcHeight = opts.outHeight;

                // check if photo needs to be rotated
                ExifInterfaceExtended srcExif = new ExifInterfaceExtended(srcUri);

                int rotationTag = srcExif.getAttributeInt(ExifInterfaceExtended.TAG_EXIF_ORIENTATION,
                        ExifInterfaceExtended.ORIENTATION_NORMAL);
                int rotateDeg = 0;

                if (rotationTag == ExifInterfaceExtended.ORIENTATION_ROTATE_90) {
                    rotateDeg = 90;
                    // swap width and height for scaling
                    int swap = srcWidth;
                    srcWidth = srcHeight;
                    srcHeight = swap;

                    swap = destWidth;
                    destWidth = destHeight;
                    destHeight = swap;
                }
                else if (rotationTag == ExifInterfaceExtended.ORIENTATION_ROTATE_180) {
                    rotateDeg = 180;
                }
                else if (rotationTag == ExifInterfaceExtended.ORIENTATION_ROTATE_270) {
                    rotateDeg = 270;
                    // swap width and height for scaling
                    int swap = srcWidth;
                    srcWidth = srcHeight;
                    srcHeight = swap;

                    swap = destWidth;
                    destWidth = destHeight;
                    destHeight = swap;
                }

                float srcRatio =  (float)srcWidth / (float)srcHeight;
                float destRatio =  (float)destWidth / (float)destHeight;

                int scale = 1;
                while(srcWidth / scale / 2 >= destWidth && srcHeight / scale / 2 >= destHeight) {
                    scale *= 2;
                }

                // now decode image with scale factor (inSampleSize)
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = scale;
                fileInput = new FileInputStream(srcUri);
                Bitmap photo = BitmapFactory.decodeStream(fileInput, null, opts);
                fileInput.close();

                // rotate photo (if needed)
                Matrix matrix = new Matrix();
                Bitmap rotatedPhoto = null;
                if (rotateDeg != 0) {
                    matrix.preRotate(rotateDeg);
                    rotatedPhoto = Bitmap.createBitmap(photo, 0, 0,
                            photo.getWidth(), photo.getHeight(), matrix, true);
                }

                if (rotatedPhoto != null) {
                    photo.recycle();
                    photo = rotatedPhoto;
                }

                // scale photo
                Bitmap croppedPhoto = null;
                if (Math.abs(srcRatio - destRatio) > 0.001) {
                    // different ratio: scale to fit CENTER
                    croppedPhoto = ThumbnailUtils.extractThumbnail(photo, destWidth, destHeight);

                    if (croppedPhoto != null) {
                        photo.recycle();
                        photo = croppedPhoto;
                    }
                }
                else {
                    photo = Bitmap.createScaledBitmap(photo, destWidth, destHeight, true);
                }

                // save image to file
                if (destUri != null) {
                    FileOutputStream outStream = new FileOutputStream(destUri);
                    photo.compress(Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, outStream);
                }

                // attach exif information from src photo
                Bundle exifData = new Bundle();
                srcExif.copyTo(exifData);
                ExifInterfaceExtended destExif = new ExifInterfaceExtended(destUri);
                destExif.copyFrom(exifData, true);
                // since we rotated the photo orientation tag should be reset
                destExif.setAttribute(ExifInterfaceExtended.TAG_EXIF_ORIENTATION,
                        String.valueOf(ExifInterfaceExtended.ORIENTATION_NORMAL));
                destExif.saveAttributes();

                return photo;
            }
            catch(Exception e) {
                Log.e(TAG, "Error while scaling image.", e);
            }
        }

        return null;
    }

}
