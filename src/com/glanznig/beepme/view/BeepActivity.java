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

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

import com.fima.glowpadview.GlowPadView;
import com.fima.glowpadview.GlowPadView.OnTriggerListener;
import com.glanznig.beepme.BeeperApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Sample;
import com.glanznig.beepme.data.SampleTable;
import com.glanznig.beepme.data.ScheduledBeepTable;
import com.glanznig.beepme.data.UptimeTable;
import com.glanznig.beepme.helper.BeepAlertManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class BeepActivity extends Activity {
	
	private static final String TAG = "BeepActivity";
	
	private GlowPadView acceptDeclineHandle;
	private GlowPadController glowPadCtrl = new GlowPadController();
	
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
	
	private static class ScreenStateReceiver extends BroadcastReceiver {
		WeakReference<BeepActivity> beepActivity;
		
		ScreenStateReceiver(BeepActivity activity) {
			beepActivity = new WeakReference<BeepActivity>(activity);
		}

	    @Override
	    public void onReceive(final Context context, final Intent intent) {
	        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
	            if (beepActivity.get() != null) {
	            	beepActivity.get().finish();
	            }
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
	
	public static final String CANCEL_INTENT = "com.glanznig.beepme.DECLINE_BEEP";
	
	private Date beepTime = null;
	private BeepAlertManager alertManager = null;
	private PowerManager.WakeLock lock = null;
	private TimeoutHandler handler = null;
	private ScreenStateReceiver receiver = null;
	private BroadcastReceiver cancelReceiver = null;
	
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
	public void onStop() {
		super.onStop();
		
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		
		if (cancelReceiver != null) {
			unregisterReceiver(cancelReceiver);
		}
		
		if (handler != null) {
			handler.removeMessages(1);
		}
		
		//release wake lock
		if (lock != null && lock.isHeld()) {
			lock.release();
		}
		
		if (alertManager != null) {
			alertManager.cleanUp();
		}
		
		if (!BeepActivity.this.isFinishing()) {
			finish();
		}
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        glowPadCtrl.startPinger();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glowPadCtrl.stopPinger();
    }
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//acquire wake lock
		PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		lock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
		if (lock != null && !lock.isHeld()) {
			lock.acquire();
		}
		
		//record beep time
		beepTime = Calendar.getInstance().getTime();
		
		BeeperApp app = (BeeperApp)getApplication();
		new ScheduledBeepTable(this.getApplicationContext()).receivedScheduledBeep(app.getPreferences().getScheduledBeepId(), Calendar.getInstance().getTimeInMillis());
		
		//decline and pause beeper if active call
		if (app.getPreferences().getPauseBeeperDuringCall() && app.getPreferences().isCall()) {
			app.setBeeperActive(BeeperApp.BEEPER_INACTIVE_AFTER_CALL);
			decline();
			return;
		}
		
		//set up broadcast receiver to decline beep at incoming call
		cancelReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(CANCEL_INTENT)) {
					BeeperApp app = (BeeperApp)getApplication();
					if (app.getPreferences().getPauseBeeperDuringCall()) {
						app.setBeeperActive(BeeperApp.BEEPER_INACTIVE_AFTER_CALL);
						decline();
					}
				}
			}
		};
		registerReceiver(cancelReceiver, new IntentFilter(CANCEL_INTENT));
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		receiver = new ScreenStateReceiver(BeepActivity.this);
        registerReceiver(receiver, filter);
        
		setContentView(R.layout.beep);
		
		alertManager = new BeepAlertManager(BeepActivity.this);
		alertManager.startAlert();
		
		setVolumeControlStream(AudioManager.STREAM_ALARM);
		
		handler = new TimeoutHandler(BeepActivity.this);
		handler.sendEmptyMessageDelayed(1, 30000); // 30 sec timeout for activity
		
		acceptDeclineHandle = (GlowPadView)findViewById(R.id.beep_glowpad);
		acceptDeclineHandle.setOnTriggerListener(glowPadCtrl);
		glowPadCtrl.startPinger();
		//acceptDeclineHandle.setShowTargetsOnIdle(true);
		
		SampleTable st = new SampleTable(this.getApplicationContext());
		int numAccepted = st.getNumAcceptedToday();
		int numDeclined = st.getSampleCountToday() - numAccepted;
		long uptimeDur = new UptimeTable(this.getApplicationContext(), app.getTimerProfile()).getUptimeDurToday();
		
		TextView acceptedToday = (TextView)findViewById(R.id.beep_accepted_today);
		TextView declinedToday = (TextView)findViewById(R.id.beep_declined_today);
		TextView beeperActive = (TextView)findViewById(R.id.beep_elapsed_today);
		
		String timeActive = String.format("%02d:%02d:%02d", uptimeDur/3600, (uptimeDur%3600)/60, (uptimeDur%60));
		
		acceptedToday.setText(String.valueOf(numAccepted));
		declinedToday.setText(String.valueOf(numDeclined));
		beeperActive.setText(String.valueOf(timeActive));
	}
	
	private void ping() {
        acceptDeclineHandle.ping();
    }
	
	public void accept() {
		if (alertManager != null) {
			alertManager.stopAlert();
		}
		
		BeeperApp app = (BeeperApp)getApplication();
		app.acceptTimer();
		
		Intent accept = new Intent(BeepActivity.this, NewSampleActivity.class);
		accept.putExtra(getApplication().getClass().getPackage().getName() + ".Timestamp", beepTime.getTime());
		startActivity(accept);
		finish();
	}
	
	public void declinePause() {
		BeeperApp app = (BeeperApp)getApplication();
		app.setBeeperActive(BeeperApp.BEEPER_INACTIVE);
		decline();
	}
	
	public void decline() {
		if (alertManager != null) {
			alertManager.stopAlert();
		}
		
		BeeperApp app = (BeeperApp)getApplication();
		Sample sample = new Sample();
		sample.setTimestamp(beepTime);
		sample.setAccepted(false);
		sample.setUptimeId(app.getPreferences().getUptimeId());
		new SampleTable(this.getApplicationContext()).addSample(sample);
		app.declineTimer();
		app.setTimer();
		finish();
	}
}
