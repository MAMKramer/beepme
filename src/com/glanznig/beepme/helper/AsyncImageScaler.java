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

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;

public class AsyncImageScaler extends Thread {
	
	public static final int SUCCESS = 32;
	public static final int ERROR = 33;
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
		        
		        float srcRatio =  opts.outWidth / opts.outHeight;
		        float destRatio =  destWidth / destHeight;
	
		        int scale = 1;
		        int shorterSrcSide = 0;
		        int destSide = 0;
		        
		        // always scale with shorter side of source
		        if (opts.outHeight > opts.outWidth) {
		        	shorterSrcSide = opts.outWidth;
		        	destSide = destWidth;
		        }
		        else {
		        	shorterSrcSide = opts.outHeight;
		        	destSide = destHeight;
		        }
		        
		        scale = (int)Math.pow(2, (int) Math.round(Math.log(destSide / (double) shorterSrcSide) / Math.log(0.5)));
	
		        // now decode image with scale factor (inSampleSize)
		        opts.inJustDecodeBounds = false;
		        opts.inSampleSize = scale;
		        fileInput = new FileInputStream(srcUri);
		        Bitmap scaledPhoto = BitmapFactory.decodeStream(fileInput, null, opts);
		        fileInput.close();
		        Log.i(TAG, "scaledPhoto.width="+scaledPhoto.getWidth() + " scaledPhoto.height=" + scaledPhoto.getHeight());
		        
		        // if different ratio needed create new bitmap with scale to fit CENTER
		        Bitmap croppedPhoto = null;
		        if (Math.abs(srcRatio - destRatio) > 0.001) {
		        	Matrix matrix = new Matrix();
		        	RectF src = new RectF(0, 0, scaledPhoto.getWidth(), scaledPhoto.getHeight());
		        	RectF dst = new RectF(0, 0, destWidth, destHeight);
		            if (matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)) {
		            	croppedPhoto = Bitmap.createBitmap(scaledPhoto, 0, 0,
		            			scaledPhoto.getWidth(), scaledPhoto.getHeight(), matrix, true);
		            	Log.i(TAG, "croppedPhoto.width="+croppedPhoto.getWidth() + " croppedPhoto.height=" + croppedPhoto.getHeight());
		            }
		            /*else {
		            	Log.i(TAG, "does not fit");
		            }*/
		        }
		        
		        if (croppedPhoto != null) {
		        	scaledPhoto.recycle();
		        	scaledPhoto = croppedPhoto;
		        }
		        
		        // save image to file
		        if (destUri != null) {
		        	FileOutputStream outStream = new FileOutputStream(destUri);
		        	scaledPhoto.compress(CompressFormat.JPEG, IMG_QUALITY, outStream);
		        }
				
				if (handler != null) {
		        	handler.obtainMessage(SUCCESS, name, 0, scaledPhoto).sendToTarget();
		        }
			}
			catch(Exception e) {
				if (handler != null) {
					handler.obtainMessage(ERROR).sendToTarget();
				}
				Log.e(TAG, "Failed loading image.", e);
			}
		}
		else {
			if (handler != null) {
				handler.obtainMessage(ERROR).sendToTarget();
			}
		}
	}

}
