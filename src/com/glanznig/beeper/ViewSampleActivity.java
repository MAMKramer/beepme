package com.glanznig.beeper;

import java.io.FileInputStream;
import java.text.DateFormat;

import com.glanznig.beeper.data.Sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewSampleActivity extends Activity {
	
	private static final String TAG = "beeper";
	private long sampleId;
	
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
	
	private class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
		
		@Override
		protected Bitmap doInBackground(String... uri) {
			try {
				if (uri.length >= 1) {
					FileInputStream input = new FileInputStream(uri[0]);
					Bitmap imageBitmap = BitmapFactory.decodeStream(input);
					
					Float width  = Float.valueOf(imageBitmap.getWidth());
					Float height = Float.valueOf(imageBitmap.getHeight());
					Float ratio = width/height;
					
					//get display dimensions
					Display display = getWindowManager().getDefaultDisplay();
					int imageWidth = display.getWidth() - 20;
					
					imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int)(imageWidth*ratio), imageWidth, false);
					
					return imageBitmap;
				}
			}
			catch(Exception e) {
				Log.e(TAG, "Something happened on the way.", e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Bitmap imageBitmap) {
			if (imageBitmap != null) {
				ImageView image = (ImageView)ViewSampleActivity.this.findViewById(R.id.view_sample_image);
				//int padding = (THUMBNAIL_WIDTH - imageBitmap.getWidth())/2;
				//image.setPadding(padding, 0, padding, 0);
				image.setImageBitmap(imageBitmap);
				ViewSampleActivity.this.findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
				image.setVisibility(View.VISIBLE);
			}
			else {
				ViewSampleActivity.this.findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
			}
		}
	}
	
	private void populateFields() {
		if (sampleId != 0L) {
			BeeperApp app = (BeeperApp)getApplication();
			Sample s = app.getSample(sampleId);
			
			TextView timestamp = (TextView)findViewById(R.id.view_sample_timestamp);
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			timestamp.setText(dateFormat.format(s.getTimestamp()));
			
			TextView title = (TextView)findViewById(R.id.view_sample_title);
			if (s.getTitle() != null) {
				title.setText(s.getTitle());
			}
			else {
				findViewById(R.id.view_sample_label_title).setVisibility(View.GONE);
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
			
			if (s.getPhotoUri() != null) {
				findViewById(R.id.view_sample_image_load).setVisibility(View.VISIBLE);
			    new ImageLoadTask().execute(new String[] { s.getPhotoUri() });
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
