package com.glanznig.beeper;

import java.util.List;
import java.text.DateFormat;

import com.glanznig.beeper.data.Sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SampleListAdapter extends ArrayAdapter<Sample> {
	
	private final Context context;
	private final List<Sample> samples;
	
	static class ViewHolder {
	    public TextView title;
	    public TextView timestamp;
	}
	
	public SampleListAdapter(Context context, List<Sample> values) {
	    super(context, R.layout.samples_list_row, values);
	    this.context = context;
	    this.samples = values;
	  }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		
		//performance optimization: reuse already inflated views
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.samples_list_row, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.title = (TextView)rowView.findViewById(R.id.sample_title);
			holder.timestamp = (TextView)rowView.findViewById(R.id.sample_timestamp);
			rowView.setTag(holder);
		}
		else {
			rowView = convertView;
		}
		
		ViewHolder holder = (ViewHolder)rowView.getTag();
		
		holder.title.setText(samples.get(position).getTitle());
		holder.timestamp.setText(dateFormat.format(samples.get(position).getTimestamp()));
		return rowView;
	}

}
