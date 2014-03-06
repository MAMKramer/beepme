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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.TagAutocompleteAdapter;
import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.data.SampleTable;
import com.glanznig.beepme.data.Tag;
import com.glanznig.beepme.helper.AsyncImageScaler;
import com.glanznig.beepme.helper.ImageHelper;
import com.glanznig.beepme.helper.ImageHelperCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NewSampleActivity extends Activity implements OnClickListener, ImageHelperCallback {
	
	private static final int THUMBNAIL_WIDTH = 80; //in dp
	private static final String TAG = "NewSampleActivity";
	
	private Sample sample = new Sample();
	private ImageHelper img = null;
	private boolean imgScalingRunning = false;
	
	private static class ImgLoadHandler extends Handler {
		WeakReference<NewSampleActivity> newSampleActivity;
		
		ImgLoadHandler(NewSampleActivity activity) {
			updateActivity(activity);
		}
		
		public void updateActivity(NewSampleActivity activity) {
			newSampleActivity = new WeakReference<NewSampleActivity>(activity);
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == AsyncImageScaler.BITMAP_MSG) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		if (newSampleActivity.get() != null) {
		    		if (imageBitmap != null) {
						View progressBar = newSampleActivity.get().findViewById(R.id.new_sample_image_load);
						if (progressBar != null) {
							progressBar.setVisibility(View.GONE);
						}
						ImageView imageView = (ImageView)newSampleActivity.get().findViewById(R.id.new_sample_thumb);
						if (imageView != null) {
							imageView.setImageBitmap(imageBitmap);
							imageView.setVisibility(View.VISIBLE);
						}
					}
					else {
						View progressBar = newSampleActivity.get().findViewById(R.id.new_sample_image_load);
						if (progressBar != null) {
							progressBar.setVisibility(View.GONE);
						}
					}
	    		}
	    	}
	    }
	}
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        
        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickDone(v);
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView);
        
        setContentView(R.layout.new_sample);
        SampleTable st = new SampleTable(this.getApplicationContext());
        
        ((ImageView)findViewById(R.id.new_sample_thumb)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                NewSampleActivity.this.onLongClickChangeThumb(view);
                return true;
            }
        });
		
		if (savedState != null) {
			if (savedState.getLong("sampleId") != 0L) {
				sample = st.getSampleWithTags(savedState.getLong("sampleId"));
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
			
			if (savedState.getCharSequence("keyword") != null) {
				EditText keyword = (EditText)findViewById(R.id.new_sample_add_keyword);
				keyword.setText(savedState.getCharSequence("keyword"));
			}
			
			if (savedState.getStringArrayList("tagList") != null) {
				Iterator<String> i = savedState.getStringArrayList("tagList").iterator();
				while (i.hasNext()) {
					Tag t = Tag.valueOf(i.next());
					if (t != null) {
						sample.addTag(t);
					}
				}
			}
			
			img = new ImageHelper(NewSampleActivity.this);
			if (savedState.getCharSequence("imgUri") != null) {
				img.setImageUri(savedState.getCharSequence("imgUri").toString());
				sample.setPhotoUri(savedState.getCharSequence("imgUri").toString());
			}
			
			sample.setAccepted(savedState.getBoolean("accepted"));
			imgScalingRunning = savedState.getBoolean("imgScalingRunning");
		}
		else {
			imgScalingRunning = false;
			img = new ImageHelper(NewSampleActivity.this);
			
			Bundle b = getIntent().getExtras();
			if (b != null) {
				if (b.containsKey(getApplication().getClass().getPackage().getName() + ".Timestamp")) {
					long timestamp = b.getLong(getApplication().getClass().getPackage().getName() + ".Timestamp");
					sample.setTimestamp(new Date(timestamp));
					sample.setAccepted(true);
					sample = st.addSample(sample);
				}
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		populateFields();
		
		if (imgScalingRunning) {
			ImageHelper.ImgScaleHandler handler = (ImageHelper.ImgScaleHandler)getLastNonConfigurationInstance();
	        if (handler != null) {
	        	handler.updateActivity(NewSampleActivity.this);
	        }
		}
		else {
			if (img.getImageUri() != null) {
				startThumbnailLoading();
			}
		}
	}
	
	private void startThumbnailLoading() {
		final float scale = getResources().getDisplayMetrics().density;
		int imageWidth = (int)(THUMBNAIL_WIDTH * scale + 0.5f);
		AsyncImageScaler loader = new AsyncImageScaler(img.getImageUri(), imageWidth, new ImgLoadHandler(NewSampleActivity.this));
		loader.start();
	}
	
	private void populateFields() {
		//check if device has camera feature
		if (!img.isEnabled()) {
			findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
		}
        
    	setTitle(R.string.new_sample);
    	
    	if (img.getImageUri() != null) {
    		findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
			findViewById(R.id.new_sample_image_load).setVisibility(View.VISIBLE);
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
        
        AutoCompleteTextView autocompleteTags = (AutoCompleteTextView)findViewById(R.id.new_sample_add_keyword);
        TagAutocompleteAdapter adapterKeywords = new TagAutocompleteAdapter(NewSampleActivity.this, R.layout.tag_autocomplete_list_row, 1);
    	autocompleteTags.setAdapter(adapterKeywords);
    	//after how many chars should auto-complete list appear?
    	autocompleteTags.setThreshold(2);
    	
    	TagButtonContainer keywordHolder = (TagButtonContainer)findViewById(R.id.new_sample_keyword_container);
    	keywordHolder.setVocabularyId(1);
    	Iterator<Tag> i = sample.getTags().iterator();
		Tag tag = null;
		
		while (i.hasNext()) {
			tag = i.next();
			if (tag.getVocabularyId() == 1) {
				keywordHolder.addTagButton(tag.getName(), this);
			}
		}
	}
	
	public void onClickAddKeyword(View view) {
		TagButtonContainer tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_keyword_container);
		EditText enteredTag = (EditText)findViewById(R.id.new_sample_add_keyword);
		if (enteredTag.getText().length() > 0) {
			Tag t = new Tag();
			t.setVocabularyId(tagHolder.getVocabularyId());
			t.setName(enteredTag.getText().toString().toLowerCase());
			if (sample.addTag(t)) {
				tagHolder.addTagButton(t.getName(), this);
				enteredTag.setText("");
			}
			else {
				Toast.makeText(getApplicationContext(), R.string.new_sample_add_tag_error, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void onClickRemoveTag(View view) {
		if (view instanceof TagButton) {
			TagButton btn = (TagButton)view;
			
			TagButtonContainer tagHolder = null;
			if (btn.getVocabularyId() == 1) {
				tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_keyword_container);
			}
			
			Tag t = new Tag();
			t.setName((btn.getText()).toString());
			t.setVocabularyId(btn.getVocabularyId());
			tagHolder.removeTagButton(btn);
			sample.removeTag(t);
		}
	}
	
	public void onClickDone(View view) {
		//Toast.makeText(getApplicationContext(), R.string.new_sample_save_success, Toast.LENGTH_SHORT).show();
		saveSample();
		finish();
	}
	
	public void saveSample() {
		BeeperApp app = (BeeperApp)getApplication();
		
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		
		sample.setTitle(title.getText().toString());
		sample.setDescription(description.getText().toString());
		sample.setPhotoUri(img.getImageUri());
		sample.setUptimeId(app.getPreferences().getUptimeId());
		
		// also save non-added keywords
		TagButtonContainer keywordTagHolder = (TagButtonContainer)findViewById(R.id.new_sample_keyword_container);
		EditText keyword = (EditText)findViewById(R.id.new_sample_add_keyword);
		if (keyword.getText().length() > 0) {
			Tag t = new Tag();
			t.setVocabularyId(keywordTagHolder.getVocabularyId());
			t.setName(keyword.getText().toString().toLowerCase());
			sample.addTag(t);
		}
		
		new SampleTable(this.getApplicationContext()).editSample(sample);
		
		app.setTimer();
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
            	if (img.deleteImage()) {
            		findViewById(R.id.new_sample_thumb).setVisibility(View.GONE);
            		takePhoto();
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
		Intent imgIntent = img.getIntent(sample.getTimestamp());
		if (imgIntent != null) {
			startActivityForResult(imgIntent, ImageHelper.TAKE_PICTURE);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ImageHelper.TAKE_PICTURE) {
			if (resultCode == Activity.RESULT_OK) {
				img.captureSuccess();
				sample.setPhotoUri(img.getImageUri());
				imgScalingRunning = true;
				img.scaleImage(NewSampleActivity.this);
			}
			else if (resultCode == Activity.RESULT_CANCELED) {
				img.setImageUri(null);
				findViewById(R.id.new_sample_btn_photo).setVisibility(View.VISIBLE);
				findViewById(R.id.new_sample_image_load).setVisibility(View.GONE);
				findViewById(R.id.new_sample_thumb).setVisibility(View.GONE);
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		EditText keyword = (EditText)findViewById(R.id.new_sample_add_keyword);
		
		if (sample.getTimestamp() != null) {
			savedState.putLong("timestamp", sample.getTimestamp().getTime());
		}
		savedState.putLong("sampleId", sample.getId());
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putBoolean("accepted", sample.getAccepted());
		savedState.putCharSequence("imgUri", img.getImageUri());
		
		if (keyword.getText().length() > 0) {
			savedState.putCharSequence("keyword", keyword.getText());
		}
		
		if (sample.getTags().size() > 0) {
			Iterator<Tag> i = sample.getTags().iterator(); 
			ArrayList<String> tags = new ArrayList<String>();
			
			while (i.hasNext()) {
				tags.add(i.next().toString());
			}
			savedState.putStringArrayList("tagList", tags);
		}
		
		savedState.putBoolean("imgScalingRunning", imgScalingRunning);
	}

	@Override
	public void onClick(View v) {
		if (v.getParent() instanceof TagButtonContainer) {
			onClickRemoveTag(v);
		}
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder sampleSavedBuilder = new AlertDialog.Builder(NewSampleActivity.this);
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
	
	@Override
	public void onStop() {
		super.onStop();
		if (NewSampleActivity.this.isFinishing()) {
			NewSampleActivity.this.saveSample();
		}
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
		if (imgScalingRunning) {
			return img.getImgScaleHandler();
		}
		
		return null;
    }

	@Override
	public void imageScalingCompleted() {
		imgScalingRunning = false;
		startThumbnailLoading();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.new_sample, menu);
        
        //check if device has camera feature
        MenuItem photoItem = menu.findItem(R.id.action_take_photo);
        if (!img.isEnabled()) {
        	photoItem.setVisible(false);
        }
        else {
        	photoItem.setVisible(true);
        }
        
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.action_take_photo:
        		Intent imgIntent = img.getIntent(sample.getTimestamp());
        		if (imgIntent != null) {
        			startActivityForResult(imgIntent, ImageHelper.TAKE_PICTURE);
        		}
        		
        		return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
