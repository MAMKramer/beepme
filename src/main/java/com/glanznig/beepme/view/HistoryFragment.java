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
http://beepme.glanznig.com
*/

package com.glanznig.beepme.view;

import java.util.List;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.HistoryListAdapter;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Statistics;

import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class HistoryFragment extends ListFragment {
	
	private static final String TAG = "HistoryFragment";

    private int position = 0;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
		if (savedState != null) {
        	if (savedState.getInt("position") != 0) {
        		position = savedState.getInt("position");
        	}
        }
		
        View rootView = inflater.inflate(R.layout.history, container, false);
        return rootView;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		populateList();
	}
	
	private void populateList() {
		BeeperApp app = (BeeperApp)getActivity().getApplication();
		List<Bundle> statList = Statistics.getStats(getActivity(), app.getTimerProfile());
		
        HistoryListAdapter historyAdapter = new HistoryListAdapter(getActivity(), statList);
        setListAdapter(historyAdapter);
        
        ListView list = (ListView)getView().findViewById(android.R.id.list);
        list.setSelectionFromTop(position, 0);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		// save list position
		position = ((ListView)getView().findViewById(android.R.id.list)).getFirstVisiblePosition();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putInt("position", position);
	}

}
