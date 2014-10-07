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

package com.glanznig.beepme.view;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.SingleValue;
import com.glanznig.beepme.data.Value;
import com.glanznig.beepme.data.db.MomentTable;
import com.glanznig.beepme.helper.AsyncImageScaler;
import com.glanznig.beepme.helper.PhotoUtils;
import com.glanznig.beepme.view.input.InputControl;
import com.glanznig.beepme.view.input.PhotoControl;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewMomentFragment extends Fragment implements Callback {
	
	private static final String TAG = "ViewMomentFragment";
	private long momentId = 0L;
    private ViewManager viewManager;
	private PhotoControl photoControl = null;
	
	private static class ImgLoadHandler extends Handler {
		WeakReference<PhotoControl> view;
		
		ImgLoadHandler(PhotoControl view) {
			this.view = new WeakReference<PhotoControl>(view);
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == PhotoUtils.MSG_PHOTO_LOADED) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		
	    		if (view.get() != null && imageBitmap != null) {
	    			view.get().setPhoto(imageBitmap);
	    		}
	    	}
	    }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        super.onCreate(savedState);

        Bundle args = getArguments();
        momentId = args.getLong("momentId");
        
        //View rootView = inflater.inflate(R.layout.view_sample, container, false);
        BeepMeApp app = (BeepMeApp)getActivity().getApplication();
        viewManager = new ViewManager(getActivity(), app.getCurrentProject());
        View rootView = viewManager.getLayout(InputControl.Mode.VIEW);

        photoControl = viewManager.getPhotoControl();
        
        return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		populateFields();
	}
	
	private void populateFields() {
		
		if (momentId != 0L) {
            String titleName = ((BeepMeApp)getActivity().getApplicationContext()).getCurrentProject().getOption("listTitle");
			Moment moment = new MomentTable(getActivity().getApplicationContext()).getMomentWithValues(momentId);
            HashMap<String, Value> values = moment.getValues();
            viewManager.setValues(values);

            // display photo
            DisplayMetrics metrics = getView().getContext().getResources().getDisplayMetrics();
            int thumbnailSize;
            if(!isLandscape()) {
                photoControl.setFrameDimensions(ViewGroup.LayoutParams.MATCH_PARENT, (int)(192 * metrics.density + 0.5f));
                thumbnailSize = (int)(metrics.widthPixels / metrics.density + 0.5f);
            }
            else {
                thumbnailSize = (int)(metrics.heightPixels / metrics.density + 0.5f);
            }

            SingleValue photoValue = (SingleValue)photoControl.getValue();
            if (photoValue.getValue().length() > 0) {
                String thumbnailUri = PhotoUtils.getThumbnailUri(photoValue.getValue(), thumbnailSize);
                if (thumbnailUri != null) {
                    File thumb = new File(thumbnailUri);
                    if (thumb.exists()) {
                        ImgLoadHandler handler = new ImgLoadHandler(photoControl);
                        PhotoUtils.getAsyncBitmap(getView().getContext(), thumbnailUri, handler);
                    } else {
                        Handler handler = new Handler(this);
                        PhotoUtils.generateThumbnails(getView().getContext(), photoValue.getValue(), handler);
                    }
                } else {
                    photoControl.unsetPhoto();
                }
            }

            //todo add title and timestamp to view manager
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == AsyncImageScaler.MSG_SUCCESS) {
			Bitmap photoBitmap = (Bitmap)msg.obj;
			if (photoBitmap != null) {
				photoControl.setPhoto(photoBitmap);
				
				return true;
			}
		}
		
		if (msg.what == AsyncImageScaler.MSG_ERROR) {
			// error handling
		}
		
		return false;
	}
	
	private boolean isLandscape() {
		DisplayMetrics metrics = getView().getContext().getResources().getDisplayMetrics();
		
		if (metrics.heightPixels < metrics.widthPixels) {
			return true;
		}
		else {
			return false;
		}
	}
}
