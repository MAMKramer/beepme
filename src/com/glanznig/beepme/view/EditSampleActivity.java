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

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditSampleActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "EditSampleActivity";
	
	private Sample sample = new Sample();
	
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
        TagAutocompleteAdapter adapterKeyword = new TagAutocompleteAdapter(EditSampleActivity.this, R.layout.tag_autocomplete_list_row, 1);
    	autocompleteTags.setAdapter(adapterKeyword);
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
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		
		sample.setTitle(title.getText().toString());
		sample.setDescription(description.getText().toString());
		
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
		
		if (sample.getTimestamp() != null) {
			savedState.putLong("timestamp", sample.getTimestamp().getTime());
		}
		savedState.putLong("sampleId", sample.getId());
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putBoolean("accepted", sample.getAccepted());
		
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
	}

	@Override
	public void onClick(View v) {
		if (v.getParent() instanceof TagButtonContainer) {
			onClickRemoveTag(v);
		}
	}
}
