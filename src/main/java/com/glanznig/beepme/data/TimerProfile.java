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

Copyright 2012-2014 Michael Glanznig
http://beepme.yourexp.at
*/

package com.glanznig.beepme.data;

import com.glanznig.beepme.db.ScheduledBeepTable;
import com.glanznig.beepme.db.UptimeTable;
import com.glanznig.beepme.helper.MersenneTwister;

import android.content.Context;


public class TimerProfile {
	
	private Long id;
	private String name;
	private int minUptimeDuration;
	private int avgBeepInterval;
	private int maxBeepInterval;
	private int minBeepInterval;
	private int minSizeBeepInterval;
	private int uptimeCountMoveToAverage;
	private int numCancelledBeepsMoveToAverage;
	
	private MersenneTwister randomGenerator;
	
	public TimerProfile(long id) {
		setId(id);
		randomGenerator = new MersenneTwister();
	}
	
	private void setId(long id) {
		this.id = Long.valueOf(id);
	}
	
	public long getId() {
		if (id != null) {
			return id.longValue();
		}
		else {
			return 0L;
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public long getTimer(Context ctx) {
		boolean negative = randomGenerator.nextBoolean();
		long randTime = 0;
		
		long avg = 0;
		long min = 0;
		long max = 0;
		
		long uptimeCount = new UptimeTable(ctx, this).getUptimeDurToday();
		int numLastCancelled = new ScheduledBeepTable(ctx).getNumLastSubsequentCancelledBeeps();
		
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
		} // max > min iff min < avg < max
		
		//later, try to fit beep into "avg beeper uptime today" interval
		else {
			double avgUptimeDuration = new UptimeTable(ctx, this).getAvgUptimeDurToday();
			
			avg = Math.min(Math.round(avgUptimeDuration / 2), avgBeepInterval);
			if (negative) {
				max = avg;
				min = minBeepInterval;
				if (min >= max) {
					min = minUptimeDuration;
					if (min >= max) {
						min = max - minSizeBeepInterval;
					}
				}
			}
			else {
				max = Math.min(Math.round(avgUptimeDuration), maxBeepInterval);
				min = avg;
				if (min >= max) {
					max = min + minSizeBeepInterval;
				}
			}
		} // if max <= min, interval is of size minSizeBeepInterval to make sure that max > min
		
		//must be max > min
		long randTerm = randomGenerator.nextLong(max - min);
		if (negative) {
			randTime = avg - randTerm;
		}
		else {
			randTime = avg + randTerm;
		}
		
		//clamp timer with min(MIN_UPTIME_DURATION)
		if (randTime < minUptimeDuration) {
			randTime = minUptimeDuration;
		}
		
		return randTime;
	}

	public int getMinUptimeDuration() {
		return minUptimeDuration;
	}

	public void setMinUptimeDuration(int minUptimeDuration) {
		this.minUptimeDuration = minUptimeDuration;
	}

	public int getAvgBeepInterval() {
		return avgBeepInterval;
	}

	public void setAvgBeepInterval(int avgBeepInterval) {
		this.avgBeepInterval = avgBeepInterval;
	}

	public int getMaxBeepInterval() {
		return maxBeepInterval;
	}

	public void setMaxBeepInterval(int maxBeepInterval) {
		this.maxBeepInterval = maxBeepInterval;
	}

	public int getMinBeepInterval() {
		return minBeepInterval;
	}

	public void setMinBeepInterval(int minBeepInterval) {
		this.minBeepInterval = minBeepInterval;
	}
	
	public int getMinSizeBeepInterval() {
		return minSizeBeepInterval;
	}
	
	public void setMinSizeBeepInterval(int minSizeBeepInterval) {
		this.minSizeBeepInterval = minSizeBeepInterval;
	}

	public int getUptimeCountMoveToAverage() {
		return uptimeCountMoveToAverage;
	}

	public void setUptimeCountMoveToAverage(int uptimeCountMoveToAverage) {
		this.uptimeCountMoveToAverage = uptimeCountMoveToAverage;
	}

	public int getNumCancelledBeepsMoveToAverage() {
		return numCancelledBeepsMoveToAverage;
	}

	public void setNumCancelledBeepsMoveToAverage(int numCancelledBeepsMoveToAverage) {
		this.numCancelledBeepsMoveToAverage = numCancelledBeepsMoveToAverage;
	}

}
