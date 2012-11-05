package com.glanznig.beeper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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
