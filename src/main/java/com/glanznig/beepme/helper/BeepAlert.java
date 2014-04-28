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

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.view.BeepActivity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

public class BeepAlert implements AudioManager.OnAudioFocusChangeListener {

    private SoundPool soundPool = null;
    private int soundID;
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
        BeepMeApp app = (BeepMeApp)((BeepActivity)ctx).getApplication();

        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:

                Resources res = ctx.getResources();
                final AssetFileDescriptor alarmSound = res.openRawResourceFd(
                        Integer.valueOf(app.getPreferences().getBeepSoundId()));

                soundPool = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
                soundID = soundPool.load(alarmSound, 1);
                soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int i, int i2) {
                        //request audio focus
                        int result = audioManager.requestAudioFocus(BeepAlert.this,
                                AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN);

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            soundPool.play(soundID, 1.0f, 1.0f, 1, -1, 1f);
                        }
                    }
                });

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
        if (soundPool != null) {
            soundPool.stop(soundID);
            soundPool.release();
        }

        //abandon audio focus
        audioManager.abandonAudioFocus(BeepAlert.this);

        vibrator.cancel();
    }
	
	public void onAudioFocusChange(int focusChange) {
	    switch (focusChange) {
	        case AudioManager.AUDIOFOCUS_GAIN:
	            // resume playback
                if (soundPool == null) {
                    start();
                }
                else {
                    soundPool.setVolume(soundID, 1.0f, 1.0f);
                    soundPool.resume(soundID);
                }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS:
	            // Lost focus for an unbounded amount of time: stop playback and release media player
                if (soundPool != null) {
                    soundPool.stop(soundID);
                    soundPool.release();
                }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	            // Lost focus for a short time, but we have to stop
	            // playback. We don't release the media player because playback
	            // is likely to resume
                if (soundPool != null) {
                    soundPool.pause(soundID);
                }
	            break;

	        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
	            // Lost focus for a short time, but it's ok to keep playing
	            // at an attenuated level
                if (soundPool != null) {
                    soundPool.setVolume(soundID, 0.1f, 0.1f);
                }
	            break;
	    }
	}

}
