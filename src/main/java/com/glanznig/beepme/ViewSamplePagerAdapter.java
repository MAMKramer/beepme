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

import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.db.SampleTable;
import com.glanznig.beepme.view.ViewSampleFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewSamplePagerAdapter extends FragmentStatePagerAdapter {
	
	private Context context;
	private List<Long> items;
	
	public ViewSamplePagerAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        context = ctx;
        items = new SampleTable(ctx.getApplicationContext()).getSampleIds();
    }

	@Override
	public Fragment getItem(int position) {
		long sampleId = items.get(position).longValue();
		
		Fragment fragment = new ViewSampleFragment();
        Bundle args = new Bundle();
        args.putLong("sampleId", sampleId);
        fragment.setArguments(args);
        return fragment;
	}
	
	public long getSampleId(int position) {
		return items.get(position);
	}
	
	public int getPosition(long sampleId) {
		return items.indexOf(Long.valueOf(sampleId));
	}

	@Override
	public int getCount() {
		return items.size();
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
		long sampleId = items.get(position);
		Sample s = new SampleTable(context.getApplicationContext()).getSample(sampleId);
		if (s.getTitle() != null && s.getTitle().length() > 0) {
			return s.getTitle();
		}
		return context.getString(R.string.sample_untitled);
    }

}
