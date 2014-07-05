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

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

import com.fima.glowpadview.GlowPadView;
import com.fima.glowpadview.GlowPadView.OnTriggerListener;
import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Beep;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.db.MomentTable;
import com.glanznig.beepme.data.util.PreferenceHandler;
import com.glanznig.beepme.data.util.Statistics;
import com.glanznig.beepme.helper.BeepAlert;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class BeepActivity extends Activity {
	
	private static class TimeoutHandler extends Handler {
		WeakReference<BeepActivity> beepActivity;
		
		TimeoutHandler(BeepActivity activity) {
			beepActivity = new WeakReference<BeepActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message message) {
			if (beepActivity.get() != null) {
				beepActivity.get().decline();
			}
		}
	}
	
	// Controller for GlowPadView (thanks to AOSP)
    private class GlowPadController extends Handler implements OnTriggerListener {
        private static final int PING_MESSAGE_WHAT = 101;
        private static final long PING_AUTO_REPEAT_DELAY_MSEC = 1200;

        public void startPinger() {
            sendEmptyMessage(PING_MESSAGE_WHAT);
        }

        public void stopPinger() {
            removeMessages(PING_MESSAGE_WHAT);
        }

        @Override
        public void handleMessage(Message msg) {
            ping();
            sendEmptyMessageDelayed(PING_MESSAGE_WHAT, PING_AUTO_REPEAT_DELAY_MSEC);
        }

        @Override
        public void onGrabbed(View v, int handle) {
            stopPinger();
        }

        @Override
        public void onReleased(View v, int handle) {
            startPinger();

        }

        @Override
    	public void onTrigger(View v, int target) {
    		final int resId = acceptDeclineHandle.getResourceIdForTarget(target);
    		switch (resId) {
    			case R.drawable.ic_item_accept:
    				accept();
    				break;

    			case R.drawable.ic_item_decline:
    				decline();
    				break;
    				
    			case R.drawable.ic_item_beeper_off:
    				declinePause();
    				break;
    		}
    	}

        @Override
        public void onGrabbedStateChange(View v, int handle) {
        }

        @Override
        public void onFinishFinalAnimation() {
        }
    }

    private static final String TAG = "BeepActivity";

	public static final String CANCEL_INTENT = "com.glanznig.beepme.DECLINE_BEEP";
    public static final int BEEP_TIMEOUT = 15;

    private GlowPadView acceptDeclineHandle;
    private GlowPadController glowPadCtrl = new GlowPadController();
	
	private long beepTimestamp;
	private BeepAlert alert = null;
	private TimeoutHandler handler = null;
	private BroadcastReceiver cancelReceiver = null;
    private BroadcastReceiver screenStateReceiver = null;
    private boolean inDecline = false;
    private boolean hasAccepted = false;
	
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        final BeepMeApp app = (BeepMeApp)getApplication();

        if (savedState != null) {
            if (!savedState.containsKey("beepTimestamp")) {
                // set beep timestamp to NOW
                beepTimestamp = Calendar.getInstance().getTimeInMillis();
                app.updateBeep(Beep.BeepStatus.RECEIVED);
            } else {
                beepTimestamp = savedState.getLong("beepTimestamp");
            }
        }
        else {
            // set beep timestamp to NOW
            beepTimestamp = Calendar.getInstance().getTimeInMillis();
            app.updateBeep(Beep.BeepStatus.RECEIVED);
        }
        handler = new TimeoutHandler(BeepActivity.this);

        // decline and pause beeper if active call
        if (app.getPreferences().getPauseBeeperDuringCall() && app.getPreferences().isCall()) {
            app.setBeeperStatus(PreferenceHandler.BeeperStatus.INACTIVE_AFTER_CALL);
            decline();
            return;
        }

        updateLayout();

        // set up broadcast receiver to detect screen events
        screenStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    decline();
                }
            }
        };
        registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

		// set up broadcast receiver to decline beep at incoming call
		cancelReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(CANCEL_INTENT)) {
					if (app.getPreferences().getPauseBeeperDuringCall()) {
						app.setBeeperStatus(PreferenceHandler.BeeperStatus.INACTIVE_AFTER_CALL);
						decline();
					}
				}
			}
		};
        registerReceiver(cancelReceiver, new IntentFilter(CANCEL_INTENT));
		
		alert = new BeepAlert(BeepActivity.this);

		int numAccepted = Statistics.getNumMomentsAcceptedToday(this.getApplicationContext());
		int numDeclined = Statistics.getNumMomentsDeclinedToday(this.getApplicationContext());
		long uptimeDur = Statistics.getUptimeDurationToday(this.getApplicationContext());
		
		TextView acceptedToday = (TextView)findViewById(R.id.beep_accepted_today);
		TextView declinedToday = (TextView)findViewById(R.id.beep_declined_today);
		TextView beeperActive = (TextView)findViewById(R.id.beep_elapsed_today);
		
		String timeActive = String.format("%02d:%02d:%02d", uptimeDur/3600, (uptimeDur%3600)/60, (uptimeDur%60));
		
		acceptedToday.setText(String.valueOf(numAccepted));
		declinedToday.setText(String.valueOf(numDeclined));
		beeperActive.setText(String.valueOf(timeActive));
	}

    @Override
    protected void onPause() {
        super.onPause();

        glowPadCtrl.stopPinger();
        alert.stop();
        handler.removeMessages(BEEP_TIMEOUT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        long timeSinceBeep = Math.abs(Calendar.getInstance().getTimeInMillis() - beepTimestamp);
        if (timeSinceBeep < 30000) {
            // max 30 sec timeout for beep
            handler.sendEmptyMessageDelayed(BEEP_TIMEOUT, 30000 - timeSinceBeep);
        }
        else {
            // timeout already elapsed
            decline();
        }

        glowPadCtrl.startPinger();
        alert.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenStateReceiver);
        unregisterReceiver(cancelReceiver);
    }
	
	private void ping() {
        acceptDeclineHandle.ping();
    }

    private void updateLayout() {
        final LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.beep, null);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        setContentView(view);

        acceptDeclineHandle = (GlowPadView)findViewById(R.id.beep_glowpad);
        acceptDeclineHandle.setOnTriggerListener(glowPadCtrl);
        glowPadCtrl.startPinger();
        //acceptDeclineHandle.setShowTargetsOnIdle(true);
    }
	
	public void accept() {
        hasAccepted = true;
		if (alert != null) {
			alert.stop();
		}
		
		Intent accept = new Intent(BeepActivity.this, ChangeMomentActivity.class);
		accept.putExtra(getApplication().getClass().getPackage().getName() + ".Timestamp", beepTimestamp);
		startActivity(accept);
	}
	
	public void declinePause() {
		BeepMeApp app = (BeepMeApp)getApplication();
		decline();
        app.setBeeperStatus(PreferenceHandler.BeeperStatus.INACTIVE);
	}
	
	public void decline() {
		if (!inDecline) { // should only be called once
            inDecline = true;

            BeepMeApp app = (BeepMeApp) getApplication();
            Moment moment = new Moment();
            moment.setProjectUid(app.getPreferences().getProjectId());
            moment.setTimestamp(new Date(beepTimestamp));
            moment.setAccepted(false);
            moment.setUptimeUid(app.getCurrentUptime().getUid());
            new MomentTable(this.getApplicationContext()).addMoment(moment);
            app.scheduleBeep();

            if (!BeepActivity.this.isFinishing()) {
                finish();
            }
        }
	}

    @Override
    protected void onUserLeaveHint() {
        // user leaves the activity
        if (!hasAccepted) {
            decline();
        }
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to decline.
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                decline();
                return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateLayout();
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putLong("beepTimestamp", beepTimestamp);
    }
}
