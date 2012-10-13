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
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewSampleActivity extends Activity {
	
	private static final int TAKE_PICTURE = 42;
	private static final int THUMBNAIL_HEIGHT = 48;
	private static final int THUMBNAIL_WIDTH = 66;
	private static final String TAG = "beeper";
	
	private Sample sample = new Sample();
	private boolean isEdit = false;
	private boolean photoTaken = false;
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.new_sample);
		
		if (savedState != null) {
			if (savedState.getLong("sampleId") != 0L) {
				sample = new Sample(savedState.getLong("sampleId"));
			}
			
			if (savedState.getLong("timestamp") != 0L) {
				sample.setTimestamp(new Date(savedState.getLong("timestamp")));
			}
			
			if (savedState.getCharSequence("title") != null) {
				sample.setTitle((String)savedState.getCharSequence("title"));
			}
			
			if (savedState.getCharSequence("description") != null) {
				sample.setDescription((String)savedState.getCharSequence("description"));
			}
			
			sample.setAccepted(savedState.getBoolean("accepted"));
		}
		else {
			Bundle b = getIntent().getExtras();
			if (b != null) {
				long sampleId = b.getLong("sampleId");
				BeeperApp app = (BeeperApp)getApplication();
				sample = app.getSample(sampleId);
				
				isEdit = true;
			}
			else {
				sample.setTimestamp(new Date());
			}
		}
		
		populateFields();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		populateFields();
	}
	
	private void populateFields() {
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
        
        if (isEdit) {
        	setTitle(R.string.edit_sample);
			findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
			
			Button save = (Button)findViewById(R.id.new_sample_btn_save);
			Button cancel = (Button)findViewById(R.id.new_sample_btn_cancel);
			save.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			cancel.setVisibility(View.VISIBLE);
			//get display dimensions
			Display display = getWindowManager().getDefaultDisplay();
			int width = (display.getWidth() - 10) / 2;
			save.setWidth(width);
			cancel.setWidth(width);
        }
        else {
        	setTitle(R.string.new_sample);
        	
        	Button save = (Button)findViewById(R.id.new_sample_btn_save);
        	save.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        
        
        if (sample.getTimestamp() != null) {
        	TextView timestamp = (TextView)findViewById(R.id.new_sample_timestamp);
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			timestamp.setText(dateFormat.format(sample.getTimestamp()));
        }
		
        if (sample.getTitle() != null) {
        	EditText titleWidget = (EditText)findViewById(R.id.new_sample_title);
        	titleWidget.setText(sample.getTitle());
        }
		
        if (sample.getDescription() != null) {
        	EditText descriptionWidget = (EditText)findViewById(R.id.new_sample_description);
        	descriptionWidget.setText(sample.getDescription());
        }
		
		CheckBox acceptedWidget = (CheckBox)findViewById(R.id.new_sample_accepted);
		acceptedWidget.setChecked(sample.getAccepted());
	}
	
	public void onClickSave(View view) {
		BeeperApp app = (BeeperApp)getApplication();
		
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		CheckBox accepted = (CheckBox)findViewById(R.id.new_sample_accepted);
		
		sample.setTitle(title.getText().toString());
		sample.setDescription(description.getText().toString());
		sample.setAccepted(accepted.isChecked());
		
		if (isEdit) {
			app.editSample(sample);
		}
		else {
			app.addSample(sample);
		}
		finish();
	}
	
	public void onClickCancel(View view) {
		finish();
	}
	
	public void onClickTakePhoto(View view) {
		//external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			//Context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
			String picPath = Environment.getExternalStorageDirectory().getName() + File.separatorChar + "Android"
			+ File.separatorChar + "data" + File.separatorChar + NewSampleActivity.this.getPackageName() + File.separatorChar
			+ "pics" + File.separatorChar + "beeper_img_" + new SimpleDateFormat("yyyyMMddHHmmss").format(sample.getTimestamp()) + ".jpg";
			File pictureFile = new File(picPath);
			try {
                if(pictureFile.exists() == false) {
                    pictureFile.getParentFile().mkdirs();
                    pictureFile.createNewFile();
                    sample.setPhotoUri(pictureFile.getAbsolutePath());
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
				photoTaken = true;
				
				//create thumbnail
				Bitmap imageBitmap = null;
				try {
					FileInputStream input = new FileInputStream(sample.getPhotoUri());
					imageBitmap = BitmapFactory.decodeStream(input);
					
					Float width  = Float.valueOf(imageBitmap.getWidth());
					Float height = Float.valueOf(imageBitmap.getHeight());
					Float ratio = width/height;
					imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int)(THUMBNAIL_HEIGHT*ratio), THUMBNAIL_HEIGHT, false);
					
					ByteArrayOutputStream output = new ByteArrayOutputStream();
		            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
		            sample.setPhotoThumb(output.toByteArray());
				}
				catch (IOException ioe) {
					Log.e(TAG, "failed to create thumbnail.", ioe);
				}
				
				//replace button with thumbnail
				if (sample.getPhotoThumb() != null) {
					findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
					
					ImageView imageView = (ImageView)findViewById(R.id.new_sample_thumb);
					imageView.setVisibility(View.VISIBLE);
					int padding = (THUMBNAIL_WIDTH - imageBitmap.getWidth())/2;
					imageView.setPadding(padding, 0, padding, 0);
					imageView.setImageBitmap(imageBitmap);
				}
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		CheckBox accepted = (CheckBox)findViewById(R.id.new_sample_accepted);
		
		if (sample.getTimestamp() != null) {
			savedState.putLong("timestamp", sample.getTimestamp().getTime());
		}
		savedState.putLong("sampleId", sample.getId());
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putBoolean("accepted", accepted.isChecked());
	}
}
