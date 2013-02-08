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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.glanznig.beepme.R;
import com.glanznig.beepme.TagAutocompleteAdapter;
import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.data.SampleTable;
import com.glanznig.beepme.data.Tag;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditSampleActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "EditSampleActivity";
	
	private Sample sample = new Sample();
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.new_sample);
        SampleTable st = new SampleTable(this.getApplicationContext());
		
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
			
			if (savedState.getCharSequence("presence") != null) {
				sample.setPresence(savedState.getCharSequence("presence").toString());
			}
			
			if (savedState.getCharSequence("mood") != null) {
				EditText mood = (EditText)findViewById(R.id.new_sample_add_mood);
				mood.setText(savedState.getCharSequence("mood"));
			}
			
			if (savedState.getCharSequence("attitude") != null) {
				EditText attitude = (EditText)findViewById(R.id.new_sample_add_attitude);
				attitude.setText(savedState.getCharSequence("attitude"));
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
			
			sample.setAccepted(savedState.getBoolean("accepted"));
		}
		else {
			Bundle b = getIntent().getExtras();
			if (b != null) {
				if (b.containsKey(getApplication().getClass().getPackage().getName() + ".SampleId")) { 
					long sampleId = b.getLong(getApplication().getClass().getPackage().getName() + ".SampleId");
					sample = st.getSampleWithTags(sampleId);
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
        
        AutoCompleteTextView presenceWidget = (AutoCompleteTextView)findViewById(R.id.new_sample_presence);
        if (sample.getPresence() != null) {
        	presenceWidget.setText(sample.getPresence());
        }
        TagAutocompleteAdapter adapterPresence = new TagAutocompleteAdapter(EditSampleActivity.this, R.layout.tag_autocomplete_list_row, 3);
    	presenceWidget.setAdapter(adapterPresence);
    	//after how many chars should auto-complete list appear?
    	presenceWidget.setThreshold(2);
        
        AutoCompleteTextView autocompleteMoodTags = (AutoCompleteTextView)findViewById(R.id.new_sample_add_mood);
        TagAutocompleteAdapter adapterMood = new TagAutocompleteAdapter(EditSampleActivity.this, R.layout.tag_autocomplete_list_row, 1);
    	autocompleteMoodTags.setAdapter(adapterMood);
    	//after how many chars should auto-complete list appear?
    	autocompleteMoodTags.setThreshold(2);
    	
    	AutoCompleteTextView autocompleteAttitudeTags = (AutoCompleteTextView)findViewById(R.id.new_sample_add_attitude);
        TagAutocompleteAdapter adapterAttitude = new TagAutocompleteAdapter(EditSampleActivity.this, R.layout.tag_autocomplete_list_row, 2);
    	autocompleteAttitudeTags.setAdapter(adapterAttitude);
    	//after how many chars should auto-complete list appear?
    	autocompleteAttitudeTags.setThreshold(2);
    	
    	TagButtonContainer moodHolder = (TagButtonContainer)findViewById(R.id.new_sample_mood_container);
    	TagButtonContainer attitudeHolder = (TagButtonContainer)findViewById(R.id.new_sample_attitude_container);
    	moodHolder.setVocabularyId(1);
    	attitudeHolder.setVocabularyId(2);
    	Iterator<Tag> i = sample.getTags().iterator();
		Tag tag = null;
		
		while (i.hasNext()) {
			tag = i.next();
			if (tag.getVocabularyId() == 1) {
				moodHolder.addTagButton(tag.getName(), this);
			}
			if (tag.getVocabularyId() == 2) {
				attitudeHolder.addTagButton(tag.getName(), this);
			}
		}
		
		if (sample.getTimerProfileId() == 1) {
			findViewById(R.id.new_sample_label_attitudes).setVisibility(View.GONE);
			findViewById(R.id.new_sample_add_attitude).setVisibility(View.GONE);
			findViewById(R.id.new_sample_btn_add_attitude).setVisibility(View.GONE);
			findViewById(R.id.new_sample_help_attitudes).setVisibility(View.GONE);
			findViewById(R.id.new_sample_attitude_container).setVisibility(View.GONE);
		}
	}
	
	public void onClickAddMood(View view) {
		TagButtonContainer tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_mood_container);
		EditText enteredTag = (EditText)findViewById(R.id.new_sample_add_mood);
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
	
	public void onClickAddAttitude(View view) {
		TagButtonContainer tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_attitude_container);
		EditText enteredTag = (EditText)findViewById(R.id.new_sample_add_attitude);
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
				tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_mood_container);
			}
			if (btn.getVocabularyId() == 2) {
				tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_attitude_container);
			}
			
			Tag t = new Tag();
			t.setName((btn.getText()).toString());
			t.setVocabularyId(btn.getVocabularyId());
			tagHolder.removeTagButton(btn);
			sample.removeTag(t);
		}
	}
	
	public void onClickSave(View view) {
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		EditText presence = (EditText)findViewById(R.id.new_sample_presence);
		
		sample.setTitle(title.getText().toString());
		sample.setDescription(description.getText().toString());
		sample.setPresence(presence.getText().toString());
		
		// also save non-added keywords
		TagButtonContainer moodTagHolder = (TagButtonContainer)findViewById(R.id.new_sample_mood_container);
		EditText mood = (EditText)findViewById(R.id.new_sample_add_mood);
		if (mood.getText().length() > 0) {
			Tag t = new Tag();
			t.setVocabularyId(moodTagHolder.getVocabularyId());
			t.setName(mood.getText().toString().toLowerCase());
			sample.addTag(t);
		}
		
		TagButtonContainer attitudeTagHolder = (TagButtonContainer)findViewById(R.id.new_sample_attitude_container);
		EditText attitude = (EditText)findViewById(R.id.new_sample_add_attitude);
		if (attitude.getText().length() > 0) {
			Tag t = new Tag();
			t.setVocabularyId(attitudeTagHolder.getVocabularyId());
			t.setName(attitude.getText().toString().toLowerCase());
			sample.addTag(t);
		}
		
		new SampleTable(this.getApplicationContext()).editSample(sample);
		
		finish();
	}
	
	public void onClickCancel(View view) {
		finish();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		EditText presence = (EditText)findViewById(R.id.new_sample_presence);
		EditText mood = (EditText)findViewById(R.id.new_sample_add_mood);
		EditText attitude = (EditText)findViewById(R.id.new_sample_add_attitude);
		
		if (sample.getTimestamp() != null) {
			savedState.putLong("timestamp", sample.getTimestamp().getTime());
		}
		savedState.putLong("sampleId", sample.getId());
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putCharSequence("presence", presence.getText());
		savedState.putBoolean("accepted", sample.getAccepted());
		
		if (mood.getText().length() > 0) {
			savedState.putCharSequence("mood", mood.getText());
		}
		if (attitude.getText().length() > 0) {
			savedState.putCharSequence("attitude", attitude.getText());
		}
		
		if (sample.getTags().size() > 0) {
			Iterator<Tag> i = sample.getTags().iterator(); 
			ArrayList<String> tags = new ArrayList<String>();
			
			while (i.hasNext()) {
				tags.add(i.next().toString());
			}
			savedState.putStringArrayList("tagList", tags);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getParent() instanceof TagButtonRow) {
			onClickRemoveTag(v);
		}
	}
}
