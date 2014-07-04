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

import java.util.Calendar;
import java.util.Iterator;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.ViewMomentPagerAdapter;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.Restriction;
import com.glanznig.beepme.data.db.MomentTable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewMomentActivity extends FragmentActivity {
	
	private static final String TAG = "ViewMomentActivity";
	private ViewMomentPagerAdapter pagerAdapter = null;
	private ViewPager pager = null;
	private long momentId = 0L;
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.view_moment_pager);
        
        pagerAdapter = new ViewMomentPagerAdapter(getSupportFragmentManager(), this);
        pager = (ViewPager)findViewById(R.id.view_moment_swipe_pager);
        pager.setAdapter(pagerAdapter);
        
        // set gap between pages
        //pager.setPageMargin((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, this.getResources().getDisplayMetrics()));
        //pager.setPageMarginDrawable(R.drawable.swipe_filler);
        
        // listening for page changes
        SimpleOnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            	invalidateOptionsMenu();
                getActionBar().setTitle(pagerAdapter.getPageTitle(position));
                momentId = pagerAdapter.getMomentId(position);
                
                TextView pos = (TextView)findViewById(R.id.moment_swipe_pos);
                pos.setText(String.format(getString(R.string.sample_swipe_pos), position + 1, pagerAdapter.getCount()));
            }
        };
        pager.setOnPageChangeListener(listener);
        listener.onPageSelected(0); // due to a bug in listener implementation
        
        if (savedState != null) {
        	if (savedState.getLong("momentId") != 0) {
        		momentId = savedState.getLong("momentId");
        	}
        }
        else {
        	Bundle b = getIntent().getExtras();
        	if (b != null) {
        		momentId = b.getLong(getApplication().getClass().getPackage().getName() + ".SampleId");
        	}
        }
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (momentId != 0L) {
			pager.setCurrentItem(pagerAdapter.getPosition(momentId));
		}
		
		TextView pos = (TextView)findViewById(R.id.moment_swipe_pos);
		pos.setText(String.format(getString(R.string.sample_swipe_pos), pagerAdapter.getPosition(momentId) + 1, pagerAdapter.getCount()));
		
		getActionBar().setTitle(pagerAdapter.getPageTitle(pagerAdapter.getPosition(momentId)));
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.view_moment, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem delete = menu.findItem(R.id.action_delete_moment);
		MenuItem edit = menu.findItem(R.id.action_edit_moment);
		Moment moment = new MomentTable(getApplicationContext()).getMoment(pagerAdapter.getMomentId(pager.getCurrentItem()));

        BeepMeApp app = (BeepMeApp)getApplicationContext();
        Iterator<Restriction> restrictionIterator = app.getCurrentProject().getRestrictions().iterator();

        while (restrictionIterator.hasNext()) {
            Restriction restriction = restrictionIterator.next();

            boolean allowed = restriction.getAllowed();
            if ((Calendar.getInstance().getTimeInMillis() - moment.getTimestamp().getTime()) >= restriction.getUntil() * 1000) {
                allowed = !allowed;
            }

            switch (restriction.getType()) {
                case EDIT:
                    edit.setVisible(allowed);
                    break;
                case DELETE:
                    delete.setVisible(allowed);
                    break;
            }
        }
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.action_edit_moment:
        		Intent i = new Intent(ViewMomentActivity.this, ChangeMomentActivity.class);
        		i.putExtra(getApplication().getClass().getPackage().getName() + ".MomentUid", pagerAdapter.getMomentId(pager.getCurrentItem()));
        		startActivity(i);
        		
        		return true;

            case R.id.action_delete_moment:
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(ViewMomentActivity.this);
                deleteBuilder.setTitle(R.string.moment_delete_warning_title);
                deleteBuilder.setMessage(R.string.moment_delete_warning_msg);
                deleteBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MomentTable momentTable = new MomentTable(getApplicationContext());
                        // delete moment in database
                        momentTable.deleteMoment(momentId);
                        // update pager
                        pagerAdapter.removeMoment(momentId);
                    }
                });
                deleteBuilder.setNegativeButton(R.string.no, null);
                deleteBuilder.create().show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		savedState.putLong("momentId", momentId);
	}
}
