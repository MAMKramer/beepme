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

import android.util.Log;

import com.glanznig.beepme.data.StorageHandler;

public class HciTimerProfile extends TimerProfile {
	
	//all times in seconds
	private static final int avgBeepInterval = 420; //7 min
	private static final int maxBeepInterval = 900; //15 min
	private static final int minBeepInterval = 180; //3 min
	
	private static final int uptimeCountMoveToAverage = 3;
	private static final int numCancelledBeepsMoveToAverage = 2;
	
	private static final String TAG = "HciTimerProfile";
	
	private MersenneTwister rand;
	
	public HciTimerProfile(StorageHandler datastore) {
		super(datastore);
		rand = new MersenneTwister();
	}

	@Override
	public long getTimer() {
		boolean negative = rand.nextBoolean();
		long randTime = 0;
		
		long avg = 0;
		long min = 0;
		long max = 0;
		
		int uptimeCount = getUptimeCountToday();
		int numLastCancelled = getNumLastSubsequentCancelledBeeps();
		
		//start with approximation values
		if (uptimeCount <= uptimeCountMoveToAverage && numLastCancelled < numCancelledBeepsMoveToAverage) {
			avg = avgBeepInterval;
			if (negative) {
				max = avg;
				min = minBeepInterval;
			}
			else {
				max = maxBeepInterval;
				min = avg;
			}
		}
		//later, try to fit beep into "avg beeper uptime today" interval
		else {
			double avgUptimeDuration = getAvgUptimeDurationToday();
			
			avg = Math.round(avgUptimeDuration / 2);
			if (negative) {
				max = avg;
				if (MIN_UPTIME_DURATION < max) {
					min = (long)MIN_UPTIME_DURATION;
				}
				else {
					min = 0;
				}
			}
			else {
				max = Math.round(avgUptimeDuration);
				min = avg;
			}
		}
		
		long randTerm = rand.nextLong(max - min);
		if (negative) {
			randTime = avg - randTerm;
		}
		else {
			randTime = avg + randTerm;
		}
		
		//clamp timer with min(MIN_UPTIME_DURATION)
		if (randTime < MIN_UPTIME_DURATION) {
			randTime = MIN_UPTIME_DURATION;
		}
		
		return randTime;
	}

}
