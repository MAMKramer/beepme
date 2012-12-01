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

import com.glanznig.beepme.data.SampleTable;
import com.glanznig.beepme.data.ScheduledBeepTable;
import com.glanznig.beepme.data.UptimeTable;

public abstract class TimerProfile {
	
	private Context ctx;
	public static final long MIN_UPTIME_DURATION = 60; //s = 1 min
	
	public TimerProfile(Context ctx) {
		this.ctx = ctx;
	}
	
	public int getNumAcceptedToday() {
		return new SampleTable(ctx).getNumAcceptedToday();
	}
	
	public int getSampleCountToday() {
		return new SampleTable(ctx).getSampleCountToday();
	}
	
	public double getRatioAcceptedToday() {
		return new SampleTable(ctx).getRatioAcceptedToday();
	}
	
	public long getUptimeDurationToday() {
		return new UptimeTable(ctx).getUptimeDurToday();
	}
	
	public double getAvgUptimeDurationToday() {
		return new UptimeTable(ctx).getAvgUptimeDurToday();
	}
	
	public int getUptimeCountToday() {
		return new UptimeTable(ctx).getUptimeCountToday();
	}
	
	public int getNumLastSubsequentCancelledBeeps() {
		return new ScheduledBeepTable(ctx).getNumLastSubsequentCancelledBeeps();
	}
	
	public abstract long getTimer();

}
