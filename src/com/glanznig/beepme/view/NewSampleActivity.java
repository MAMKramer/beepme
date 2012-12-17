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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	
	private static final int THUMBNAIL_WIDTH = 80; //in dp
	private static final String TAG = "NewSampleActivity";
	
	private Sample sample = new Sample();
	private ImageHelper img = null;
	private boolean isEdit = false;
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
						NewSampleActivity nsa = newSampleActivity.get();
						if (nsa != null) {
							View progressBar = nsa.findViewById(R.id.new_sample_image_load);
							if (progressBar != null) {
								progressBar.setVisibility(View.GONE);
							}
						}
						ImageView imageView = (ImageView)newSampleActivity.get().findViewById(R.id.new_sample_thumb);
						if (imageView != null) {
							imageView.setImageBitmap(imageBitmap);
							imageView.setVisibility(View.VISIBLE);
						}
					}
					else {
						NewSampleActivity nsa = newSampleActivity.get();
						if (nsa != null) {
							View progressBar = nsa.findViewById(R.id.view_sample_image_load);
							if (progressBar != null) {
								progressBar.setVisibility(View.GONE);
							}
						}
					}
	    		}
	    	}
	    }
	}
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
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
			
			if (savedState.getStringArrayList("tagList") != null) {
				Iterator<String> i = savedState.getStringArrayList("tagList").iterator();
				while (i.hasNext()) {
					Tag t = new Tag();
					t.setName(i.next());
					sample.addTag(t);
				}
			}
			
			if (savedState.getCharSequence("img") != null) {
				img = new ImageHelper(NewSampleActivity.this);
				img.setImageUri(savedState.getCharSequence("img").toString());
			}
			
			lastTagId = savedState.getInt("tagId");
			
			sample.setAccepted(savedState.getBoolean("accepted"));
			
			isEdit = savedState.getBoolean("isEdit");
		}
		else {
			img = new ImageHelper(NewSampleActivity.this);
			
			Bundle b = getIntent().getExtras();
			if (b != null) {
				lastTagId = 0;
				
				if (b.containsKey(getApplication().getClass().getPackage().getName() + ".SampleId")) { 
					long sampleId = b.getLong(getApplication().getClass().getPackage().getName() + ".SampleId");
					sample = st.getSampleWithTags(sampleId);
					isEdit = true;
				}
				
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
		final float scale = getResources().getDisplayMetrics().density;
		
		//check if device has camera feature
		if (!img.isEnabled()) {
			findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
		}
        
		TagButtonContainer tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_tag_container);
		tagHolder.setLastTagId(lastTagId);
        
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
        	
        	if (img.getImageUri() != null) {
        		findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
				findViewById(R.id.new_sample_image_load).setVisibility(View.VISIBLE);
				
				int imageWidth = (int)(THUMBNAIL_WIDTH * scale + 0.5f);
				AsyncImageScaler loader = new AsyncImageScaler(img.getImageUri(), imageWidth, new ImgLoadHandler(NewSampleActivity.this));
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
				TagButtonContainer tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_tag_container);
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
		TagButtonContainer tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_tag_container);
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
		sample.setPhotoUri(img.getImageUri());
		new SampleTable(this.getApplicationContext()).editSample(sample);
		
		if (!isEdit) {
			app.setTimer();
		}
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
				img.scaleImage();
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
		
		if (sample.getTimestamp() != null) {
			savedState.putLong("timestamp", sample.getTimestamp().getTime());
		}
		savedState.putLong("sampleId", sample.getId());
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putBoolean("accepted", sample.getAccepted());
		savedState.putCharSequence("imgUri", img.getImageUri());
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
	
	@Override
	public void onStop() {
		super.onStop();
		if (NewSampleActivity.this.isFinishing()) {
			if (!isEdit) {
				NewSampleActivity.this.saveSample();
			}
		}
	}
}
