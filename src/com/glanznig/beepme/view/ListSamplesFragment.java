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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.SampleListAdapter;
import com.glanznig.beepme.SampleListEntry;
import com.glanznig.beepme.SampleListItem;
import com.glanznig.beepme.SampleListSectionHeader;
import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.data.SampleTable;
import com.glanznig.beepme.data.UptimeTable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


public class ListSamplesFragment extends ListFragment {
	
	private static final String TAG = "ListSamplesFragment";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.samples_list, container, false);
        return rootView;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		populateList();
		updateStats();
	}
	
	private void populateList() {
		List<Sample> samplesList = new SampleTable(getActivity().getApplicationContext()).getSamples();
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
		
        SampleListAdapter samples = new SampleListAdapter(getActivity(), viewList);
        setListAdapter(samples);
	}
	
	private void updateStats() {
		BeeperApp app = (BeeperApp)getActivity().getApplication();
		
		SampleTable st = new SampleTable(getActivity().getApplicationContext());
		
		int numAccepted = st.getNumAcceptedToday();
		int numDeclined = st.getSampleCountToday() - numAccepted;
		long uptimeDur = new UptimeTable(getActivity().getApplicationContext(), app.getTimerProfile()).getUptimeDurToday();
		
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
		Sample s = ((SampleListEntry)listView.getItemAtPosition(position)).getSample();
		Intent i = new Intent(getActivity(), ViewSampleActivity.class);
		i.putExtra(getActivity().getApplication().getClass().getPackage().getName() + ".SampleId", s.getId());
		startActivity(i);
	}

}
