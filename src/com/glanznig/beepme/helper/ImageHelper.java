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

package com.glanznig.beepme.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

public class ImageHelper {
	
	private static final String TAG = "ImageHelper";
	public static final int TAKE_PICTURE = 3642;
	
	private static final String PICTURE_PREFIX = "beeper_img_";
	private static final int IMG_MAX_DIM = 1024; //in px
	private static final int IMG_QUALITY = 75;
	
	private static class ImgScaleHandler extends Handler {
		String imageUri = null;
		
		ImgScaleHandler(String uri) {
			this.imageUri = uri;
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == AsyncImageScaler.BITMAP_MSG) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		if (imageUri != null) {
	    			//save downscaled image to file
	    			if (imageBitmap != null) {
	    				try {
							FileOutputStream outStream = new FileOutputStream(imageUri);
							//TODO: save in copy, delete, rename?
							imageBitmap.compress(CompressFormat.JPEG, IMG_QUALITY, outStream);
						} catch (Exception e) {
							Log.e(TAG, "error while writing downscaled image", e);
						}
	    			}
	    		}
	    	}
	    }
	};
	
	private Context ctx = null;
	private String imageUri = null;
	private String uriPending = null;
	
	public ImageHelper(Context ctx) {
		this.ctx = ctx;
	}
	
	public Intent getIntent(Date timestamp) {
		//external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File picDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			String picFilename = PICTURE_PREFIX + new SimpleDateFormat("yyyyMMddHHmmss").format(timestamp) + ".jpg";
			File pictureFile = new File(picDir, picFilename);
			try {
                if(pictureFile.exists() == false) {
                    pictureFile.getParentFile().mkdirs();
                    pictureFile.createNewFile();
                    uriPending = pictureFile.getAbsolutePath();
                }
            } catch (IOException e) {
            	Log.e(TAG, "unable to create file.", e);
            }
			
			Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			takePic = takePic.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(pictureFile));
			
			return takePic;
		}
		
		return null;
	}
	
	public String getImageUri() {
		return imageUri;
	}
	
	public void setImageUri(String uri) {
		imageUri = uri;
	}
	
	public void captureSuccess() {
		if (uriPending != null) {
			imageUri = uriPending;
			uriPending = null;
		}
	}
	
	public void scaleImage() {
		if (imageUri != null) {
			AsyncImageScaler loader = new AsyncImageScaler(imageUri, IMG_MAX_DIM, new ImgScaleHandler(imageUri));
			loader.start();
		}
	}
	
	public boolean deleteImage() {
		if (imageUri != null) {
    		File pic = new File(imageUri);
    		if (pic.delete()) {
    			return true;
    		}
    	}
		
		return false;
	}
	
	public boolean isEnabled() {
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
}
