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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.MomentListEntry;
import com.glanznig.beepme.R;
import com.glanznig.beepme.SampleListAdapter;
import com.glanznig.beepme.ListItem;
import com.glanznig.beepme.DateListSectionHeader;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.Restriction;
import com.glanznig.beepme.data.util.Statistics;
import com.glanznig.beepme.data.db.MomentTable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


public class MomentListFragment extends ListFragment {
	
	private static final String TAG = "MomentListFragment";

    private static final int VIEW = 0;
    private static final int EDIT = 1;
    private static final int DELETE = 2;

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
        BeepMeApp app = (BeepMeApp)getActivity().getApplication();
		List<Moment> momentList = null;
        if (app.getPreferences().getProjectId() != 0L) {
            momentList = new MomentTable(getActivity().getApplicationContext()).getMoments(app.getPreferences().getProjectId());
        }
        else {
            momentList = new ArrayList<Moment>();
        }

		ArrayList<ListItem> viewList = new ArrayList<ListItem>();
		
		Iterator<Moment> momentIterator = momentList.iterator();
		DateListSectionHeader header = null;
		while (momentIterator.hasNext()) {
			Moment moment = momentIterator.next();
			if (header == null || !header.isSameDay(moment.getTimestamp())) {
				header = new DateListSectionHeader(moment.getTimestamp());
				viewList.add(header);
			}
			viewList.add(new MomentListEntry(this.getActivity().getApplicationContext(), moment));
		}
		
        SampleListAdapter moments = new SampleListAdapter(getActivity(), viewList);
        setListAdapter(moments);
        
        ListView list = (ListView)getView().findViewById(android.R.id.list);
        list.setSelectionFromTop(position, 0);
        registerForContextMenu(list);
	}
	
	private void updateStats() {
		BeepMeApp app = (BeepMeApp)getActivity().getApplication();
		Bundle stats = Statistics.getStatsOfToday(getActivity());
		
		int numAccepted = 0;
		int numDeclined = 0;
		long uptimeDur = 0;
		
		if (stats != null) {
			if (stats.containsKey("uptimeDuration")) {
				uptimeDur = stats.getLong("uptimeDuration") / 1000;
			}
			if (stats.containsKey("acceptedMoments")) {
				numAccepted = stats.getInt("acceptedMoments");
			}
			if (stats.containsKey("declinedMoments")) {
				numDeclined = stats.getInt("declinedMoments");
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
		long momentUid = ((MomentListEntry)listView.getItemAtPosition(position)).getMomentUid();
		Intent i = new Intent(getActivity(), ViewMomentActivity.class);
		i.putExtra(getActivity().getApplication().getClass().getPackage().getName() + ".MomentUid", momentUid);
		startActivity(i);
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View item, ContextMenu.ContextMenuInfo menuInfo) {
        if (item.getId() == android.R.id.list) {
            ListView listView = (ListView)item;
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            MomentListEntry momentEntry = (MomentListEntry)listView.getItemAtPosition(info.position);
            menu.setHeaderTitle(dateTimeFormat.format(momentEntry.getTimestamp()));

            menu.add(ContextMenu.NONE, VIEW, VIEW, getString(R.string.action_view_moment));

            BeepMeApp app = (BeepMeApp)getActivity().getApplicationContext();
            Iterator<Restriction> restrictionIterator = app.getCurrentProject().getRestrictions().iterator();

            while (restrictionIterator.hasNext()) {
                Restriction restriction = restrictionIterator.next();

                boolean allowed = restriction.getAllowed();
                Long until = restriction.getUntil();
                if (until != null && (Calendar.getInstance().getTimeInMillis() - momentEntry.getTimestamp().getTime() >= until * 1000)) {
                    allowed = !allowed;
                }

                if (allowed) {
                    switch (restriction.getType()) {
                        case EDIT:
                            menu.add(ContextMenu.NONE, EDIT, EDIT, getString(R.string.action_edit_sample));
                            break;
                        case DELETE:
                            menu.add(ContextMenu.NONE, DELETE, DELETE, getString(R.string.action_delete_moment));
                            break;
                    }
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemIndex = item.getItemId();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final int position = info.position;

        final ListView listView = (ListView)getView().findViewById(android.R.id.list);
        final long momentUid = ((MomentListEntry)listView.getItemAtPosition(position)).getMomentUid();

        switch (menuItemIndex) {
            case VIEW:
                Intent view = new Intent(getActivity(), ViewMomentActivity.class);
                view.putExtra(getActivity().getApplication().getClass().getPackage().getName() + ".MomentUid", momentUid);
                startActivity(view);
                break;

            case EDIT:
                Intent edit = new Intent(getActivity(), ChangeMomentActivity.class);
                edit.putExtra(getActivity().getApplication().getClass().getPackage().getName() + ".MomentUid", momentUid);
                startActivity(edit);
                break;

            case DELETE:
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getActivity());
                deleteBuilder.setTitle(R.string.moment_delete_warning_title);
                deleteBuilder.setMessage(R.string.moment_delete_warning_msg);
                deleteBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MomentTable momentTable = new MomentTable(getActivity().getApplicationContext());
                        // delete moment in database
                        momentTable.deleteMoment(momentUid);
                        // todo update adapter and list
                    }
                });
                deleteBuilder.setNegativeButton(R.string.no, null);
                deleteBuilder.create().show();
                break;
        }
        return true;
    }
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putInt("position", position);
	}

}
