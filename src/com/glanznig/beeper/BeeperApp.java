package com.glanznig.beeper;

import java.util.List;

import com.glanznig.beeper.data.Sample;
import com.glanznig.beeper.data.StorageHandler;

import android.app.Application;

public class BeeperApp extends Application {
	
	private StorageHandler store;
	
	public Sample getSample(long id) {
		return store.getSample(id);
	}
	
	public List<Sample> getSamples() {
		return store.getSamples();
	}
	
	public boolean addSample(Sample s) {
		return store.addSample(s);
	}
	
	public boolean editSample(Sample s) {
		return store.editSample(s);
	}
	
	public void onCreate() {
		super.onCreate();
		
		store = new StorageHandler(this.getApplicationContext());
	}

}

