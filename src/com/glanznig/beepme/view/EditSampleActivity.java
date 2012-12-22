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
	private int lastTagId = 0;
	
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
		}
		else {
			Bundle b = getIntent().getExtras();
			if (b != null) {
				lastTagId = 0;
				
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
		TagButtonContainer tagHolder = (TagButtonContainer)findViewById(R.id.new_sample_tag_container);
		tagHolder.setLastTagId(lastTagId);
        
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
        
        AutoCompleteTextView autocompleteTags = (AutoCompleteTextView)findViewById(R.id.new_sample_add_tag);
        TagAutocompleteAdapter adapter = new TagAutocompleteAdapter(EditSampleActivity.this, R.layout.tag_autocomplete_list_row);
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
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		
		sample.setTitle(title.getText().toString());
		sample.setDescription(description.getText().toString());
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
		
		if (sample.getTimestamp() != null) {
			savedState.putLong("timestamp", sample.getTimestamp().getTime());
		}
		savedState.putLong("sampleId", sample.getId());
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putBoolean("accepted", sample.getAccepted());
		
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
}
