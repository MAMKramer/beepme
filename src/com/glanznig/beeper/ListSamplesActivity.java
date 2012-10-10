package com.glanznig.beeper;

import java.util.List;

import com.glanznig.beeper.R;
import com.glanznig.beeper.data.Sample;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;


public class ListSamplesActivity extends ListActivity implements OnSharedPreferenceChangeListener {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.samples_list);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        
        populateList();
    }
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        populateList();
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
	
	@Override
	public void onResume() {
		super.onResume();
		populateList();
	}
	
	private void populateList() {
		BeeperApp app = (BeeperApp)getApplication();
		List<Sample> samplesList = app.getSamples();
        SampleListAdapter samples = new SampleListAdapter(this, samplesList);
        setListAdapter(samples);
	}
	
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		Sample s = (Sample)listView.getItemAtPosition(position);
		Intent i = new Intent(ListSamplesActivity.this, ViewSampleActivity.class);
		Bundle b = new Bundle();
		b.putLong("sampleId", s.getId());
		i.putExtras(b);
		startActivity(i);
	}

}
