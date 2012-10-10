package com.glanznig.beeper;

import java.text.DateFormat;
import java.util.Date;

import com.glanznig.beeper.data.Sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class NewSampleActivity extends Activity {
	
	private Date sampleTimestamp;
	private long sampleId = 0L;
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.new_sample);
        
        sampleTimestamp = new Date();
        String title = null;
        String description = null;
        boolean accepted = false;
		
		if (savedState != null) {
			if (savedState.getLong("timestamp") != 0L) {
				sampleTimestamp = new Date(savedState.getLong("timestamp"));
			}
			if (savedState.getCharSequence("title") != null) {
				title = (String)savedState.getCharSequence("title");
			}
			if (savedState.getCharSequence("description") != null) {
				description = (String)savedState.getCharSequence("description");
			}
			if (savedState.getLong("sampleId") != 0L) {
				sampleId = savedState.getLong("sampleId");
			}
			accepted = savedState.getBoolean("accepted");
		}
		else {
			Bundle b = getIntent().getExtras();
			if (b != null) {
				sampleId = b.getLong("sampleId");
				BeeperApp app = (BeeperApp)getApplication();
				Sample s = app.getSample(sampleId);
				
				sampleTimestamp = s.getTimestamp();
				title = s.getTitle();
				description = s.getDescription();
				accepted = s.getAccepted();
			}
		}
		
		TextView timestamp = (TextView)findViewById(R.id.new_sample_timestamp);
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		timestamp.setText(dateFormat.format(sampleTimestamp));
		
		EditText titleWidget = (EditText)findViewById(R.id.new_sample_title);
		titleWidget.setText(title);
		
		EditText descriptionWidget = (EditText)findViewById(R.id.new_sample_description);
		descriptionWidget.setText(description);
		
		CheckBox acceptedWidget = (CheckBox)findViewById(R.id.new_sample_accepted);
		acceptedWidget.setChecked(accepted);
	}
	
	public void onClickSave(View view) {
		BeeperApp app = (BeeperApp)getApplication();
		Sample s;
		if (sampleId == 0L) {
			s = new Sample();
		}
		else {
			s = new Sample(sampleId);
		}
		
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		CheckBox accepted = (CheckBox)findViewById(R.id.new_sample_accepted);
		
		s.setTimestamp(sampleTimestamp);
		s.setTitle(title.getText().toString());
		s.setDescription(description.getText().toString());
		s.setAccepted(accepted.isChecked());
		
		if (sampleId == 0L) {
			app.addSample(s);
		}
		else {
			app.editSample(s);
		}
		finish();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		CheckBox accepted = (CheckBox)findViewById(R.id.new_sample_accepted);
		
		savedState.putLong("timestamp", sampleTimestamp.getTime());
		savedState.putLong("sampleId", sampleId);
		savedState.putCharSequence("title", title.getText());
		savedState.putCharSequence("description", description.getText());
		savedState.putBoolean("accepted", accepted.isChecked());
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.samples_list_menu, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent iSettings = new Intent(this, Preferences.class);
                startActivity(iSettings);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
