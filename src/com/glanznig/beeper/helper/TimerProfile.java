package com.glanznig.beeper.helper;

import com.glanznig.beeper.data.StorageHandler;

public abstract class TimerProfile {
	
	private StorageHandler store;
	
	public TimerProfile(StorageHandler datastore) {
		store = datastore;
	}
	
	public int getNumAcceptedToday() {
		return store.getNumAcceptedToday();
	}
	
	public float getSampleCountToday() {
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
