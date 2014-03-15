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

package com.glanznig.beepme.view;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;

import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.data.Tag;
import com.glanznig.beepme.db.SampleTable;
import com.glanznig.beepme.helper.AsyncImageScaler;
import com.glanznig.beepme.helper.FlowLayout;
import com.glanznig.beepme.helper.PhotoUtils;

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
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class ViewSampleFragment extends Fragment implements Callback {
	
	private static final String TAG = "ViewSampleFragment";
	private long sampleId = 0L;
	private SamplePhotoView photoView; 
	
	private static class ImgLoadHandler extends Handler {
		WeakReference<SamplePhotoView> view;
		
		ImgLoadHandler(SamplePhotoView view) {
			this.view = new WeakReference<SamplePhotoView>(view);
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
        
        View rootView = inflater.inflate(R.layout.view_sample, container, false);
        Bundle args = getArguments();
		sampleId = args.getLong("sampleId");
        
        return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		populateFields();
	}
	
	private void populateFields() {
		
		if (sampleId != 0L) {
			Sample s = new SampleTable(getActivity().getApplicationContext()).getSampleWithTags(sampleId);
			
			TextView timestamp = (TextView)getView().findViewById(R.id.view_sample_timestamp);
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			timestamp.setText(dateFormat.format(s.getTimestamp()));
			
			TextView title = (TextView)getView().findViewById(R.id.view_sample_title);
			if (s.getTitle() != null && s.getTitle().length() > 0) {
				title.setText(s.getTitle());
			}
			else {
				title.setText(getString(R.string.sample_untitled));
			}
			
			TextView description = (TextView)getView().findViewById(R.id.view_sample_description);
			if (s.getDescription() != null && s.getDescription().length() > 0) {
				description.setTextSize(14);
				description.setText(s.getDescription());
			}
			else {
				description.setTextSize(12);
				// not editable any more
				if ((Calendar.getInstance().getTimeInMillis() - s.getTimestamp().getTime()) >= 24 * 60 * 60 * 1000) {
					description.setText(getString(R.string.sample_no_description));
				}
				else {
					description.setText(getString(R.string.sample_no_description_editable));
				}
			}
			
			boolean hasKeywordTags = false;
			
			FlowLayout keywordHolder = (FlowLayout)getView().findViewById(R.id.view_sample_keyword_container);
			keywordHolder.removeAllViews();
			
	    	Iterator<Tag> i = s.getTags().iterator();
			Tag tag = null;
			
			while (i.hasNext()) {
				tag = i.next();
				if (tag.getVocabularyId() == 1) {
					
					TextView view = new TextView(getView().getContext());
					view.setText(tag.getName());
					
					final float scale = getResources().getDisplayMetrics().density;
					int textPaddingLeftRight = 6;
					int textPaddingTopBottom = 2;
					
					view.setPadding((int)(textPaddingLeftRight * scale + 0.5f), (int)(textPaddingTopBottom * scale + 0.5f), (int)(textPaddingLeftRight * scale + 0.5f), (int)(textPaddingTopBottom * scale + 0.5f));
					view.setBackgroundColor(getResources().getColor(R.color.bg_keyword));
					
					keywordHolder.addView(view);
					hasKeywordTags = true;
				}
			}
			
			TextView noKeywordsView = (TextView)getView().findViewById(R.id.view_sample_no_keywords);
			if (!hasKeywordTags) {
				keywordHolder.setVisibility(View.GONE);
				noKeywordsView.setVisibility(View.VISIBLE);
				// not editable any more
				if ((Calendar.getInstance().getTimeInMillis() - s.getTimestamp().getTime()) >= 24 * 60 * 60 * 1000) {
					noKeywordsView.setText(getString(R.string.sample_no_keywords));
				}
				else {
					noKeywordsView.setText(getString(R.string.sample_no_keywords_editable));
				}
			}
			else {
				noKeywordsView.setVisibility(View.GONE);
				keywordHolder.setVisibility(View.VISIBLE);
			}
			
			photoView = (SamplePhotoView)getView().findViewById(R.id.view_sample_photo);
			photoView.setRights(false, false); // read only
			DisplayMetrics metrics = getView().getContext().getResources().getDisplayMetrics();

			if(!isLandscape()) {
				photoView.setFrameWidth(LayoutParams.MATCH_PARENT);
			}
			
			String thumbnailUri = PhotoUtils.getThumbnailUri(s.getPhotoUri(), (int)(metrics.widthPixels / metrics.density + 0.5f));
			if (thumbnailUri != null) {
				File thumb = new File(thumbnailUri);
				if (thumb.exists()) {
					ImgLoadHandler handler = new ImgLoadHandler(photoView);
					PhotoUtils.getAsyncBitmap(getView().getContext(), thumbnailUri, handler);
				}
				else {
					Handler handler = new Handler(this);
					photoView.measure(0, 0);
					PhotoUtils.generateThumbnail(s.getPhotoUri(), (int)(metrics.widthPixels / metrics.density + 0.5f), metrics.widthPixels, handler);
				}
			}
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == AsyncImageScaler.MSG_SUCCESS) {
			Bitmap photoBitmap = (Bitmap)msg.obj;
			if (photoBitmap != null) {
				photoView.setPhoto(photoBitmap);
				
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
