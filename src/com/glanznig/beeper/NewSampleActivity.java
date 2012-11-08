package com.glanznig.beeper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.glanznig.beeper.data.Sample;
import com.glanznig.beeper.data.Tag;
import com.glanznig.beeper.helper.AsyncImageScaler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NewSampleActivity extends Activity implements OnClickListener {
	
	private static final int TAKE_PICTURE = 42;
	private static final int THUMBNAIL_HEIGHT = 120; //in dp
	private static final int THUMBNAIL_WIDTH = 80; //in dp
	private static final int IMG_MAX_DIM = 1024; //in px
	private static final int IMG_QUALITY = 75;
	private static final String PICTURE_PREFIX = "beeper_img_";
	private static final String TAG = "newSampleActivity";
	private static final int ID_TAG_HOLDER = 14653;
	
	private Sample sample = new Sample();
	private String photoUri = null;
	private boolean isEdit = false;
	private boolean photoTaken = false;
	private int lastTagId = 0;
	
	private static class ImgLoadHandler extends Handler {
		WeakReference<NewSampleActivity> newSampleActivity;
		
		ImgLoadHandler(NewSampleActivity activity) {
			newSampleActivity = new WeakReference<NewSampleActivity>(activity);
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == AsyncImageScaler.BITMAP_MSG) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		if (newSampleActivity != null) {
		    		if (imageBitmap != null) {
						newSampleActivity.get().findViewById(R.id.new_sample_image_load).setVisibility(View.GONE);
						ImageView imageView = (ImageView)newSampleActivity.get().findViewById(R.id.new_sample_thumb);
						imageView.setImageBitmap(imageBitmap);
						imageView.setVisibility(View.VISIBLE);
					}
					else {
						newSampleActivity.get().findViewById(R.id.view_sample_image_load).setVisibility(View.GONE);
					}
	    		}
	    	}
	    }
	}
	
	private static class ImgScaleHandler extends Handler {
		WeakReference<Sample> sample;
		
		ImgScaleHandler(Sample sample) {
			this.sample = new WeakReference<Sample>(sample);
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == AsyncImageScaler.BITMAP_MSG) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		if (sample != null) {
	    			//save downscaled image to file
	    			if (imageBitmap != null) {
	    				try {
							FileOutputStream outStream = new FileOutputStream(sample.get().getPhotoUri());
							imageBitmap.compress(CompressFormat.JPEG, IMG_QUALITY, outStream);
						} catch (Exception e) {
							Log.e(TAG, "error while writing downscaled image", e);
						}
	    			}
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
				BeeperApp app = (BeeperApp)getApplication();
				sample = app.getDataStore().getSampleWithTags(savedState.getLong("sampleId"));
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
			
			if (savedState.getStringArrayList("tagList") != null) {
				Iterator<String> i = savedState.getStringArrayList("tagList").iterator();
				while (i.hasNext()) {
					Tag t = new Tag();
					t.setName(i.next());
					sample.addTag(t);
				}
			}
			
			lastTagId = savedState.getInt("tagId");
			
			sample.setAccepted(savedState.getBoolean("accepted"));
			
			isEdit = savedState.getBoolean("isEdit");
			photoTaken = savedState.getBoolean("photoTaken");
		}
		else {
			Bundle b = getIntent().getExtras();
			if (b != null) {
				BeeperApp app = (BeeperApp)getApplication();
				lastTagId = 0;
				
				if (b.containsKey(getApplication().getClass().getPackage().getName() + ".SampleId")) { 
					long sampleId = b.getLong(getApplication().getClass().getPackage().getName() + ".SampleId");
					sample = app.getDataStore().getSampleWithTags(sampleId);
					isEdit = true;
				}
				
				if (b.containsKey(getApplication().getClass().getPackage().getName() + ".Timestamp")) {
					long timestamp = b.getLong(getApplication().getClass().getPackage().getName() + ".Timestamp");
					sample.setTimestamp(new Date(timestamp));
					sample.setAccepted(true);
					sample = app.getDataStore().addSample(sample);
				}
			}
		}
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
        
        LinearLayout baseLayout = (LinearLayout)findViewById(R.id.new_sample_layout);
		TagButtonContainer tagHolder = new TagButtonContainer(NewSampleActivity.this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		final float scale = getResources().getDisplayMetrics().density;
		lp.setMargins(0, (int)(10 * scale + 0.5f), 0, 0);
		tagHolder.setLayoutParams(lp);
		tagHolder.setId(ID_TAG_HOLDER);
		tagHolder.setLastTagId(lastTagId);
		baseLayout.addView(tagHolder, 5);
        
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
				
				int imageWidth = (int)(THUMBNAIL_WIDTH * scale + 0.5f);
				AsyncImageScaler loader = new AsyncImageScaler(sample.getPhotoUri(), imageWidth, new ImgLoadHandler(NewSampleActivity.this));
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
        
        AutoCompleteTextView autocompleteTags = (AutoCompleteTextView)findViewById(R.id.new_sample_add_tag);
        TagAutocompleteAdapter adapter = new TagAutocompleteAdapter(NewSampleActivity.this, R.layout.tag_autocomplete_list_row);
    	autocompleteTags.setAdapter(adapter);
    	//after how many chars should auto-complete list appear?
    	autocompleteTags.setThreshold(2);
    	//autocompleteTags.setMaxLines(5);
    	
    	Iterator<Tag> i = sample.getTags().iterator();
		Tag tag = null;
		
		while (i.hasNext()) {
			tag = i.next();
			tagHolder.addTagButton(tag.getName(), this);
		}
	}
	
	public void onClickAddTag(View view) {
		EditText enteredTag = (EditText)findViewById(R.id.new_sample_add_tag);
		if (enteredTag.getText().length() > 0) {
			Tag t = new Tag();
			t.setName(enteredTag.getText().toString().toLowerCase());
			if (sample.addTag(t)) {
				TagButtonContainer tagHolder = (TagButtonContainer)findViewById(ID_TAG_HOLDER);
				tagHolder.addTagButton(t.getName(), this);
				enteredTag.setText("");
			}
			else {
				Toast.makeText(getApplicationContext(), R.string.new_sample_add_tag_error, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void onClickRemoveTag(View view) {
		Tag t = new Tag();
		t.setName(((Button)view).getText().toString());
		TagButtonContainer tagHolder = (TagButtonContainer)findViewById(ID_TAG_HOLDER);
		tagHolder.removeTagButton((Button)view);
		sample.removeTag(t);
	}
	
	public void onClickSave(View view) {
		saveSample();
		
		if (!isEdit) {
			Toast.makeText(getApplicationContext(), R.string.new_sample_save_success, Toast.LENGTH_SHORT).show();
		}
		finish();
	}
	
	public void saveSample() {
		BeeperApp app = (BeeperApp)getApplication();
		
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		
		sample.setTitle(title.getText().toString());
		sample.setDescription(description.getText().toString());
		app.getDataStore().editSample(sample);
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
        replaceImgBuilder.setNegativeButton(R.string.no, null);
        
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
				sample.setPhotoUri(photoUri);
				AsyncImageScaler loader = new AsyncImageScaler(sample.getPhotoUri(), IMG_MAX_DIM, new ImgScaleHandler(sample));
				loader.start();
				photoTaken = true;
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
		
		if (sample.getTimestamp() != null) {
			savedState.putLong("timestamp", sample.getTimestamp().getTime());
		}
		savedState.putLong("sampleId", sample.getId());
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putBoolean("accepted", sample.getAccepted());
		savedState.putCharSequence("photoUri", sample.getPhotoUri());
		savedState.putBoolean("photoTaken", photoTaken);
		savedState.putBoolean("isEdit", isEdit);
		
		if (sample.getTags().size() > 0) {
			Iterator<Tag> i = sample.getTags().iterator(); 
			ArrayList<String> tags = new ArrayList<String>();
			
			while (i.hasNext()) {
				tags.add(i.next().getName());
			}
			savedState.putStringArrayList("tagList", tags);
		}
		
		savedState.putInt("tagId", lastTagId);
	}

	@Override
	public void onClick(View v) {
		if (v.getParent() instanceof TagButtonRow) {
			onClickRemoveTag(v);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (!isEdit) {
			AlertDialog.Builder sampleSavedBuilder = new AlertDialog.Builder(NewSampleActivity.this);
			//sampleSavedBuilder.setIcon(icon);
	        sampleSavedBuilder.setTitle(R.string.new_sample_back_warning_title);
	        sampleSavedBuilder.setMessage(R.string.new_sample_back_warning_msg);
	        sampleSavedBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	NewSampleActivity.this.saveSample();
	            	NewSampleActivity.this.finish();
	            }
	        });
	        sampleSavedBuilder.setNegativeButton(R.string.no, null);
	        
	        sampleSavedBuilder.create().show();
		}
		else {
			super.onBackPressed();
		}
	}
}
