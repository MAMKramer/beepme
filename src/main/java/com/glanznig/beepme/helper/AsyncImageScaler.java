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

package com.glanznig.beepme.helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.util.Log;

public class AsyncImageScaler extends Thread {
	
	public static final int MSG_SUCCESS = 32;
	public static final int MSG_ERROR = 33;
	private static final int IMG_QUALITY = 75;
	private static final String TAG = "AsyncImageScaler";
	
	private String srcUri;
	private String destUri;
	private int destWidth;
	private int destHeight;
	private int name;
	private Handler handler;
	
	public AsyncImageScaler(String srcUri, String destUri, int name, int destWidth, int destHeight, Handler handler) {
		this.srcUri = srcUri;
		this.destUri = destUri;
		this.destWidth = destWidth;
		this.destHeight = destHeight;
		this.handler = handler;
		this.name = name;
	}

	@Override
	public void run() {
		if (srcUri != null && destWidth > 0 && destHeight > 0) {
			try {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				FileInputStream fileInput = new FileInputStream(srcUri);
				
				// first decode only image size to determine scale factor
		        opts.inJustDecodeBounds = true;
		        BitmapFactory.decodeStream(fileInput, null, opts);
		        fileInput.close();
		        
		        float srcRatio =  (float)opts.outWidth / (float)opts.outHeight;
		        float destRatio =  (float)destWidth / (float)destHeight;
	
		        int scale = 1;
		        while(opts.outWidth / scale / 2 > destWidth && opts.outHeight / scale / 2 > destHeight) {
		            scale *= 2;
		        }
	
		        // now decode image with scale factor (inSampleSize)
		        opts.inJustDecodeBounds = false;
		        opts.inSampleSize = scale;
		        fileInput = new FileInputStream(srcUri);
		        Bitmap scaledPhoto = BitmapFactory.decodeStream(fileInput, null, opts);
		        fileInput.close();
		        
		        // if different ratio needed create new bitmap with scale to fit CENTER
		        Bitmap croppedPhoto = null;
		        if (Math.abs(srcRatio - destRatio) > 0.001) {
		        	croppedPhoto = ThumbnailUtils.extractThumbnail(scaledPhoto, destWidth, destHeight);
		        }
		        
		        if (croppedPhoto != null) {
		        	scaledPhoto.recycle();
		        	scaledPhoto = croppedPhoto;
		        }
		        
		        // check if photo needs to be rotated
		        ExifInterface exif = new ExifInterface(srcUri);
		        int rotationTag = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		        int rotateDeg = 0;
		        if (rotationTag == ExifInterface.ORIENTATION_ROTATE_90) {
		        	rotateDeg = 90;
		        } 
		        else if (rotationTag == ExifInterface.ORIENTATION_ROTATE_180) {
		        	rotateDeg = 180;
		        } 
		        else if (rotationTag == ExifInterface.ORIENTATION_ROTATE_270) {
		        	rotateDeg = 270;
		        } 
		        
		        Matrix matrix = new Matrix();
		        Bitmap rotatedPhoto = null;
		        if (rotateDeg != 0) {
		        	matrix.preRotate(rotateDeg);
		        	rotatedPhoto = Bitmap.createBitmap(scaledPhoto, 0, 0,
			        		scaledPhoto.getWidth(), scaledPhoto.getHeight(), matrix, true);
		        }
		        
		        if (rotatedPhoto != null) {
		        	scaledPhoto.recycle();
		        	scaledPhoto = rotatedPhoto;
		        }
		        
		        // save image to file
		        if (destUri != null) {
		        	FileOutputStream outStream = new FileOutputStream(destUri);
		        	scaledPhoto.compress(CompressFormat.JPEG, IMG_QUALITY, outStream);
		        }
				
				if (handler != null) {
		        	handler.obtainMessage(MSG_SUCCESS, name, 0, scaledPhoto).sendToTarget();
		        }
			}
			catch(Exception e) {
				if (handler != null) {
					handler.obtainMessage(MSG_ERROR).sendToTarget();
				}
				Log.e(TAG, "Failed loading image.", e);
			}
		}
		else {
			if (handler != null) {
				handler.obtainMessage(MSG_ERROR).sendToTarget();
			}
		}
	}

}
