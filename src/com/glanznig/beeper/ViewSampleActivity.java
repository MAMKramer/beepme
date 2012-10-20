package com.glanznig.beeper;

import java.text.DateFormat;
import java.util.ArrayList;

import com.glanznig.beeper.data.Sample;
import com.glanznig.beeper.data.Tag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewSampleActivity extends Activity {
	
	private static final String TAG = "beeper";
	private long sampleId;
	
	private final Handler imgLoadHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == AsyncImageLoader.BITMAP_MSG) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
		    	if (imageBitmap != null) {
					ViewSampleActivity.this.findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
					ImageView image = (ImageView)ViewSampleActivity.this.findViewById(R.id.view_sample_image);
					image.setImageBitmap(imageBitmap);
					image.setVisibility(View.VISIBLE);
				}
				else {
					ViewSampleActivity.this.findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
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
				sampleId = b.getLong("sampleId");
			}
		}
		
		populateFields();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		findViewById(R.id.view_sample_image).setVisibility(View.GONE);
		populateFields();
	}
	
	private void populateFields() {
		if (sampleId != 0L) {
			BeeperApp app = (BeeperApp)getApplication();
			Sample s = app.getSampleWithTags(sampleId);
			
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
			if (s.getDescription() != null) {
				description.setText(s.getDescription());
			}
			else {
				findViewById(R.id.view_sample_label_description).setVisibility(View.GONE);
				description.setVisibility(View.GONE);
			}
			
			ArrayList<Tag> tags = (ArrayList<Tag>)s.getTags();
			if (tags != null) {
				String tagsOutput = "";
				for (int i = 0; i < tags.size(); i++) {
					Tag t = tags.get(i);
					if (i > 0) {
						tagsOutput += ", ";
					}
					tagsOutput += t.getName();
				}
				
				TextView tagsView = (TextView)findViewById(R.id.view_sample_tags);
				tagsView.setText(tagsOutput);
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
				AsyncImageLoader loader = new AsyncImageLoader(s.getPhotoUri(), imageWidth, imgLoadHandler);
				loader.start();
			}
		}
	}
	
	public void onClickEdit(View view) {
		Intent i = new Intent(ViewSampleActivity.this, NewSampleActivity.class);
		Bundle b = new Bundle();
		b.putLong("sampleId", sampleId);
		i.putExtras(b);
		startActivity(i);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putLong("sampleId", sampleId);
	}
}
