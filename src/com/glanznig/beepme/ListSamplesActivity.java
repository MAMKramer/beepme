package com.glanznig.beepme;

import java.util.List;

import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Sample;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;


public class ListSamplesActivity extends ListActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.samples_list);
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
