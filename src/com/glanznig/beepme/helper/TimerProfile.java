package com.glanznig.beepme.helper;

import com.glanznig.beepme.data.StorageHandler;

public abstract class TimerProfile {
	
	private StorageHandler store;
	
	public TimerProfile(StorageHandler datastore) {
		store = datastore;
	}
	
	public int getNumAcceptedToday() {
		return store.getNumAcceptedToday();
	}
	
	public int getSampleCountToday() {
		return store.getSampleCountToday();
	}
	
	public double getRatioAcceptedToday() {
		return store.getRatioAcceptedToday();
	}
	
	public long getUptimeDurationToday() {
		return store.getUptimeDurToday();
	}
	
	public double getAvgUptimeDurationToday() {
		return store.getAvgUptimeDurToday();
	}
	
	public abstract int getTimer();

}
