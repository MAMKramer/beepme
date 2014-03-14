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

package com.glanznig.beepme;

import java.util.Date;
import java.util.List;
import java.text.DateFormat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HistoryListAdapter extends ArrayAdapter<Bundle> {
	
	private static final String TAG = "HistoryListAdapter";
	
	private final Context context;
	private final List<Bundle> history;
	
	static class EntryHolder {
	    public TextView day;
	    public TextView accepted;
	    public TextView declined;
	    public TextView elapsed;
	}
	
	public HistoryListAdapter(Context context, List<Bundle> values) {
	    super(context, R.layout.list_history_row, values);
	    this.context = context;
	    this.history = values;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return false;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView;
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		
		//performance optimization: reuse already inflated views
		if (convertView != null && convertView instanceof LinearLayout) {
			rowView = convertView;
		}
		else {
			rowView = inflater.inflate(R.layout.list_history_row, parent, false);
			
			EntryHolder holder = new EntryHolder();
			holder.day = (TextView)rowView.findViewById(R.id.list_history_day);
			holder.accepted = (TextView)rowView.findViewById(R.id.list_history_accepted);
			holder.declined = (TextView)rowView.findViewById(R.id.list_history_declined);
			holder.elapsed = (TextView)rowView.findViewById(R.id.list_history_elapsed);
			rowView.setTag(holder);
		}
		
		EntryHolder holder = (EntryHolder)rowView.getTag();
		Bundle item = history.get(position);
		
		if (item.containsKey("timestamp")) {
			holder.day.setText(dateFormat.format(new Date(item.getLong("timestamp"))));
		}
		
		if (item.containsKey("acceptedSamples")) {
			String acceptedText = "";
			int accepted = item.getInt("acceptedSamples");
			if (accepted < 10) {
				acceptedText += " ";
			}
			acceptedText += String.valueOf(accepted);
			holder.accepted.setText(acceptedText);
		}
		
		if (item.containsKey("declinedSamples")) {
			String declinedText = "";
			int declined = item.getInt("declinedSamples");
			if (declined < 10) {
				declinedText += " ";
			}
			declinedText += String.valueOf(declined);
			holder.declined.setText(declinedText);
		}
		
		if (item.containsKey("uptimeDuration")) {
			long uptimeDur = item.getLong("uptimeDuration") / 1000;
			String timeActive = String.format("%02d:%02d:%02d", uptimeDur/3600, (uptimeDur%3600)/60, (uptimeDur%60));
			holder.elapsed.setText(timeActive);
		}
		
		return rowView;
	}

}
