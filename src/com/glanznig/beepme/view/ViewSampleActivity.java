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

import java.util.Calendar;

import com.glanznig.beepme.R;
import com.glanznig.beepme.ViewSamplePagerAdapter;
import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.data.SampleTable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewSampleActivity extends FragmentActivity {
	
	private static final String TAG = "ViewSampleActivity";
	private ViewSamplePagerAdapter pagerAdapter = null;
	private ViewPager pager = null;
	private int position;
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.view_sample_pager);
        
        position = 0;
        
        pagerAdapter = new ViewSamplePagerAdapter(getSupportFragmentManager(), this);
        pager = (ViewPager)findViewById(R.id.view_sample_swipe_pager);
        pager.setAdapter(pagerAdapter);
        
        // set gap between pages
        pager.setPageMargin((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, this.getResources().getDisplayMetrics()));
        pager.setPageMarginDrawable(R.drawable.swipe_filler);
        
        // listening for page changes
        SimpleOnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            	invalidateOptionsMenu();
                getActionBar().setTitle(pagerAdapter.getPageTitle(position));
                
                TextView pos = (TextView)findViewById(R.id.sample_swipe_pos);
                pos.setText(String.format(getString(R.string.sample_swipe_pos), position + 1, pagerAdapter.getCount()));
            }
        };
        pager.setOnPageChangeListener(listener);
        listener.onPageSelected(0); // due to a bug in listener implementation
        
        if (savedState != null) {
        	if (savedState.getLong("position") != 0L) {
        		position = savedState.getInt("position");
        	}
        }
        else {
        	Bundle b = getIntent().getExtras();
        	if (b != null) {
        		long sampleId = b.getLong(getApplication().getClass().getPackage().getName() + ".SampleId");
        		position = pagerAdapter.getPosition(sampleId);
        	}
        }
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (position >= 0) {
			pager.setCurrentItem(position);
		}
		
		TextView pos = (TextView)findViewById(R.id.sample_swipe_pos);
		pos.setText(String.format(getString(R.string.sample_swipe_pos), position + 1, pagerAdapter.getCount()));
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.view_sample, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem edit = menu.findItem(R.id.action_edit_sample);
		Sample s = new SampleTable(this.getApplicationContext()).getSampleWithTags(pagerAdapter.getSampleId(pager.getCurrentItem()));
		
		//not editable if more than a day old
		if ((Calendar.getInstance().getTimeInMillis() - s.getTimestamp().getTime()) >= 24 * 60 * 60 * 1000) {
			edit.setVisible(false);
		}
		else {
			edit.setVisible(true);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.action_edit_sample:
        		Intent i = new Intent(ViewSampleActivity.this, EditSampleActivity.class);
        		i.putExtra(getApplication().getClass().getPackage().getName() + ".SampleId", pagerAdapter.getSampleId(pager.getCurrentItem()));
        		startActivity(i);
        		
        		return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putLong("position", position);
	}
}
