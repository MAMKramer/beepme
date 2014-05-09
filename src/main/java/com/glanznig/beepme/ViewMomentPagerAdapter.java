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

package com.glanznig.beepme;

import java.util.List;

import com.glanznig.beepme.data.InputElement;
import com.glanznig.beepme.data.Project;
import com.glanznig.beepme.data.SingleValue;
import com.glanznig.beepme.data.Value;
import com.glanznig.beepme.data.db.InputElementTable;
import com.glanznig.beepme.data.db.MomentTable;
import com.glanznig.beepme.data.db.ValueTable;
import com.glanznig.beepme.view.ViewSampleFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Provides the pager adapter for swiping through the individual moments.
 */
public class ViewMomentPagerAdapter extends FragmentStatePagerAdapter {
	
	private Context ctx;
	private List<Long> items;
    private InputElement listTitle;
	
	public ViewMomentPagerAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        this.ctx = ctx.getApplicationContext();
        BeepMeApp app = (BeepMeApp)ctx.getApplicationContext();
        Project project = app.getCurrentProject();
        listTitle = new InputElementTable(ctx).getInputElementByName(project.getUid(), project.getOption("listTitle"));
        items = new MomentTable(ctx.getApplicationContext()).getMomentUids(app.getCurrentProject().getUid());
    }

    /**
     * Gets the view of an individual moment.
     * @param position position of the moment in the list
     * @return view of individual moment
     */
	@Override
	public Fragment getItem(int position) {
		long momentId = items.get(position).longValue();
		
		Fragment fragment = new ViewSampleFragment();
        Bundle args = new Bundle();
        args.putLong("momentId", momentId);
        fragment.setArguments(args);
        return fragment;
	}

    /**
     * Converts a list position into a moment uid.
     * @param position position of the moment in the list
     * @return moment uid of the moment at this position
     */
	public long getMomentId(int position) {
		return items.get(position);
	}

    /**
     * Converts a moment uid into a list position.
     * @param momentId uid of the moment
     * @return position of the moment in the list
     */
	public int getPosition(long momentId) {
		return items.indexOf(Long.valueOf(momentId));
	}

    /**
     * Gets the number of moments in the list.
     * @return number of moments
     */
	@Override
	public int getCount() {
		return items.size();
	}

    /**
     * Gets the title of a moment at the given position.
     * @param position position of the moment in the list
     * @return title of the moment
     */
	@Override
    public CharSequence getPageTitle(int position) {
		long momentId = items.get(position);

        if (listTitle != null) {
            Value value = new ValueTable(ctx).getValue(momentId, listTitle.getUid());
            if (value instanceof SingleValue) {
                String valueContent = ((SingleValue) value).getValue();
                if (valueContent != null && valueContent.length() > 0) {
                    return valueContent;
                }
            }
        }

		return ctx.getString(R.string.sample_untitled);
    }

}
