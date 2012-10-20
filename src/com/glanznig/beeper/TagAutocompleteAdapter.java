package com.glanznig.beeper;

import java.util.ArrayList;

import com.glanznig.beeper.data.Tag;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public class TagAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
	
	private static final String TAG = "beeper";
	
	private ArrayList<Tag> resultList;
	private Context ctx;
    
    public TagAutocompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        ctx = context;
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
                    // Retrieve the autocomplete results.
                	BeeperApp app = (BeeperApp)((NewSampleActivity)ctx).getApplication();
            		resultList = (ArrayList<Tag>)app.getTags(constraint.toString());
                    
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
