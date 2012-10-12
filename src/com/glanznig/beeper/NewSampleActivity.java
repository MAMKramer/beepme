package com.glanznig.beeper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.glanznig.beeper.data.Sample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class NewSampleActivity extends Activity {
	
	private static final int TAKE_PICTURE = 42;
	private static final int THUMBNAIL_HEIGHT = 48;
	private static final int THUMBNAIL_WIDTH = 66;
	private static final String TAG = "beeper";
	
	private Date sampleTimestamp;
	private long sampleId = 0L;
	private File pictureFile;
	private byte[] pictureThumb;
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.new_sample);
        
        //hide thumbnail placeholder
        findViewById(R.id.new_sample_thumb).setVisibility(View.GONE);
        
        //check if device has camera feature
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
        	findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
        }
        else {
        	//check if device has app for taking images
        	List<ResolveInfo> availApps = getPackageManager().queryIntentActivities(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), PackageManager.MATCH_DEFAULT_ONLY);
        	if (availApps.size() == 0) {
        		findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
        	}
        	//check if image can be saved to external storage
        	else if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
        		findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
        	}
        }
        
        sampleTimestamp = new Date();
        pictureFile = null;
        String title = null;
        String description = null;
        boolean accepted = false;
		
		if (savedState != null) {
			if (savedState.getLong("timestamp") != 0L) {
				sampleTimestamp = new Date(savedState.getLong("timestamp"));
			}
			if (savedState.getCharSequence("title") != null) {
				title = (String)savedState.getCharSequence("title");
			}
			if (savedState.getCharSequence("description") != null) {
				description = (String)savedState.getCharSequence("description");
			}
			if (savedState.getLong("sampleId") != 0L) {
				sampleId = savedState.getLong("sampleId");
			}
			accepted = savedState.getBoolean("accepted");
		}
		else {
			Bundle b = getIntent().getExtras();
			if (b != null) {
				sampleId = b.getLong("sampleId");
				BeeperApp app = (BeeperApp)getApplication();
				Sample s = app.getSample(sampleId);
				
				sampleTimestamp = s.getTimestamp();
				title = s.getTitle();
				description = s.getDescription();
				accepted = s.getAccepted();
			}
		}
		
		TextView timestamp = (TextView)findViewById(R.id.new_sample_timestamp);
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		timestamp.setText(dateFormat.format(sampleTimestamp));
		
		EditText titleWidget = (EditText)findViewById(R.id.new_sample_title);
		titleWidget.setText(title);
		
		EditText descriptionWidget = (EditText)findViewById(R.id.new_sample_description);
		descriptionWidget.setText(description);
		
		CheckBox acceptedWidget = (CheckBox)findViewById(R.id.new_sample_accepted);
		acceptedWidget.setChecked(accepted);
	}
	
	public void onClickSave(View view) {
		BeeperApp app = (BeeperApp)getApplication();
		Sample s;
		if (sampleId == 0L) {
			s = new Sample();
		}
		else {
			s = new Sample(sampleId);
		}
		
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		CheckBox accepted = (CheckBox)findViewById(R.id.new_sample_accepted);
		
		s.setTimestamp(sampleTimestamp);
		s.setTitle(title.getText().toString());
		s.setDescription(description.getText().toString());
		s.setAccepted(accepted.isChecked());
		if (pictureFile != null) {
			s.setPhotoUri(pictureFile.getAbsolutePath());
			if (pictureThumb != null) {
				s.setPhotoThumb(pictureThumb);
			}
		}
		
		if (sampleId == 0L) {
			app.addSample(s);
		}
		else {
			app.editSample(s);
		}
		finish();
	}
	
	public void onClickTakePhoto(View view) {
		//external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			//Context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
			String picPath = Environment.getExternalStorageDirectory().getName() + File.separatorChar + "Android"
			+ File.separatorChar + "data" + File.separatorChar + NewSampleActivity.this.getPackageName() + File.separatorChar
			+ "pics" + File.separatorChar + "beeper_img_" + new SimpleDateFormat("yyyyMMddHHmmss").format(sampleTimestamp) + ".jpg";
			pictureFile = new File(picPath);
			try {
                if(pictureFile.exists() == false) {
                    pictureFile.getParentFile().mkdirs();
                    pictureFile.createNewFile();
                }
            } catch (IOException e) {
            	Log.e(TAG, "unable to create file.", e);
            }
			
			Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			takePic = takePic.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(pictureFile));
			startActivityForResult(takePic, TAKE_PICTURE);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TAKE_PICTURE) {
			if (resultCode == Activity.RESULT_OK) {
				//create thumbnail
				pictureThumb = null;
				Bitmap imageBitmap = null;
				try {
					FileInputStream input = new FileInputStream(pictureFile);
					imageBitmap = BitmapFactory.decodeStream(input);
					
					Float width  = Float.valueOf(imageBitmap.getWidth());
					Float height = Float.valueOf(imageBitmap.getHeight());
					Float ratio = width/height;
					imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int)(THUMBNAIL_HEIGHT*ratio), THUMBNAIL_HEIGHT, false);
					
					ByteArrayOutputStream output = new ByteArrayOutputStream();
		            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
		            pictureThumb = output.toByteArray();
				}
				catch (IOException ioe) {
					Log.e(TAG, "failed to create thumbnail.", ioe);
				}
				
				//replace button with thumbnail
				if (pictureThumb != null) {
					findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
					
					ImageView imageView = (ImageView)findViewById(R.id.new_sample_thumb);
					imageView.setVisibility(View.VISIBLE);
					int padding = (THUMBNAIL_WIDTH - imageBitmap.getWidth())/2;
					imageView.setPadding(padding, 0, padding, 0);
					imageView.setImageBitmap(imageBitmap);
				}
			}
			else if (resultCode == Activity.RESULT_CANCELED) {
				pictureFile = null;
				pictureThumb = null;
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		CheckBox accepted = (CheckBox)findViewById(R.id.new_sample_accepted);
		
		savedState.putLong("timestamp", sampleTimestamp.getTime());
		savedState.putLong("sampleId", sampleId);
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putBoolean("accepted", accepted.isChecked());
	}
}
