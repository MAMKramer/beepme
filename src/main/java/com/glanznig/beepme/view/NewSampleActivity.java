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
http://beepme.yourexp.at
*/

package com.glanznig.beepme.view;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.TagAutocompleteAdapter;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.VocabularyItem;
import com.glanznig.beepme.data.db.MomentTable;
import com.glanznig.beepme.helper.AsyncImageScaler;
import com.glanznig.beepme.helper.PhotoUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class NewSampleActivity extends Activity implements OnClickListener, PopupMenu.OnMenuItemClickListener, Callback {
	
	private static final String TAG = "NewSampleActivity";
	
	private Moment sample = new Moment();
	private SamplePhotoView photoView = null;
	
	private static class ImgLoadHandler extends Handler {
		WeakReference<SamplePhotoView> view;
		
		ImgLoadHandler(SamplePhotoView view) {
			this.view = new WeakReference<SamplePhotoView>(view);
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == PhotoUtils.MSG_PHOTO_LOADED) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		
	    		if (view.get() != null && imageBitmap != null) {
	    			view.get().setPhoto(imageBitmap);
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
        MomentTable st = new MomentTable(this.getApplicationContext());
		
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
			
			if (savedState.getCharSequence("imgUri") != null) {
				sample.setPhotoUri(savedState.getCharSequence("imgUri").toString());
			}
			
			if (savedState.getCharSequence("keyword") != null) {
				EditText keyword = (EditText)findViewById(R.id.new_sample_add_keyword);
				keyword.setText(savedState.getCharSequence("keyword"));
			}
			
			if (savedState.getStringArrayList("tagList") != null) {
				Iterator<String> i = savedState.getStringArrayList("tagList").iterator();
				while (i.hasNext()) {
					VocabularyItem t = VocabularyItem.valueOf(i.next());
					if (t != null) {
						sample.addTag(t);
					}
				}
			}
		}
		else {
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
	}
	
	private void populateFields() {
		photoView = (SamplePhotoView)findViewById(R.id.new_sample_photoview);
		//check if device has camera feature
		if (!PhotoUtils.isEnabled(NewSampleActivity.this)) {
			photoView.setVisibility(View.GONE);
		}
		else {
			photoView.setVisibility(View.VISIBLE);
			photoView.setOnMenuItemClickListener(NewSampleActivity.this);
			
			String thumbnailUri = PhotoUtils.getThumbnailUri(sample.getPhotoUri(), 48);
			if (thumbnailUri != null) {
				File thumb = new File(thumbnailUri);
				if (thumb.exists()) {
					ImgLoadHandler handler = new ImgLoadHandler(photoView);
					PhotoUtils.getAsyncBitmap(NewSampleActivity.this, thumbnailUri, handler);
				}
			}
		}
        
    	setTitle(R.string.new_sample);
        
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
    	Iterator<VocabularyItem> i = sample.getTags().iterator();
		VocabularyItem tag = null;
		
		while (i.hasNext()) {
			tag = i.next();
			if (tag.getVocabularyUid() == 1) {
				keywordHolder.addTagButton(tag.getName(), this);
			}
		}
	}
	
	public void onClickAddKeyword(View view) {
		TagButtonContainer tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_keyword_container);
		EditText enteredTag = (EditText)findViewById(R.id.new_sample_add_keyword);
		if (enteredTag.getText().length() > 0) {
			VocabularyItem t = new VocabularyItem();
			t.setVocabularyUid(tagHolder.getVocabularyId());
			t.setName(enteredTag.getText().toString().toLowerCase(Locale.getDefault()));
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
			
			VocabularyItem t = new VocabularyItem();
			t.setName((btn.getText()).toString());
			t.setVocabularyUid(btn.getVocabularyId());
			tagHolder.removeTagButton(btn);
			sample.removeTag(t);
		}
	}
	
	public void onClickDone(View view) {
		saveSample();
		finish();
	}
	
	public void saveSample() {
		BeepMeApp app = (BeepMeApp)getApplication();
		
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		
		sample.setTitle(title.getText().toString());
		sample.setDescription(description.getText().toString());
		sample.setUptimeUid(app.getCurrentUptime().getUid());
		
		// also save non-added keywords
		TagButtonContainer keywordTagHolder = (TagButtonContainer)findViewById(R.id.new_sample_keyword_container);
		EditText keyword = (EditText)findViewById(R.id.new_sample_add_keyword);
		if (keyword.getText().length() > 0) {
			VocabularyItem t = new VocabularyItem();
			t.setVocabularyUid(keywordTagHolder.getVocabularyId());
			t.setName(keyword.getText().toString().toLowerCase(Locale.getDefault()));
			sample.addTag(t);
		}
		
		new MomentTable(this.getApplicationContext()).editSample(sample);
		
		app.scheduleBeep();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
			case PhotoUtils.TAKE_PHOTO_INTENT:
				if (resultCode == Activity.RESULT_OK) {
					Handler handler = new Handler(NewSampleActivity.this);
					PhotoUtils.generateThumbnails(NewSampleActivity.this, sample.getPhotoUri(), handler);
				}
				else {
					sample.setPhotoUri(null);
				}
				break;
			
			case PhotoUtils.CHANGE_PHOTO_INTENT:
				if (resultCode == Activity.RESULT_OK) {
					if (PhotoUtils.swapPhoto(NewSampleActivity.this, sample.getTimestamp())) {
						Handler handler = new Handler(NewSampleActivity.this);
						PhotoUtils.regenerateThumbnails(NewSampleActivity.this, sample.getPhotoUri(), handler);
					}
				}
				else {
					PhotoUtils.deleteSwapPhoto(NewSampleActivity.this);
				}
				break;
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
		savedState.putCharSequence("imgUri", sample.getPhotoUri());
		
		if (keyword.getText().length() > 0) {
			savedState.putCharSequence("keyword", keyword.getText());
		}
		
		if (sample.getTags().size() > 0) {
			Iterator<VocabularyItem> i = sample.getTags().iterator();
			ArrayList<String> tags = new ArrayList<String>();
			
			while (i.hasNext()) {
				tags.add(i.next().toString());
			}
			savedState.putStringArrayList("tagList", tags);
		}
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
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_take_photo:
				
				Intent takePhoto = PhotoUtils.getTakePhotoIntent(NewSampleActivity.this, sample.getTimestamp());
				
				if (takePhoto != null) {
					Bundle extras = takePhoto.getExtras();
					Uri photoUri = (Uri)extras.get(PhotoUtils.EXTRA_KEY);
					sample.setPhotoUri(photoUri.getPath());
					
					startActivityForResult(takePhoto, PhotoUtils.TAKE_PHOTO_INTENT);
				}
				break;
				
			case R.id.action_change_photo:
				
				Intent changePhoto = PhotoUtils.getChangePhotoIntent(NewSampleActivity.this);
				
				if (changePhoto != null) {
					startActivityForResult(changePhoto, PhotoUtils.CHANGE_PHOTO_INTENT);
				}
				break;
				
			case R.id.action_delete_photo:
				
				AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(NewSampleActivity.this);
		        deleteBuilder.setTitle(R.string.photo_delete_warning_title);
		        deleteBuilder.setMessage(R.string.photo_delete_warning_msg);
		        deleteBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            	// delete photo on storage
		            	PhotoUtils.deletePhoto(NewSampleActivity.this, sample.getPhotoUri());
		            	
		            	sample.setPhotoUri(null);
		            	photoView.unsetPhoto();
		            }
		        });
		        deleteBuilder.setNegativeButton(R.string.no, null);
		        deleteBuilder.create().show();
		        break;
		}
		return true;
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == AsyncImageScaler.MSG_SUCCESS) {
			Bitmap photoBitmap = (Bitmap)msg.obj;
			if (photoBitmap != null && msg.arg1 == 48) {
				photoView.setPhoto(photoBitmap);
				return true;
			}
		}
		
		if (msg.what == AsyncImageScaler.MSG_ERROR) {
			// error handling
		}
		
		return false;
	}
}
