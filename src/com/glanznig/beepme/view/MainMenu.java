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

package com.glanznig.beepme.view;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.DataExporter;
import com.glanznig.beepme.data.SampleTable;
import com.glanznig.beepme.data.ScheduledBeepTable;
import com.glanznig.beepme.data.UptimeTable;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff.Mode;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainMenu extends Activity {
	
	private static final String TAG = "MainMenu";
	private AudioManager audioManager = null;
	private Menu actionMenu = null;
	
	private static class ExportHandler extends Handler {
		WeakReference<MainMenu> mainMenu;
		Bundle data;
		
		ExportHandler(MainMenu activity) {
			mainMenu = new WeakReference<MainMenu>(activity);
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
		WeakReference<MainMenu> mainMenu;
		WeakReference<ExportHandler> handler;
		
		ExportRunnable(MainMenu activity, ExportHandler handler) {
			mainMenu = new WeakReference<MainMenu>(activity);
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
		setContentView(R.layout.main_menu);
		
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		populateFields();
		
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
	
	private void populateFields() {
		BeeperApp app = (BeeperApp)getApplication();
		
		ActionBar bar = getActionBar();
		if (app.getPreferences().isTestMode()) {
			bar.setSubtitle(getString(R.string.pref_title_test_mode));
		}
		
		SampleTable st = new SampleTable(this.getApplicationContext());
		int numAccepted = st.getNumAcceptedToday();
		int numDeclined = st.getSampleCountToday() - numAccepted;
		long uptimeDur = new UptimeTable(this.getApplicationContext(), app.getTimerProfile()).getUptimeDurToday();
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
	
	public void onClickListSamples(View view) {
		startActivity(new Intent(MainMenu.this, ListSamplesActivity.class));
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
		BeeperApp app = (BeeperApp)getApplication();
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main_menu, menu);
        this.actionMenu = menu;
        
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
        
        return super.onCreateOptionsMenu(menu);
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
            			new Thread(new ExportRunnable(MainMenu.this, new ExportHandler(MainMenu.this))).start();
            		}
            	}
            	else {
            		Toast.makeText(MainMenu.this, R.string.sdcard_error, Toast.LENGTH_SHORT).show();
            	}
            	return true;
            	
            case R.id.action_settings:
                Intent iSettings = new Intent(this, Preferences.class);
                startActivity(iSettings);
                return true;
                
            case R.id.action_about:
            	Intent iAbout = new Intent(this, AboutActivity.class);
                startActivity(iAbout);
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
