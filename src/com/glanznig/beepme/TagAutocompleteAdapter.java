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

import com.glanznig.beepme.data.Tag;
import com.glanznig.beepme.view.NewSampleActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class TagAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
	
	private static final String TAG = "beeper";
	
	private ArrayList<Tag> resultList;
	private Context ctx;
	private int resourceId;
	
	static class ViewHolder {
	    public TextView name;
	}
    
    public TagAutocompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        ctx = context;
        resourceId = textViewResourceId;
    }
    
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		
		//performance optimization: reuse already inflated views
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(resourceId, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.name = (TextView)rowView.findViewById(R.id.autocomplete_list_tag_name);
			rowView.setTag(holder);
		}
		else {
			rowView = convertView;
		}
		
		ViewHolder holder = (ViewHolder)rowView.getTag();
		
		holder.name.setText(resultList.get(position).getName());
		return rowView;
	}
	
	@Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index).getName();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the auto-complete results.
                	BeeperApp app = (BeeperApp)((NewSampleActivity)ctx).getApplication();
            		resultList = (ArrayList<Tag>)app.getDataStore().getTags(constraint.toString());
                    
                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

}
