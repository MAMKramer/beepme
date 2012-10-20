package com.glanznig.beeper;

import java.util.List;

import com.glanznig.beeper.data.Sample;
import com.glanznig.beeper.data.StorageHandler;
import com.glanznig.beeper.data.Tag;

import android.app.Application;

public class BeeperApp extends Application {
	
	private StorageHandler store;
	
	public Sample getSample(long id) {
		return store.getSample(id);
	}
	
	public Sample getSampleWithTags(long id) {
		return store.getSampleWithTags(id);
	}
	
	public List<Sample> getSamples() {
		return store.getSamples();
	}
	
	public Sample addSample(Sample s) {
		return store.addSample(s);
	}
	
	public boolean editSample(Sample s) {
		return store.editSample(s);
	}
	
	public Tag addTag(String name, Sample s) {
		return store.addTag(name, s);
	}
	
	public boolean removeTag(Tag t, Sample s) {
		return store.removeTag(t, s);
	}
	
	public List<Tag> getTags(String search) {
		return store.getTags(search);
	}
	
	public List<Tag> getTags() {
		return store.getTags();
	}
	
	public void onCreate() {
		super.onCreate();
		
		store = new StorageHandler(this.getApplicationContext());
	}

}

