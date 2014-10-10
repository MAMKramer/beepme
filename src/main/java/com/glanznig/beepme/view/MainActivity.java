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

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.MainSectionsPagerAdapter;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Beep;
import com.glanznig.beepme.data.db.BeepTable;
import com.glanznig.beepme.data.util.PreferenceHandler;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	
	private static final String TAG = "MainActivity";
	private AudioManager audioManager = null;
	private Menu actionMenu = null;
	private MainSectionsPagerAdapter pagerAdapter = null;
	private ViewPager pager = null;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        final BeepMeApp app = (BeepMeApp)getApplication();

		setContentView(R.layout.main);
		
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
        pagerAdapter = new MainSectionsPagerAdapter(getSupportFragmentManager(), this);
        final ActionBar actionBar = getActionBar();
        
        if (app.getPreferences().isTestMode()) {
        	actionBar.setSubtitle(getString(R.string.pref_title_test_mode));
        }
        
        // displaying tabs in the action bar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        pager = (ViewPager)findViewById(R.id.main_tab_pager);
        pager.setAdapter(pagerAdapter);
        
        // set gap between pages
        pager.setPageMargin((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, this.getResources().getDisplayMetrics()));
        pager.setPageMarginDrawable(R.drawable.swipe_filler);
        
        // listening for page changes
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // for each of the sections in the app, add a tab to the action bar
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(actionBar.newTab()
                            .setText(pagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
	}
	
	@Override
	public void onResume() {
		super.onResume();

        final BeepMeApp app = (BeepMeApp)getApplication();
        // if there is no active project display welcome screen
        if (app.getPreferences().getProjectId() == 0L) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }
		
		// make sure the current state is reflected in the options menu
		invalidateOptionsMenu();
		
		//make sure that scheduled beeps do not expire due to an error
		if (app.isBeeperActive()) {
            Beep currentBeep = app.getCurrentScheduledBeep();
			
			if (currentBeep != null) {
                if (currentBeep.getStatus() != Beep.BeepStatus.RECEIVED && currentBeep.isOverdue()) {
                    app.updateBeep(Beep.BeepStatus.EXPIRED);
                    app.scheduleBeep();
                }
			}
			else {
				app.scheduleBeep();
			}
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				if (action == KeyEvent.ACTION_UP) {
					audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
							AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
				}
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if (action == KeyEvent.ACTION_DOWN) {
					audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
							AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
				}
				return true;
			default:
				return super.dispatchKeyEvent(event);
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main_menu, menu);
        this.actionMenu = menu;
        
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		BeepMeApp app = (BeepMeApp)getApplication();
		
		// hide/show test beep menu entry
        MenuItem testBeep = menu.findItem(R.id.action_test_beep);
        if (app.getPreferences().isTestMode() && app.isBeeperActive()) {
        	testBeep.setVisible(true);
        }
        else {
        	testBeep.setVisible(false);
        }
        
        // update toggle beeper icon
        MenuItem item = menu.findItem(R.id.action_toggle_beeper);
        if (app.isBeeperActive()) {
			item.setIcon(R.drawable.ic_menu_beeper_on);
		}
		else {
			item.setIcon(R.drawable.ic_menu_beeper_off);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		BeepMeApp app = (BeepMeApp)getApplication();
		
        switch (item.getItemId()) {
        	case R.id.action_toggle_beeper:
        		if (app.isBeeperActive()) {
                    app.setBeeperStatus(PreferenceHandler.BeeperStatus.INACTIVE);
        			item.setIcon(R.drawable.ic_menu_beeper_off);
        			
        			// hide generate beep menu entry
        			if (this.actionMenu != null && app.getPreferences().isTestMode()) {
        				MenuItem testBeep = actionMenu.findItem(R.id.action_test_beep);
        				testBeep.setVisible(false);
        			}
        		}
        		else {
                    app.setBeeperStatus(PreferenceHandler.BeeperStatus.ACTIVE);
        			item.setIcon(R.drawable.ic_menu_beeper_on);
        			
        			// show generate beep menu entry
        			if (this.actionMenu != null && app.getPreferences().isTestMode()) {
        				MenuItem testBeep = actionMenu.findItem(R.id.action_test_beep);
        				testBeep.setVisible(true);
        			}
        		}
        		
        		return true;
        		
        	case R.id.action_test_beep:
        		app.beep();
        		return true;
        		
            case R.id.action_export:
                Intent iExport = new Intent(this, ExportActivity.class);
                startActivity(iExport);
            	return true;

            case R.id.action_project_info:
                Intent iProjectInfo = new Intent(this, ProjectInfoActivity.class);
                startActivity(iProjectInfo);
                return true;
            	
            case R.id.action_settings:
                Intent iSettings = new Intent(this, SettingsActivity.class);
                startActivity(iSettings);
                return true;
                
            case R.id.action_about:
            	
            	// thanks to F-Droid for the inspiration
				View view = null;
				LayoutInflater inflater = LayoutInflater.from(this);
				view = inflater.inflate(R.layout.about, null);
				
				try {
					PackageInfo pkgInfo = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
					((TextView)view.findViewById(R.id.about_version)).setText(pkgInfo.versionName);
				} catch (Exception e) {}
				
				AlertDialog.Builder alertBuilder = null;
				alertBuilder = new AlertDialog.Builder(this).setView(view);
				 
				AlertDialog dia = alertBuilder.create();
				dia.setIcon(R.drawable.ic_launcher_beepme);
				dia.setTitle(getString(R.string.about_title));
				dia.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.about_donate_button),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								Uri uri = Uri.parse("http://beepme.yourexp.at/support-beepme");
								startActivity(new Intent(Intent.ACTION_VIEW, uri));
							}
						});
				
				dia.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				dia.show();
				return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		pager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

}
