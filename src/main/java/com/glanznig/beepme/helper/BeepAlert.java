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

public class BeepAlert implements AudioManager.OnAudioFocusChangeListener {
	
	private MediaPlayer player = null;
	private Context ctx = null;
	AudioManager audioManager = null;
	private Vibrator vibrator = null;

    //whole length 2353 ms
    //start at 100, vibrate 800 ms, pause 1453 ms
    private static long[] pattern = { 100, 800, 1453 };
	
	private static final String TAG = "BeepAlertManager";
	
	public BeepAlert(Context ctx) {
		this.ctx = ctx;
		audioManager = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
		vibrator = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);
	}

    public void start() {
        BeeperApp app = (BeeperApp)((BeepActivity)ctx).getApplication();

        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:

                player = new MediaPlayer();
                Resources res = ctx.getResources();
                //beep sound is CC-BY JustinBW
                AssetFileDescriptor alarmSound = res.openRawResourceFd(R.raw.beep);
                player.setAudioStreamType(AudioManager.STREAM_ALARM);
                player.setLooping(true);
                player.setOnPreparedListener(new OnPreparedListener() {
                    public void onPrepared(MediaPlayer mplayer) {
                        //request audio focus
                        int result = audioManager.requestAudioFocus(BeepAlert.this, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN);

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            mplayer.start();
                        }
                    }
                });

                try {
                    player.setDataSource(alarmSound.getFileDescriptor(), alarmSound.getStartOffset(), alarmSound.getLength());
                    player.prepareAsync();
                } catch (Exception e) {
                    Log.e(TAG, "error while playing beep sound", e);
                }

                if (app.getPreferences().isVibrateAtBeep()) {
                    vibrator.vibrate(pattern, 0);
                }
                break;

            case AudioManager.RINGER_MODE_SILENT:
            case AudioManager.RINGER_MODE_VIBRATE:
                vibrator.vibrate(pattern, 0);

                // still request audio focus to stop other playing audio
                audioManager.requestAudioFocus(BeepAlert.this, AudioManager.STREAM_ALARM,
                        AudioManager.AUDIOFOCUS_GAIN);
                break;
        }
    }

    public void stop() {
        if (player != null) {
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
                player.release();
                player = null;

                //abandon audio focus
                audioManager.abandonAudioFocus(BeepAlert.this);
            } catch (IllegalStateException ise) {
            }
        }

        vibrator.cancel();
    }
	
	public void onAudioFocusChange(int focusChange) {
	    switch (focusChange) {
	        case AudioManager.AUDIOFOCUS_GAIN:
	            // resume playback
	            if (player == null) {
                    start();
	            }
	            else {
                    player.setVolume(1.0f, 1.0f);

                    if (!player.isPlaying()) {
                        player.start();
                    }
                }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS:
	            // Lost focus for an unbounded amount of time: stop playback and release media player
	            if (player != null) {
                    if (player.isPlaying()) {
                        player.stop();
                    }
                    player.release();
                    player = null;
                }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	            // Lost focus for a short time, but we have to stop
	            // playback. We don't release the media player because playback
	            // is likely to resume
                if (player != null) {
                    if (player.isPlaying()) {
                        player.pause();
                    }
                }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
	            // Lost focus for a short time, but it's ok to keep playing
	            // at an attenuated level
                if (player != null) {
                    if (player.isPlaying()) {
                        player.setVolume(0.1f, 0.1f);
                    }
                }
	            break;
	    }
	}

}
