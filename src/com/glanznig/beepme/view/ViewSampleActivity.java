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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.data.SampleTable;
import com.glanznig.beepme.data.Tag;
import com.glanznig.beepme.helper.AsyncImageScaler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewSampleActivity extends Activity {
	
	private static final String TAG = "ViewSampleActivity";
	private long sampleId;
	
	private static class ImgLoadHandler extends Handler {
		WeakReference<ViewSampleActivity> viewSampleActivity;
		
		ImgLoadHandler(ViewSampleActivity activity) {
			viewSampleActivity = new WeakReference<ViewSampleActivity>(activity);
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == AsyncImageScaler.BITMAP_MSG) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		if (viewSampleActivity.get() != null) {
			    	if (imageBitmap != null) {
						viewSampleActivity.get().findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
						ImageView image = (ImageView)viewSampleActivity.get().findViewById(R.id.view_sample_image);
						image.setImageBitmap(imageBitmap);
						image.setVisibility(View.VISIBLE);
					}
					else {
						viewSampleActivity.get().findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
					}
	    		}
	    	}
	    }
	};
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.view_sample);
        
        sampleId = 0L;
		
		if (savedState != null) {
			if (savedState.getLong("sampleId") != 0L) {
				sampleId = savedState.getLong("sampleId");
			}
		}
		else {
			Bundle b = getIntent().getExtras();
			if (b != null) {
				sampleId = b.getLong(getApplication().getClass().getPackage().getName() + ".SampleId");
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		findViewById(R.id.view_sample_image).setVisibility(View.GONE);
		populateFields();
	}
	
	private void populateFields() {
		if (sampleId != 0L) {
			Sample s = new SampleTable(this.getApplicationContext()).getSampleWithTags(sampleId);
			
			LinearLayout editBtn = (LinearLayout)findViewById(R.id.view_sample_buttons);
			//not editable if more than a day old
			if ((new Date().getTime() - s.getTimestamp().getTime()) >= 24 * 60 * 60 * 1000) {
				editBtn.setVisibility(View.GONE);
			}
			
			TextView timestamp = (TextView)findViewById(R.id.view_sample_timestamp);
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			timestamp.setText(dateFormat.format(s.getTimestamp()));
			
			TextView title = (TextView)findViewById(R.id.view_sample_title);
			if (s.getTitle() != null) {
				title.setText(s.getTitle());
			}
			else {
				title.setVisibility(View.GONE);
			}
			
			TextView description = (TextView)findViewById(R.id.view_sample_description);
			if (s.getDescription() != null && s.getDescription().length() > 0) {
				description.setText(s.getDescription());
			}
			else {
				findViewById(R.id.view_sample_label_description).setVisibility(View.GONE);
				description.setVisibility(View.GONE);
			}
			
			List<Tag> tags = s.getTags();
			if (tags.size() > 0) {
				Iterator<Tag> i = tags.iterator();
				String tagsOutput = "";
				if (i.hasNext()) {
					tagsOutput = i.next().getName();
				}
				while (i.hasNext()) {
					tagsOutput += "   " + i.next().getName();
				}
				
				TextView tagsView = (TextView)findViewById(R.id.view_sample_tags);
				tagsView.setText(tagsOutput);
				tagsView.setVisibility(View.VISIBLE);
				findViewById(R.id.view_sample_label_tags).setVisibility(View.VISIBLE);
			}
			else {
				findViewById(R.id.view_sample_tags).setVisibility(View.GONE);
				findViewById(R.id.view_sample_label_tags).setVisibility(View.GONE);
			}
			
			if (s.getPhotoUri() != null) {
				findViewById(R.id.view_sample_image_load).setVisibility(View.VISIBLE);
			    
				//get display dimensions
				Display display = getWindowManager().getDefaultDisplay();
				int imageWidth = display.getWidth() - 20;
				AsyncImageScaler loader = new AsyncImageScaler(s.getPhotoUri(), imageWidth, new ImgLoadHandler(ViewSampleActivity.this));
				loader.start();
			}
		}
	}
	
	public void onClickEdit(View view) {
		Intent i = new Intent(ViewSampleActivity.this, NewSampleActivity.class);
		i.putExtra(getApplication().getClass().getPackage().getName() + ".SampleId", sampleId);
		startActivity(i);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putLong("sampleId", sampleId);
	}
}
