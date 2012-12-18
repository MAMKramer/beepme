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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

public class AsyncImageScaler extends Thread {
	
	public static final int BITMAP_MSG = 32;
	private static final String TAG = "AsyncImageScaler";
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
