package com.glanznig.beeper;

import java.util.Date;

import com.glanznig.beeper.data.Sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class BeepActivity extends Activity {
	
	private Date beepTime = null;
	
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
		finish();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.beep);
		
		Button accept = (Button)findViewById(R.id.beep_btn_accept);
		Button decline = (Button)findViewById(R.id.beep_btn_decline);
		Button decline_pause = (Button)findViewById(R.id.beep_btn_decline_pause);
		//get display dimensions
		Display display = getWindowManager().getDefaultDisplay();
		int width = (display.getWidth() - 40) / 2;
		decline.setWidth(width);
		decline_pause.setWidth(width);
		PorterDuffColorFilter green = new PorterDuffColorFilter(Color.rgb(96, 191, 96), Mode.MULTIPLY);
		PorterDuffColorFilter red = new PorterDuffColorFilter(Color.rgb(191, 96, 96), Mode.MULTIPLY);
		accept.getBackground().setColorFilter(green);
		decline.getBackground().setColorFilter(red);
		decline_pause.getBackground().setColorFilter(red);
		
		//record beep time
		beepTime = new Date();
	}
	
	public void onClickAccept(View view) {
		Intent accept = new Intent(BeepActivity.this, NewSampleActivity.class);
		accept.putExtra(getApplication().getClass().getPackage().getName() + ".Timestamp", beepTime.getTime());
		startActivity(accept);
		finish();
	}
	
	public void onClickDecline(View view) {
		decline();
	}
	
	public void onClickDeclinePause(View view) {
		BeeperApp app = (BeeperApp)getApplication();
		app.setBeeperActive(false);
		decline();
	}
	
	public void decline() {
		BeeperApp app = (BeeperApp)getApplication();
		Sample sample = new Sample();
		sample.setTimestamp(beepTime);
		sample.setAccepted(false);
		app.getDataStore().addSample(sample);
		finish();
	}
}
