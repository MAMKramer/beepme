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

Copyright 2012-2014 Michael Glanznig
http://beepme.yourexp.at
*/

package com.glanznig.beepme.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.SampleListAdapter;
import com.glanznig.beepme.SampleListEntry;
import com.glanznig.beepme.ListItem;
import com.glanznig.beepme.DateListSectionHeader;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.util.Statistics;
import com.glanznig.beepme.data.db.MomentTable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


public class SampleListFragment extends ListFragment {
	
	private static final String TAG = "ListSamplesFragment";
	private int position = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
		if (savedState != null) {
        	if (savedState.getInt("position") != 0) {
        		position = savedState.getInt("position");
        	}
        }

        return inflater.inflate(R.layout.list_sample, container, false);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		populateList();
		updateStats();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		// save list position
		position = ((ListView)getView().findViewById(android.R.id.list)).getFirstVisiblePosition();
	}
	
	private void populateList() {
		List<Moment> samplesList = new MomentTable(getActivity().getApplicationContext()).getSamples();
		ArrayList<ListItem> viewList = new ArrayList<ListItem>();
		
		Iterator<Moment> i = samplesList.iterator();
		DateListSectionHeader header = null;
		while (i.hasNext()) {
			Moment s = i.next();
			if (header == null || !header.isSameDay(s.getTimestamp())) {
				header = new DateListSectionHeader(s.getTimestamp());
				viewList.add(header);
			}
			viewList.add(new SampleListEntry(s));
		}
		
        SampleListAdapter samples = new SampleListAdapter(getActivity(), viewList);
        setListAdapter(samples);
        
        ListView list = (ListView)getView().findViewById(android.R.id.list);
        list.setSelectionFromTop(position, 0);
	}
	
	private void updateStats() {
		BeepMeApp app = (BeepMeApp)getActivity().getApplication();
		Bundle stats = Statistics.getStatsOfToday(getActivity(), app.getTimerProfile());
		
		int numAccepted = 0;
		int numDeclined = 0;
		long uptimeDur = 0;
		
		if (stats != null) {
			if (stats.containsKey("uptimeDuration")) {
				uptimeDur = stats.getLong("uptimeDuration") / 1000;
			}
			if (stats.containsKey("acceptedSamples")) {
				numAccepted = stats.getInt("acceptedSamples");
			}
			if (stats.containsKey("declinedSamples")) {
				numDeclined = stats.getInt("declinedSamples");
			}
		}
		
		TextView acceptedToday = (TextView)getView().findViewById(R.id.samples_list_today_accepted);
		TextView declinedToday = (TextView)getView().findViewById(R.id.samples_list_today_declined);
		TextView beeperActive = (TextView)getView().findViewById(R.id.samples_list_today_elapsed);

		String timeActive = String.format("%02d:%02d:%02d", uptimeDur/3600, (uptimeDur%3600)/60, (uptimeDur%60));

		acceptedToday.setText(String.valueOf(numAccepted));
		declinedToday.setText(String.valueOf(numDeclined));
		beeperActive.setText(String.valueOf(timeActive));
	}
	
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		Moment s = ((SampleListEntry)listView.getItemAtPosition(position)).getSample();
		Intent i = new Intent(getActivity(), ViewSampleActivity.class);
		i.putExtra(getActivity().getApplication().getClass().getPackage().getName() + ".SampleId", s.getId());
		startActivity(i);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putInt("position", position);
	}

}
