package com.glanznig.beeper;

import java.util.List;

import com.glanznig.beeper.data.Sample;
import com.glanznig.beeper.data.StorageHandler;

import android.app.Application;

public class BeeperApp extends Application {
	
	private StorageHandler store;
	
	public List<Sample> getSamples() {
		return store.getSamples();
	}
	
	public void onCreate() {
		super.onCreate();
		
		store = new StorageHandler(this.getApplicationContext());
	}

}

