package com.glanznig.beeper;

import java.text.DateFormat;

import com.glanznig.beeper.data.Sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class ViewSampleActivity extends Activity {
	
	private long sampleId;
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.view_sample);
        
        sampleId = 0L;
		
		if (savedState != null) {
			if (savedState.getLong("sampleId") != 0L) {
				sampleId = savedState.getLong("sampleId");
			}
		}
		else {
			Bundle b = getIntent().getExtras();
			if (b != null) {
				sampleId = b.getLong("sampleId");
			}
		}
		
		populateFields();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		populateFields();
	}
	
	private void populateFields() {
		if (sampleId != 0L) {
			BeeperApp app = (BeeperApp)getApplication();
			Sample s = app.getSample(sampleId);
			
			TextView timestamp = (TextView)findViewById(R.id.view_sample_timestamp);
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			timestamp.setText(dateFormat.format(s.getTimestamp()));
			
			TextView title = (TextView)findViewById(R.id.view_sample_title);
			title.setText(s.getTitle());
			
			TextView description = (TextView)findViewById(R.id.view_sample_description);
			description.setText(s.getDescription());
			
			TextView status = (TextView)findViewById(R.id.view_sample_status);
			String statusText = "Status: ";
			if (s.getAccepted()) {
				statusText += "accepted";
			}
			else {
				statusText += "not accepted";
			}
			status.setText(statusText);
		}
	}
	
	public void onClickEdit(View view) {
		Intent i = new Intent(ViewSampleActivity.this, NewSampleActivity.class);
		Bundle b = new Bundle();
		b.putLong("sampleId", sampleId);
		i.putExtras(b);
		startActivity(i);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putLong("sampleId", sampleId);
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
