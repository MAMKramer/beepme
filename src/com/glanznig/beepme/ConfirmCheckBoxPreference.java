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

import com.glanznig.beepme.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class ConfirmCheckBoxPreference extends CheckBoxPreference {
	
	public ConfirmCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public ConfirmCheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ConfirmCheckBoxPreference(Context context) {
		super(context);
	}
	
	@Override
	public void onClick() {
		//super.onClick();
		
		AlertDialog.Builder warnTestModeBuilder = new AlertDialog.Builder(getContext());
        warnTestModeBuilder.setTitle(R.string.warn_test_mode_title);
        warnTestModeBuilder.setMessage(R.string.warn_test_mode_msg);
        warnTestModeBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	if (callChangeListener(!isChecked())) {
            		setChecked(!isChecked());
            	}
            }
        });
        warnTestModeBuilder.setNegativeButton(R.string.no, null);
        warnTestModeBuilder.create().show();
	}

}
