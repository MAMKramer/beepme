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

import android.content.Context;
import android.util.Log;

public class HciTimerProfile extends TimerProfile {
	
	//all times in seconds
	public static final int avgBeepInterval = 300; //5 min
	public static final int maxBeepInterval = 600; //10 min
	public static final int minBeepInterval = 120; //2 min
	
	private static final int uptimeCountMoveToAverage = 3;
	private static final int numCancelledBeepsMoveToAverage = 2;
	
	private static final String TAG = "HciTimerProfile";
	
	private MersenneTwister rand;
	
	public HciTimerProfile(Context ctx) {
		super(ctx);
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
			
			avg = Math.min(Math.round(avgUptimeDuration / 2), GeneralTimerProfile.avgBeepInterval);
			if (negative) {
				max = avg;
				min = minBeepInterval;
				if (min >= max) {
					min = MIN_UPTIME_DURATION;
				}
			}
			else {
				max = Math.min(Math.round(avgUptimeDuration), GeneralTimerProfile.maxBeepInterval);
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
