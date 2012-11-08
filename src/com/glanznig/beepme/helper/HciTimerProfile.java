package com.glanznig.beepme.helper;

import com.glanznig.beepme.data.StorageHandler;

public class HciTimerProfile extends TimerProfile {
	
	//all times in seconds
	private static final int avgBeepInterval = 420; //7 min
	private static final int maxBeepInterval = 900; //15 min
	private static final int minBeepInterval = 180; //3 min
	
	public HciTimerProfile(StorageHandler datastore) {
		super(datastore);
	}

	@Override
	public int getTimer() {
		// TODO Auto-generated method stub
		return 0;
	}

}
