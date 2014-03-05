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

package com.glanznig.beepme.view;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.MainSectionsPagerAdapter;
import com.glanznig.beepme.R;
import com.glanznig.beepme.ViewSamplePagerAdapter;
import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.data.SampleTable;
import com.glanznig.beepme.data.Tag;
import com.glanznig.beepme.helper.AsyncImageScaler;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewSampleFragment extends Fragment {
	
	private static final String TAG = "ViewSampleFragment";
	private long sampleId;
	
	private static class ImgLoadHandler extends Handler {
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
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        super.onCreate(savedState);
        
        View rootView = inflater.inflate(R.layout.view_sample, container, false);
        
        sampleId = 0L;
		
		if (savedState != null) {
			if (savedState.getLong("sampleId") != 0L) {
				sampleId = savedState.getLong("sampleId");
			}
		}
		else {
			Bundle args = getArguments();
			sampleId = args.getLong("sampleId");
		}
        
        return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getView().findViewById(R.id.view_sample_image).setVisibility(View.GONE);
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
				title.setVisibility(View.VISIBLE);
			}
			else {
				title.setVisibility(View.GONE);
			}
			
			TextView description = (TextView)getView().findViewById(R.id.view_sample_description);
			if (s.getDescription() != null && s.getDescription().length() > 0) {
				description.setText(s.getDescription());
				getView().findViewById(R.id.view_sample_label_description).setVisibility(View.VISIBLE);
				description.setVisibility(View.VISIBLE);
			}
			else {
				getView().findViewById(R.id.view_sample_label_description).setVisibility(View.GONE);
				description.setVisibility(View.GONE);
			}
			
			List<Tag> tags = s.getTags();
			boolean hasKeywordTags = false;
			
			if (tags.size() > 0) {
				Iterator<Tag> i = tags.iterator();
				String keywordsOutput = "";
				while (i.hasNext()) {
					Tag t = i.next();
					if (t.getVocabularyId() == 1) {
						if (keywordsOutput.length() > 0) {
							keywordsOutput += "   ";
						}
						keywordsOutput += t.getName();
						hasKeywordTags = true;
					}
				}
				
				TextView keywordsView = (TextView)getView().findViewById(R.id.view_sample_keywords);
				keywordsView.setText(keywordsOutput);
			}
			
			if (!hasKeywordTags) {
				getView().findViewById(R.id.view_sample_keywords).setVisibility(View.GONE);
				getView().findViewById(R.id.view_sample_label_keywords).setVisibility(View.GONE);
			}
			else {
				getView().findViewById(R.id.view_sample_keywords).setVisibility(View.VISIBLE);
				getView().findViewById(R.id.view_sample_label_keywords).setVisibility(View.VISIBLE);
			}
			
			if (s.getPhotoUri() != null) {
				getView().findViewById(R.id.view_sample_image_load).setVisibility(View.VISIBLE);
			    
				//get display dimensions
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				int imageWidth = display.getWidth() - 20;
				AsyncImageScaler loader = new AsyncImageScaler(s.getPhotoUri(), imageWidth, new ImgLoadHandler(ViewSampleFragment.this));
				loader.start();
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putLong("sampleId", sampleId);
	}
}
