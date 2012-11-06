package com.glanznig.beeper.helper;

import com.glanznig.beeper.R;

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
