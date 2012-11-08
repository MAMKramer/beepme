package com.glanznig.beeper.helper;

import java.io.FileInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

public class AsyncImageScaler extends Thread {
	
	public static final int BITMAP_MSG = 32;
	private static final String TAG = "AsyncImageLoader";
	private String uri;
	private int imageWidth;
	private Handler handler;
	
	public AsyncImageScaler(String uri, int imageWidth, Handler handler) {
		this.uri = uri;
		this.imageWidth = imageWidth;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			//Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;

	        FileInputStream fis = new FileInputStream(uri);
	        BitmapFactory.decodeStream(fis, null, o);
	        fis.close();

	        int scale = 1;
	        if (o.outHeight > imageWidth || o.outWidth > imageWidth) {
	            scale = (int)Math.pow(2, (int) Math.round(Math.log(imageWidth / 
	               (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	        }

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        fis = new FileInputStream(uri);
	        Bitmap imageBitmap = BitmapFactory.decodeStream(fis, null, o2);
	        fis.close();
	        handler.obtainMessage(BITMAP_MSG, imageBitmap).sendToTarget();
		}
		catch(Exception e) {
			Log.e(TAG, "Failed loading image.", e);
		}
	}

}
