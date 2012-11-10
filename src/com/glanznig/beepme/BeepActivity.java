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

import java.lang.ref.WeakReference;
import java.util.Date;

import com.glanznig.beepme.data.Sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class BeepActivity extends Activity implements AudioManager.OnAudioFocusChangeListener {
	
	private static final String TAG = "BeepActivity";
	
	private static class TimeoutHandler extends Handler {
		WeakReference<BeepActivity> beepActivity;
		
		TimeoutHandler(BeepActivity activity) {
			beepActivity = new WeakReference<BeepActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message message) {
			if (beepActivity != null) {
				beepActivity.get().decline();
			}
		}
	}
	
	private Date beepTime = null;
	private MediaPlayer player = null;
	private Vibrator vibrator = null;
	private PowerManager.WakeLock lock = null;
	private TimeoutHandler handler = null;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		//on home key threat beep as declined
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			decline();
			return false;
		}
		
		//block back key
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (handler != null) {
			handler.removeMessages(1);
		}
		
		//release wake lock
		if (lock != null && lock.isHeld()) {
			lock.release();
		}
		
		if (player != null) {
			player.stop();
			player.release();
			
			//abandon audio focus
			AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			audioManager.abandonAudioFocus(BeepActivity.this);
		}
		
		if (vibrator != null) {
			vibrator.cancel();
		}
		
		finish();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.beep);
		
		LinearLayout buttons = (LinearLayout)findViewById(R.id.beep_buttons);
		buttons.measure(0, 0);
		int buttonsHeight = buttons.getMeasuredHeight();
		int displayHeight = getWindowManager().getDefaultDisplay().getHeight();
		ImageView beepIcon = (ImageView)findViewById(R.id.beep_icon);
		beepIcon.measure(0, 0);
		int iconHeight = beepIcon.getMeasuredHeight();
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, ((displayHeight - buttonsHeight) / 2) - (iconHeight / 2), 0, 0);
		beepIcon.setLayoutParams(lp);
		
		BeeperApp app = (BeeperApp)getApplication();
		
		handler = new TimeoutHandler(BeepActivity.this);
		handler.sendEmptyMessageDelayed(1, 60000); // 1 minute timeout for activity
		
		//acquire wake lock
		PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		lock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		if (lock != null && !lock.isHeld()) {
			lock.acquire();
		}
		
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
			
			initSound();
			if (app.getPreferences().isVibrateAtBeep()) {
				initVibration();
			}
		}
		
		if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT
				|| audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
			initVibration();
		}
		
		Button accept = (Button)findViewById(R.id.beep_btn_accept);
		Button decline = (Button)findViewById(R.id.beep_btn_decline);
		Button decline_pause = (Button)findViewById(R.id.beep_btn_decline_pause);
		//get display dimensions
		Display display = getWindowManager().getDefaultDisplay();
		int width = (display.getWidth() - 40) / 2;
		decline.setWidth(width);
		decline_pause.setWidth(width);
		PorterDuffColorFilter green = new PorterDuffColorFilter(Color.rgb(130, 217, 130), Mode.MULTIPLY); // was 96, 191, 96
		PorterDuffColorFilter red = new PorterDuffColorFilter(Color.rgb(217, 130, 130), Mode.MULTIPLY); // was 191, 96, 96
		accept.getBackground().setColorFilter(green);
		decline.getBackground().setColorFilter(red);
		decline_pause.getBackground().setColorFilter(red);
		
		int numAccepted = app.getDataStore().getNumAcceptedToday();
		int numDeclined = app.getDataStore().getSampleCountToday() - numAccepted;
		TextView acceptedToday = (TextView)findViewById(R.id.beep_accepted_today);
		TextView declinedToday = (TextView)findViewById(R.id.beep_declined_today);
		String accepted = String.format(getString(R.string.beep_accepted_today), numAccepted);
		String declined = String.format(getString(R.string.beep_declined_today), numDeclined);
		acceptedToday.setText(accepted);
		acceptedToday.setTextColor(Color.rgb(130, 217, 130));
		declinedToday.setText(declined);
		declinedToday.setTextColor(Color.rgb(217, 130, 130));
		
		//record beep time
		beepTime = new Date();
	}
	
	private void initVibration() {
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		//whole length 2353 ms
		//start at 100, vibrate 800 ms, pause 1453 ms
		long[] pattern = { 100, 800, 1453 };
		vibrator.vibrate(pattern, 0);
	}
	
	private void initSound() {
		Resources res = getResources();
		//beep sound is CC-BY JustinBW
		AssetFileDescriptor alarmSound = res.openRawResourceFd(R.raw.beep);
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_ALARM);
		setVolumeControlStream(AudioManager.STREAM_ALARM);
		player.setLooping(true);
		player.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				//request audio focus
				AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				int result = audioManager.requestAudioFocus(BeepActivity.this, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN);

				if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
					mp.start();
				}
			}
		});
		
		try {
			player.setDataSource(alarmSound.getFileDescriptor(), alarmSound.getStartOffset(), alarmSound.getLength());
			player.prepareAsync();
		} catch (Exception e) {
			Log.e(TAG, "error while playing beep sound", e);
		}
	}
	
	public void onClickAccept(View view) {
		Intent accept = new Intent(BeepActivity.this, NewSampleActivity.class);
		accept.putExtra(getApplication().getClass().getPackage().getName() + ".Timestamp", beepTime.getTime());
		startActivity(accept);
		finish();
	}
	
	public void onClickDecline(View view) {
		if (player != null) {
			player.stop();
		}
		
		if (vibrator != null) {
			vibrator.cancel();
		}
		
		decline();
	}
	
	public void onClickDeclinePause(View view) {
		if (player != null) {
			player.stop();
		}
		
		if (vibrator != null) {
			vibrator.cancel();
		}
		
		BeeperApp app = (BeeperApp)getApplication();
		app.setBeeperActive(false);
		decline();
	}
	
	public void decline() {
		if (player != null) {
			player.stop();
		}
		
		if (vibrator != null) {
			vibrator.cancel();
		}
		
		BeeperApp app = (BeeperApp)getApplication();
		Sample sample = new Sample();
		sample.setTimestamp(beepTime);
		sample.setAccepted(false);
		app.getDataStore().addSample(sample);
		app.cancelCurrentScheduledBeep(); //mark beep as cancelled/unsuccessful because it was declined
		app.setTimer();
		finish();
	}
	
	public void onAudioFocusChange(int focusChange) {
	    switch (focusChange) {
	        case AudioManager.AUDIOFOCUS_GAIN:
	            // resume playback
	            if (player == null) {
	            	initSound();
	            }
	            else if (!player.isPlaying()) {
	            	player.start();
	            }
	            player.setVolume(1.0f, 1.0f);
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS:
	            // Lost focus for an unbounded amount of time: stop playback and release media player
	            if (player.isPlaying()) {
	            	player.stop();
	            }
	            player.release();
	            player = null;
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	            // Lost focus for a short time, but we have to stop
	            // playback. We don't release the media player because playback
	            // is likely to resume
	            if (player.isPlaying()) {
	            	player.pause();
	            }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
	            // Lost focus for a short time, but it's ok to keep playing
	            // at an attenuated level
	            if (player.isPlaying()) {
	            	player.setVolume(0.1f, 0.1f);
	            }
	            break;
	    }
	}
}
