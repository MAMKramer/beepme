package com.glanznig.beeper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff.Mode;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainMenu extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		populateFields();
	}
	
	private void populateFields() {
		BeeperApp app = (BeeperApp)getApplication();
		
		Button testBeep = (Button)findViewById(R.id.btn_main_menu_test_beep);
		TextView labelTestMode = (TextView)findViewById(R.id.main_menu_label_test_mode);
		
		if (app.getPreferences().isTestMode()) {
			testBeep.setVisibility(View.VISIBLE);
			labelTestMode.setVisibility(View.VISIBLE);
		}
		else {
			testBeep.setVisibility(View.GONE);
			labelTestMode.setVisibility(View.GONE);
		}
		
		LinearLayout buttons = (LinearLayout)findViewById(R.id.main_buttons);
		buttons.measure(0, 0);
		int buttonsHeight = buttons.getMeasuredHeight();
		int displayHeight = getWindowManager().getDefaultDisplay().getHeight();
		ImageView beeperStatus = (ImageView)findViewById(R.id.beeper_status);
		
		if (app.isBeeperActive()) {
			beeperStatus.setImageResource(R.drawable.beeper_status_active);
			testBeep.setEnabled(true);
		}
		else {
			beeperStatus.setImageResource(R.drawable.beeper_status_paused);
			testBeep.setEnabled(false);
		}
		
		beeperStatus.measure(0, 0);
		int iconHeight = beeperStatus.getMeasuredHeight();
		beeperStatus.setPadding(0, (displayHeight - buttonsHeight) / 2 - iconHeight / 2, 0, 0);
		
		PorterDuffColorFilter green = new PorterDuffColorFilter(Color.rgb(130, 217, 130), Mode.MULTIPLY);
		PorterDuffColorFilter red = new PorterDuffColorFilter(Color.rgb(217, 130, 130), Mode.MULTIPLY);
		Button beeperStateToggle = (Button)findViewById(R.id.btn_main_menu_beeper_state_toggle);
		
		if (app.getPreferences().isBeeperActive()) {
			beeperStateToggle.setText(R.string.pause_beeper);
			beeperStateToggle.getBackground().setColorFilter(red);
		}
		else {
			beeperStateToggle.setText(R.string.start_beeper);
			beeperStateToggle.getBackground().setColorFilter(green);
		}
		
		int numAccepted = app.getDataStore().getNumAcceptedToday();
		int numDeclined = app.getDataStore().getSampleCountToday() - numAccepted;
		long uptimeDur = app.getDataStore().getUptimeDurToday();
		TextView acceptedToday = (TextView)findViewById(R.id.beep_accepted_today);
		TextView declinedToday = (TextView)findViewById(R.id.beep_declined_today);
		TextView beeperActive = (TextView)findViewById(R.id.main_beeper_active_today);
		String accepted = String.format(getString(R.string.beep_accepted_today), numAccepted);
		String declined = String.format(getString(R.string.beep_declined_today), numDeclined);
		String timeActive = String.format("%d:%02d:%02d", uptimeDur/3600, (uptimeDur%3600)/60, (uptimeDur%60));
		String active = String.format(getString(R.string.time_beeper_active), timeActive);
		acceptedToday.setText(accepted);
		acceptedToday.setTextColor(Color.rgb(130, 217, 130));
		declinedToday.setText(declined);
		declinedToday.setTextColor(Color.rgb(217, 130, 130));
		beeperActive.setText(active);
	}
	
	public void onClickBeeperStateToggle(View view) {
		BeeperApp app = (BeeperApp)getApplication();
		if (app.isBeeperActive()) {
			app.setBeeperActive(false);
			app.clearTimer();
		}
		else {
			app.setBeeperActive(true);
			app.setTimer();
		}
		
		populateFields();
	}
	
	public void onClickListSamples(View view) {
		startActivity(new Intent(MainMenu.this, ListSamplesActivity.class));
	}
	
	public void onClickBeep(View view) {
		BeeperApp app = (BeeperApp)getApplication();
		app.beep();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main_menu, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent iSettings = new Intent(this, Preferences.class);
                startActivity(iSettings);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
