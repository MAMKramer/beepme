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

package com.glanznig.beepme;

import java.util.ArrayList;
import java.util.Iterator;
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
		ArrayList<SampleListItem> viewList = new ArrayList<SampleListItem>();
		
		Iterator<Sample> i = samplesList.iterator();
		SampleListSectionHeader header = null;
		while (i.hasNext()) {
			Sample s = i.next();
			if (header == null || !header.isSameDay(s.getTimestamp())) {
				header = new SampleListSectionHeader(s.getTimestamp());
				viewList.add(header);
			}
			viewList.add(new SampleListEntry(s));
		}
		
        SampleListAdapter samples = new SampleListAdapter(this, viewList);
        setListAdapter(samples);
	}
	
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		Sample s = ((SampleListEntry)listView.getItemAtPosition(position)).getSample();
		Intent i = new Intent(ListSamplesActivity.this, ViewSampleActivity.class);
		i.putExtra(getApplication().getClass().getPackage().getName() + ".SampleId", s.getId());
		startActivity(i);
	}

}
