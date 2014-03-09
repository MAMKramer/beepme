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

package com.glanznig.beepme.helper;

import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.view.BeepActivity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Vibrator;
import android.util.Log;

public class BeepAlertManager implements AudioManager.OnAudioFocusChangeListener {
	
	private MediaPlayer player = null;
	private Context ctx = null;
	AudioManager audioManager = null;
	private Vibrator vibrator = null;
	
	private static final String TAG = "BeepAlertManager";
	
	public BeepAlertManager(Context ctx) {
		this.ctx = ctx;
		audioManager = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
		player = new MediaPlayer();
		vibrator = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	public void startAlert() {
		BeeperApp app = (BeeperApp)((BeepActivity)ctx).getApplication();
		
		if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
			
			initSound();
			if (app.getPreferences().isVibrateAtBeep()) {
				initVibration();
			}
		}
		
		if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT
				|| audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
			// request audio focus that other playing audio is stopped
			audioManager.requestAudioFocus(BeepAlertManager.this, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN);
			
			initVibration();
		}
	}
	
	public void stopAlert() {
		if (player.isPlaying()) {
			player.stop();
		}
		
		vibrator.cancel();
	}
	
	public void cleanUp() {
		player.release();
		
		//abandon audio focus
		audioManager.abandonAudioFocus(BeepAlertManager.this);
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
	
	private void initVibration() {
		//whole length 2353 ms
		//start at 100, vibrate 800 ms, pause 1453 ms
		long[] pattern = { 100, 800, 1453 };
		vibrator.vibrate(pattern, 0);
	}
	
	private void initSound() {
		Resources res = ctx.getResources();
		//beep sound is CC-BY JustinBW
		AssetFileDescriptor alarmSound = res.openRawResourceFd(R.raw.beep);
		player.setAudioStreamType(AudioManager.STREAM_ALARM);
		player.setLooping(true);
		player.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				//request audio focus
				int result = audioManager.requestAudioFocus(BeepAlertManager.this, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN);

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

}
