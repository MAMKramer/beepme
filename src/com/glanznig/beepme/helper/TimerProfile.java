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

package com.glanznig.beepme.helper;

import com.glanznig.beepme.data.StorageHandler;

public abstract class TimerProfile {
	
	private StorageHandler store;
	public static final long MIN_UPTIME_DURATION = 60; //s = 1 min
	
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
	
	public int getUptimeCountToday() {
		return store.getUptimeCountToday();
	}
	
	public int getNumLastSubsequentCancelledBeeps() {
		return store.getNumLastSubsequentCancelledBeeps();
	}
	
	public abstract long getTimer();

}
