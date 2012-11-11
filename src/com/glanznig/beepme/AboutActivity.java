package com.glanznig.beepme;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		TextView nameVersion = (TextView)findViewById(R.id.app_name_version);
		String version = "";
		try {
			version = " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}
		nameVersion.setText(getString(R.string.app_name) + version);
	}

}
