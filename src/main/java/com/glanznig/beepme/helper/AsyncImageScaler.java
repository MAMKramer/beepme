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

import android.graphics.Bitmap;
import android.os.Handler;

public class AsyncImageScaler extends Thread {
	
	public static final int MSG_SUCCESS = 32;
	public static final int MSG_ERROR = 33;
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
        Bitmap photo = PhotoUtils.scalePhoto(srcUri, destUri, destWidth, destHeight);

        if (handler != null) {
            if (photo != null) {
                handler.obtainMessage(MSG_SUCCESS, name, 0, photo).sendToTarget();
            } else {
                handler.obtainMessage(MSG_ERROR).sendToTarget();
            }
        }
	}

}
