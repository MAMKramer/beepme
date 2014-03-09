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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;

import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.data.SampleTable;
import com.glanznig.beepme.data.Tag;
import com.glanznig.beepme.helper.FlowLayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewSampleFragment extends Fragment {
	
	private static final String TAG = "ViewSampleFragment";
	private long sampleId = 0L;
	
	/*private static class ImgLoadHandler extends Handler {
		WeakReference<ViewSampleFragment> viewSampleFragment;
		
		ImgLoadHandler(ViewSampleFragment activity) {
			viewSampleFragment = new WeakReference<ViewSampleFragment>(activity);
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == AsyncImageScaler.BITMAP_MSG) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		if (viewSampleFragment.get() != null) {
			    	if (imageBitmap != null) {
						viewSampleFragment.get().getView().findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
						ImageView image = (ImageView)viewSampleFragment.get().getView().findViewById(R.id.view_sample_image);
						image.setImageBitmap(imageBitmap);
						image.setVisibility(View.VISIBLE);
					}
					else {
						viewSampleFragment.get().getView().findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
					}
	    		}
	    	}
	    }
	};*/
	
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
		//getView().findViewById(R.id.view_sample_image).setVisibility(View.GONE);
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
			
			/*if (s.getPhotoUri() != null) {
				getView().findViewById(R.id.view_sample_image_load).setVisibility(View.VISIBLE);
			    
				//get display dimensions
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				int imageWidth = display.getWidth() - 20;
				AsyncImageScaler loader = new AsyncImageScaler(s.getPhotoUri(), imageWidth, new ImgLoadHandler(ViewSampleFragment.this));
				loader.start();
			}*/
		}
	}
}
