package com.glanznig.beeper;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.glanznig.beeper.data.Sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

public class NewSampleActivity extends Activity {
	
	private static final int TAKE_PICTURE = 42;
	private static final int THUMBNAIL_HEIGHT = 120; //in dp
	private static final int THUMBNAIL_WIDTH = 80; //in dp
	private static final String PICTURE_PREFIX = "beeper_img_";
	private static final String TAG = "beeper";
	
	private Sample sample = new Sample();
	private String photoUri = null;
	private boolean isEdit = false;
	private boolean photoTaken = false;
	
	private final Handler imgLoadHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == AsyncImageLoader.BITMAP_MSG) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		if (imageBitmap != null) {
					NewSampleActivity.this.findViewById(R.id.new_sample_image_load).setVisibility(View.GONE);
					ImageView imageView = (ImageView)NewSampleActivity.this.findViewById(R.id.new_sample_thumb);
					imageView.setImageBitmap(imageBitmap);
					imageView.setVisibility(View.VISIBLE);
				}
				else {
					NewSampleActivity.this.findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
				}
	    	}
	    }
	};
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.new_sample);
        
        ((ImageView)findViewById(R.id.new_sample_thumb)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                NewSampleActivity.this.onLongClickChangeThumb(view);
                return true;
            }
        });
		
		if (savedState != null) {
			if (savedState.getLong("sampleId") != 0L) {
				sample = new Sample(savedState.getLong("sampleId"));
			}
			
			if (savedState.getLong("timestamp") != 0L) {
				sample.setTimestamp(new Date(savedState.getLong("timestamp")));
			}
			
			if (savedState.getCharSequence("title") != null) {
				sample.setTitle(savedState.getCharSequence("title").toString());
			}
			
			if (savedState.getCharSequence("description") != null) {
				sample.setDescription(savedState.getCharSequence("description").toString());
			}
			
			if (savedState.getCharSequence("photoUri") != null) {
				sample.setPhotoUri(savedState.getCharSequence("photoUri").toString());
			}
			
			sample.setAccepted(savedState.getBoolean("accepted"));
			
			isEdit = savedState.getBoolean("isEdit");
			photoTaken = savedState.getBoolean("photoTaken");
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
        	
        	if (photoTaken && sample.getPhotoUri() != null) {
        		findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
				findViewById(R.id.new_sample_image_load).setVisibility(View.VISIBLE);
				
				final float scale = getResources().getDisplayMetrics().density;
				int imageWidth = (int)(THUMBNAIL_WIDTH * scale + 0.5f);
				AsyncImageLoader loader = new AsyncImageLoader(sample.getPhotoUri(), imageWidth, imgLoadHandler);
				loader.start();
        	}
        	
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
			Toast.makeText(getApplicationContext(), R.string.new_sample_save_success, Toast.LENGTH_SHORT).show();
		}
		finish();
	}
	
	public void onClickCancel(View view) {
		finish();
	}
	
	public void onLongClickChangeThumb(View view) {
		AlertDialog.Builder replaceImgBuilder = new AlertDialog.Builder(NewSampleActivity.this);
		//replaceImgBuilder.setIcon(icon);
        replaceImgBuilder.setTitle(R.string.new_sample_replace_img_title);
        replaceImgBuilder.setMessage(R.string.new_sample_replace_img_msg);
        replaceImgBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	if (sample.getPhotoUri() != null) {
            		File pic = new File(sample.getPhotoUri());
            		if (pic.delete()) {
            			sample.setPhotoUri(null);
            			findViewById(R.id.new_sample_thumb).setVisibility(View.GONE);
                		takePhoto();
            		}
            	}
            }
        });
        replaceImgBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        
        replaceImgBuilder.create().show();
	}
	
	public void onClickTakePhoto(View view) {
		takePhoto();
	}
	
	private void takePhoto() {
		//external storage is ready and writable - can be used
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File picDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			String picFilename = PICTURE_PREFIX + new SimpleDateFormat("yyyyMMddHHmmss").format(sample.getTimestamp()) + ".jpg";
			File pictureFile = new File(picDir, picFilename);
			try {
                if(pictureFile.exists() == false) {
                    pictureFile.getParentFile().mkdirs();
                    pictureFile.createNewFile();
                    photoUri = pictureFile.getAbsolutePath();
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
				sample.setPhotoUri(photoUri);
				photoUri = null;
			}
			else if (resultCode == Activity.RESULT_CANCELED) {
				photoTaken = false;
				if (sample.getPhotoUri() == null) {
					findViewById(R.id.new_sample_btn_photo).setVisibility(View.VISIBLE);
					findViewById(R.id.new_sample_image_load).setVisibility(View.GONE);
					findViewById(R.id.new_sample_thumb).setVisibility(View.GONE);
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
		savedState.putCharSequence("photoUri", sample.getPhotoUri());
		savedState.putBoolean("photoTaken", photoTaken);
		savedState.putBoolean("isEdit", isEdit);
	}
}
