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
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import it.sephiroth.android.library.media.ExifInterfaceExtended;

public class AsyncImageScaler extends Thread {
	
	public static final int MSG_SUCCESS = 32;
	public static final int MSG_ERROR = 33;
	private static final int IMG_QUALITY = 90;
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
                }

                float srcRatio =  (float)srcWidth / (float)srcHeight;
                float destRatio =  (float)destWidth / (float)destHeight;
	
		        int scale = 1;
		        while(srcWidth / scale / 2 > destWidth && srcHeight / scale / 2 > destHeight) {
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

                // rotate photo
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

                //attach exif information from src image
                Bundle exifData = new Bundle();
                srcExif.copyTo(exifData);
                ExifInterfaceExtended destExif = new ExifInterfaceExtended(destUri);
                destExif.copyFrom(exifData, true);
                destExif.setAttribute(ExifInterfaceExtended.TAG_EXIF_ORIENTATION,
                        String.valueOf(ExifInterfaceExtended.ORIENTATION_NORMAL));
                destExif.saveAttributes();
				
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
