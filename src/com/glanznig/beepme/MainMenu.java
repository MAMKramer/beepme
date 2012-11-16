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

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.glanznig.beepme.data.DataExporter;

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
			if (mainMenu != null) {
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
			if (mainMenu != null) {
				DataExporter exporter = new DataExporter(mainMenu.get().getApplicationContext());
				String fileName = exporter.exportToZipFile();
				if (handler != null) {
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
		
		labelTestMode.measure(0, 0);
		int labelHeight = labelTestMode.getMeasuredHeight();
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
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		float scale = getResources().getDisplayMetrics().density;
		lp.setMargins(0, ((displayHeight - buttonsHeight - labelHeight - (int)(5 * scale + 0.5f)) / 2) - (iconHeight / 2) - 10, 0, 0);
		beeperStatus.setLayoutParams(lp);
		
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
			app.clearTimer(); //call before setBeeperActive
			app.setBeeperActive(false);
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
            case R.id.menu_export:
            	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            		new Thread(new ExportRunnable(MainMenu.this, new ExportHandler(MainMenu.this))).start();
            	}
            	else {
            		Toast.makeText(MainMenu.this, R.string.sdcard_error, Toast.LENGTH_SHORT).show();
            	}
            	return true;
            case R.id.menu_info:
            	Intent iAbout = new Intent(this, AboutActivity.class);
                startActivity(iAbout);
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
