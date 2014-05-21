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

import com.glanznig.beepme.R;
import com.glanznig.beepme.view.input.TagAutocompleteAdapter;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.VocabularyItem;
import com.glanznig.beepme.data.db.MomentTable;
import com.glanznig.beepme.helper.AsyncImageScaler;
import com.glanznig.beepme.helper.PhotoUtils;
import com.glanznig.beepme.view.input.TagControl;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class EditSampleActivity extends Activity implements OnClickListener, PopupMenu.OnMenuItemClickListener, Callback {
	
	private static final String TAG = "EditSampleActivity";
	
	private Moment moment = new Moment();
	private SamplePhotoView photoView;
	
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
	    		else if (view.get() != null) {
	    			view.get().setVisibility(View.GONE);
	    		}
	    	}
	    	
	    	if (msg.what == PhotoUtils.MSG_PHOTO_LOAD_ERROR && view.get() != null) {
	    		view.get().setVisibility(View.GONE);
	    	}
	    }
	}
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        
        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_done_cancel, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickDone(v);
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickCancel(v);
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
     
        setContentView(R.layout.new_sample);
        MomentTable momentTable = new MomentTable(this.getApplicationContext());
		
		if (savedState != null) {
			if (savedState.getLong("sampleId") != 0L) {
				moment = momentTable.getMomentWithValues(savedState.getLong("sampleId"));
			}
			
			if (savedState.getLong("timestamp") != 0L) {
				moment.setTimestamp(new Date(savedState.getLong("timestamp")));
			}
			
			if (savedState.getCharSequence("title") != null) {
				moment.setTitle(savedState.getCharSequence("title").toString());
			}
			
			if (savedState.getCharSequence("description") != null) {
				moment.setDescription(savedState.getCharSequence("description").toString());
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
						moment.addTag(t);
					}
				}
			}
			
			moment.setAccepted(savedState.getBoolean("accepted"));
		}
		else {
			Bundle b = getIntent().getExtras();
			if (b != null) {
				if (b.containsKey(getApplication().getClass().getPackage().getName() + ".SampleId")) { 
					long momentId = b.getLong(getApplication().getClass().getPackage().getName() + ".SampleId");
					moment = momentTable.getMomentWithValues(momentId);
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
		//findViewById(R.id.new_sample_btn_photo).setVisibility(View.GONE);
		photoView = (SamplePhotoView)findViewById(R.id.new_sample_photoview);
		photoView.setRights(false, true);
		photoView.setOnMenuItemClickListener(this);
		
		String thumbnailUri = PhotoUtils.getThumbnailUri(moment.getPhotoUri(), 48);
		if (thumbnailUri != null) {
			File thumb = new File(thumbnailUri);
			if (thumb.exists()) {
				ImgLoadHandler handler = new ImgLoadHandler(photoView);
				PhotoUtils.getAsyncBitmap(EditSampleActivity.this, thumbnailUri, handler);
			}
			else {
				Handler handler = new Handler(EditSampleActivity.this);
				photoView.measure(0, 0);
				PhotoUtils.generateThumbnail(EditSampleActivity.this, moment.getPhotoUri(), 48, photoView.getMeasuredWidth(), handler);
				photoView.setVisibility(View.GONE);
			}
		}
		else {
			photoView.setVisibility(View.GONE);
		}
        
        if (moment.getTimestamp() != null) {
        	TextView timestamp = (TextView)findViewById(R.id.new_sample_timestamp);
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			timestamp.setText(dateFormat.format(moment.getTimestamp()));
        }
		
        TextView titleHead = (TextView)findViewById(R.id.new_sample_show_title);
        if (moment.getTitle() != null && moment.getTitle().length() > 0) {
        	EditText titleWidget = (EditText)findViewById(R.id.new_sample_title);
        	titleWidget.setText(moment.getTitle());
        	
        	titleHead.setText(moment.getTitle());
        }
        else {
        	titleHead.setText(R.string.sample_untitled);
        }
		
        if (moment.getDescription() != null) {
        	EditText descriptionWidget = (EditText)findViewById(R.id.new_sample_description);
        	descriptionWidget.setText(moment.getDescription());
        }
        
        final AutoCompleteTextView autocompleteTags = (AutoCompleteTextView)findViewById(R.id.new_sample_add_keyword);
        TagAutocompleteAdapter adapterKeyword = new TagAutocompleteAdapter(EditSampleActivity.this, R.layout.tag_autocomplete_list_row, 1);
    	autocompleteTags.setAdapter(adapterKeyword);
    	//after how many chars should auto-complete list appear?
    	autocompleteTags.setThreshold(2);
    	
    	TagControl keywordHolder = (TagControl)findViewById(R.id.new_sample_keyword_container);
    	keywordHolder.setVocabularyUid(1);
    	Iterator<VocabularyItem> i = moment.getTags().iterator();
		VocabularyItem tag = null;
		
		while (i.hasNext()) {
			tag = i.next();
			if (tag.getVocabularyUid() == 1) {
				keywordHolder.addTagButton(tag.getName(), this);
			}
		}
	}
	
	public void onClickAddKeyword(View view) {
		TagControl tagHolder = (TagControl)findViewById(R.id.new_sample_keyword_container);
		EditText enteredTag = (EditText)findViewById(R.id.new_sample_add_keyword);
		if (enteredTag.getText().length() > 0) {
			VocabularyItem t = new VocabularyItem();
			t.setVocabularyUid(tagHolder.getVocabularyUid());
			t.setName(enteredTag.getText().toString().toLowerCase(Locale.getDefault()));
			if (moment.addTag(t)) {
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
			
			TagControl tagHolder = null;
			if (btn.getVocabularyId() == 1) {
				tagHolder = (TagControl)findViewById(R.id.new_sample_keyword_container);
			}
			
			VocabularyItem t = new VocabularyItem();
			t.setName((btn.getText()).toString());
			t.setVocabularyUid(btn.getVocabularyId());
			tagHolder.removeTagButton(btn);
			moment.removeTag(t);
		}
	}
	
	public void onClickDone(View view) {
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		
		moment.setTitle(title.getText().toString());
		moment.setDescription(description.getText().toString());
		
		// also save non-added keywords
		TagControl keywordTagHolder = (TagControl)findViewById(R.id.new_sample_keyword_container);
		EditText keyword = (EditText)findViewById(R.id.new_sample_add_keyword);
		if (keyword.getText().length() > 0) {
			VocabularyItem t = new VocabularyItem();
			t.setVocabularyUid(keywordTagHolder.getVocabularyUid());
			t.setName(keyword.getText().toString().toLowerCase(Locale.getDefault()));
			moment.addTag(t);
		}
		
		new MomentTable(this.getApplicationContext()).editSample(moment);
		
		finish();
	}
	
	public void onClickCancel(View view) {
		finish();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		EditText keyword = (EditText)findViewById(R.id.new_sample_add_keyword);
		
		if (moment.getTimestamp() != null) {
			savedState.putLong("timestamp", moment.getTimestamp().getTime());
		}
		savedState.putLong("sampleId", moment.getId());
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putBoolean("accepted", moment.getAccepted());
		
		if (keyword.getText().length() > 0) {
			savedState.putCharSequence("keyword", keyword.getText());
		}
		
		if (moment.getTags().size() > 0) {
			Iterator<VocabularyItem> i = moment.getTags().iterator();
			ArrayList<String> tags = new ArrayList<String>();
			
			while (i.hasNext()) {
				tags.add(i.next().toString());
			}
			savedState.putStringArrayList("tagList", tags);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getParent() instanceof TagControl) {
			onClickRemoveTag(v);
		}
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (item.getItemId() == R.id.action_delete_photo) {
			AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(EditSampleActivity.this);
	        deleteBuilder.setTitle(R.string.photo_delete_warning_title);
	        deleteBuilder.setMessage(R.string.photo_delete_warning_msg);
	        deleteBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	String photoUri = moment.getPhotoUri();
	            	MomentTable st = new MomentTable(getApplicationContext());
	            	
	            	moment.setPhotoUri(null);
	            	// save immediately to prevent orphan uris when leaving with dismiss
	            	st.editSample(moment);
	            	
	            	// delete photo on storage
	            	PhotoUtils.deletePhoto(EditSampleActivity.this, photoUri);
	            	
	            	photoView.unsetPhoto();
	            }
	        });
	        deleteBuilder.setNegativeButton(R.string.no, null);
	        deleteBuilder.create().show();
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == AsyncImageScaler.MSG_SUCCESS) {
			Bitmap photoBitmap = (Bitmap)msg.obj;
			if (photoBitmap != null && msg.arg1 == 48) {
				photoView.setPhoto(photoBitmap);
				if (!photoView.isPhotoSet()) {
					photoView.setVisibility(View.GONE);
				}
				else {
					photoView.setVisibility(View.VISIBLE);
				}
				return true;
			}
		}
		
		if (msg.what == AsyncImageScaler.MSG_ERROR) {
			// error handling
			photoView.setVisibility(View.GONE);
		}
		
		return false;
	}
}
