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
import android.view.View;
import android.widget.ListView;


public class ListSamplesActivity extends ListActivity implements OnSharedPreferenceChangeListener {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.samples_list);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        populateList();
    }
	
	@Override
	public void onResume() {
		super.onResume();
		populateList();
	}
	
	private void populateList() {
		BeeperApp app = (BeeperApp)getApplication();
		List<Sample> samplesList = app.getDataStore().getSamples();
        SampleListAdapter samples = new SampleListAdapter(this, samplesList);
        setListAdapter(samples);
	}
	
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		Sample s = (Sample)listView.getItemAtPosition(position);
		Intent i = new Intent(ListSamplesActivity.this, ViewSampleActivity.class);
		i.putExtra(getApplication().getClass().getPackage().getName() + ".SampleId", s.getId());
		startActivity(i);
	}

}
