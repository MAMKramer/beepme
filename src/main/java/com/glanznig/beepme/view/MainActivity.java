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

package com.glanznig.beepme.view;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.MainSectionsPagerAdapter;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.DataExporter;
import com.glanznig.beepme.db.ScheduledBeepTable;

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
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	
	private static final String TAG = "MainActivity";
	private AudioManager audioManager = null;
	private Menu actionMenu = null;
	private MainSectionsPagerAdapter pagerAdapter = null;
	private ViewPager pager = null;
	
	private static class ExportHandler extends Handler {
		WeakReference<MainActivity> mainMenu;
		Bundle data;
		
		ExportHandler(MainActivity activity) {
			mainMenu = new WeakReference<MainActivity>(activity);
		}
		
		public static String readableFileSize(long size) {
		    if(size <= 0) return "0";
		    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		    NumberFormat numFormat = DecimalFormat.getInstance();
		    numFormat.setMaximumFractionDigits(1);
		    return numFormat.format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
		}
		
		@Override
		public void handleMessage(Message message) {
			if (mainMenu.get() != null) {
				data = message.getData();
				if (data.getString("fileName") != null) {
					
					AlertDialog.Builder sendMailBuilder = new AlertDialog.Builder(mainMenu.get());
			        sendMailBuilder.setTitle(R.string.export_success_send_mail_title);
			        File dataFile = new File(data.getString("fileName"));
			        String msg = String.format(mainMenu.get().getString(R.string.export_send_mail_msg),
			        		readableFileSize(dataFile.length()));
			        sendMailBuilder.setMessage(msg);
			        sendMailBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int id) {
			            	
			            	Uri fUri = Uri.fromFile(new File(data.getString("fileName")));
							Intent sendIntent = new Intent(Intent.ACTION_SEND);
							sendIntent.putExtra(Intent.EXTRA_SUBJECT, mainMenu.get().getString(R.string.export_mail_subject));
							sendIntent.putExtra(Intent.EXTRA_STREAM, fUri);
							sendIntent.setType("text/rfc822");
							try {
							    mainMenu.get().startActivity(Intent.createChooser(sendIntent, mainMenu.get().getString((R.string.mail_chooser_title))));
							} catch (android.content.ActivityNotFoundException ex) {
							    Toast.makeText(mainMenu.get(), R.string.error_no_mail_apps, Toast.LENGTH_SHORT).show();
							}
			            }
			        });
			        sendMailBuilder.setNegativeButton(R.string.no, null);
			        sendMailBuilder.create().show();
			        
			        BeeperApp app = (BeeperApp)mainMenu.get().getApplication();
			        app.getPreferences().setExportRunningSince(0L);
				}
			}
		}
	}
	
	private static class ExportRunnable implements Runnable {
		WeakReference<MainActivity> mainMenu;
		WeakReference<ExportHandler> handler;
		
		ExportRunnable(MainActivity activity, ExportHandler handler) {
			mainMenu = new WeakReference<MainActivity>(activity);
			this.handler = new WeakReference<ExportHandler>(handler); 
		}
		@Override
	    public void run() {
			if (mainMenu.get() != null) {
				DataExporter exporter = new DataExporter(mainMenu.get().getApplicationContext());
				String fileName = exporter.exportToZipFile();
				if (handler.get() != null) {
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("fileName", fileName);
					msg.setData(bundle);
					handler.get().sendMessage(msg);
				}
			}
	    }
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
        pagerAdapter = new MainSectionsPagerAdapter(getSupportFragmentManager(), this);
        final ActionBar actionBar = getActionBar();
        
        BeeperApp app = (BeeperApp)getApplication();
        
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
		
		// make sure the current state is reflected in the options menu
		invalidateOptionsMenu();
		
		//make sure that scheduled beeps do not expire due to an error
		BeeperApp app = (BeeperApp)getApplication();
		if (app.isBeeperActive()) {
			long scheduledBeepId = app.getPreferences().getScheduledBeepId();
			
			if (scheduledBeepId != 0L) {
				ScheduledBeepTable sbt = new ScheduledBeepTable(this.getApplicationContext());
				if (sbt.getStatus(scheduledBeepId) != 3 && sbt.isExpired(scheduledBeepId)) {
					app.expireTimer();
					app.setTimer();
				}
			}
			else {
				app.setTimer();
			}
		}
	}
	
	public void onClickListSamples(View view) {
		startActivity(new Intent(MainActivity.this, SampleListFragment.class));
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
		BeeperApp app = (BeeperApp)getApplication();
		
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
		BeeperApp app = (BeeperApp)getApplication();
		
        switch (item.getItemId()) {
        	case R.id.action_toggle_beeper:
        		if (app.isBeeperActive()) {
        			app.cancelTimer(); //call before setBeeperActive
        			app.setBeeperActive(BeeperApp.BEEPER_INACTIVE);
        			item.setIcon(R.drawable.ic_menu_beeper_off);
        			
        			// hide generate beep menu entry
        			if (this.actionMenu != null && app.getPreferences().isTestMode()) {
        				MenuItem testBeep = actionMenu.findItem(R.id.action_test_beep);
        				testBeep.setVisible(false);
        			}
        		}
        		else {
        			app.setBeeperActive(BeeperApp.BEEPER_ACTIVE);
        			app.setTimer();
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
            	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            		if (app.getPreferences().exportRunningSince() == 0L ||
            				(Calendar.getInstance().getTimeInMillis() -
            				app.getPreferences().exportRunningSince()) >= 120000) { //2 min
            			app.getPreferences().setExportRunningSince(Calendar.getInstance().getTimeInMillis());
            			new Thread(new ExportRunnable(MainActivity.this, new ExportHandler(MainActivity.this))).start();
            		}
            	}
            	else {
            		Toast.makeText(MainActivity.this, R.string.sdcard_error, Toast.LENGTH_SHORT).show();
            	}
            	return true;
            	
            case R.id.action_settings:
                Intent iSettings = new Intent(this, Preferences.class);
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
								Uri uri = Uri.parse("http://beepme.glanznig.com/support-beepme");
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
