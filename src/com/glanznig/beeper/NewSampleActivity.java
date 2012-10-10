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
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.new_sample);
        
        sampleTimestamp = new Date();
		
		if (savedState != null) {
			if (savedState.getLong("timestamp") != 0L) {
				sampleTimestamp = new Date(savedState.getLong("timestamp"));
			}
			if (savedState.getCharSequence("title") != null) {
				EditText title = (EditText)findViewById(R.id.new_sample_title);
				title.setText(savedState.getCharSequence("title"));
			}
			if (savedState.getCharSequence("description") != null) {
				EditText description = (EditText)findViewById(R.id.new_sample_description);
				description.setText(savedState.getCharSequence("description"));
			}
			CheckBox accepted = (CheckBox)findViewById(R.id.new_sample_accepted);
			accepted.setChecked(savedState.getBoolean("accepted"));
		}        
		
		TextView timestamp = (TextView)findViewById(R.id.new_sample_timestamp);
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		timestamp.setText(dateFormat.format(sampleTimestamp));
		
	}
	
	public void onClickSave(View view) {
		BeeperApp app = (BeeperApp)getApplication();
		Sample s = new Sample();
		
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		CheckBox accepted = (CheckBox)findViewById(R.id.new_sample_accepted);
		
		s.setTimestamp(sampleTimestamp);
		s.setTitle(title.getText().toString());
		s.setDescription(description.getText().toString());
		s.setAccepted(accepted.isChecked());
		
		app.addSample(s);
		finish();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		TextView timestamp = (TextView)findViewById(R.id.new_sample_timestamp);
		EditText title = (EditText)findViewById(R.id.new_sample_title);
		EditText description = (EditText)findViewById(R.id.new_sample_description);
		CheckBox accepted = (CheckBox)findViewById(R.id.new_sample_accepted);
		
		savedState.putCharSequence("timestamp", timestamp.getText());
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
