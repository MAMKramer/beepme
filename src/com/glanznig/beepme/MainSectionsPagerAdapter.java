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

import com.glanznig.beepme.R;
import com.glanznig.beepme.view.HistoryFragment;
import com.glanznig.beepme.view.ListSamplesFragment;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

public class MainSectionsPagerAdapter extends FragmentPagerAdapter {
	
	private Context context;
	
	public MainSectionsPagerAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        context = ctx;
    }

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return new ListSamplesFragment();
			case 1:
			default:
				return new HistoryFragment();
		}
	}

	@Override
	public int getCount() {
		return 2;
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
		Resources res = context.getResources();
        switch(position) {
        	case 0:
        		return res.getString(R.string.samples);
        	case 1:
        		return res.getString(R.string.history);
        }
        
        return "";
    }

}
