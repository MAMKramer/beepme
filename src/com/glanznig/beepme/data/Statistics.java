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
http://beepme.glanznig.com
*/

package com.glanznig.beepme.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.glanznig.beepme.db.SampleTable;
import com.glanznig.beepme.db.UptimeTable;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class Statistics {
	
	private static final String TAG = "Statistics";
	
	private static class NegativeComparator implements Comparator<Long> {

		@Override
		public int compare(Long lhs, Long rhs) {
			if (lhs.longValue() > rhs.longValue()) {
				return -1;
			}
			if (lhs.longValue() < rhs.longValue()) {
				return 1;
			}
			else {
				return 0;
			}
		}
		
	}
	
	public static int getSampleCountToday(Context ctx) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		
		return getSampleCount(ctx, today);
	}
	
	public static int getSampleCount(Context ctx, Calendar day) {
		SampleTable sTbl = new SampleTable(ctx.getApplicationContext());
		List<Sample> samples = sTbl.getSamplesOfDay(day);
		
		if (samples != null && samples.size() > 0) {
			return samples.size();
		}
		return 0;
	}
	
	public static int getNumSamplesDeclinedToday(Context ctx) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		
		return getNumSamplesDeclined(ctx, today);
	}
	
	public static int getNumSamplesDeclined(Context ctx, Calendar day) {
		SampleTable sTbl = new SampleTable(ctx.getApplicationContext());
		List<Sample> samples = sTbl.getSamplesOfDay(day);
		
		if (samples != null && samples.size() > 0) {
			int declined = 0;
			for (int i = 0; i < samples.size(); i++) {
				Sample s = samples.get(i);
				if (s.getAccepted().equals(Boolean.FALSE)) {
					declined += 1;
				}
			}
			
			return declined;
		}
		return 0;
	}
	
	public static int getNumSamplesAcceptedToday(Context ctx) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		
		return getNumSamplesAccepted(ctx, today);
	}
	
	public static int getNumSamplesAccepted(Context ctx, Calendar day) {
		SampleTable sTbl = new SampleTable(ctx.getApplicationContext());
		List<Sample> samples = sTbl.getSamplesOfDay(day);
		
		if (samples != null && samples.size() > 0) {
			int accepted = 0;
			for (int i = 0; i < samples.size(); i++) {
				Sample s = samples.get(i);
				if (s.getAccepted().equals(Boolean.TRUE)) {
					accepted += 1;
				}
			}
			
			return accepted;
		}
		return 0;
	}
	
	public static Bundle getStatsOfToday(Context ctx, TimerProfile profile) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		
		return getStatsOfDay(ctx, profile, today);
	}
	
	public static Bundle getStatsOfDay(Context ctx, TimerProfile profile, Calendar day) {
		long uptimeDur = getUptimeDuration(ctx, profile, day);
		int accepted = 0;
		int declined = 0;
		
		SampleTable sTbl = new SampleTable(ctx.getApplicationContext());
		List<Sample> samples = sTbl.getSamplesOfDay(day);
		
		if (samples != null && samples.size() > 0) {
			for (int i = 0; i < samples.size(); i++) {
				Sample s = samples.get(i);
				if (s.getAccepted().equals(Boolean.TRUE)) {
					accepted += 1;
				}
				else {
					declined += 1;
				}
			}
		}
		
		Bundle b = new Bundle();
		b.putLong("uptimeDuration", uptimeDur);
		b.putInt("acceptedSamples", accepted);
		b.putInt("declinedSamples", declined);
		b.putInt("countSamples", accepted+declined);
		
		return b;
	}
	
	public static List<Bundle> getStats(Context ctx, TimerProfile profile) {
		UptimeTable upTbl = new UptimeTable(ctx.getApplicationContext(), profile);
		List<Uptime> uptimes = upTbl.getUptimes();
		SampleTable sTbl = new SampleTable(ctx.getApplicationContext());
		List<Sample> samples = sTbl.getSamples(true);
		
		if (uptimes != null && samples != null) {
			TreeMap<Long, Bundle> map = new TreeMap<Long, Bundle>(new NegativeComparator());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			
			if (uptimes.size() > 0 && samples.size() > 0) {
				
				// handle samples (accepted, declined, count)
				Iterator<Sample> sampleIterator = samples.iterator();
				Bundle item = null;
				Sample s = null;
				
				while (sampleIterator.hasNext()) {
					s = sampleIterator.next();
					
					if (!map.containsKey(Long.parseLong(dateFormat.format(s.getTimestamp())))) {
						item = new Bundle();
						item.putLong("timestamp", s.getTimestamp().getTime());
						item.putLong("uptimeDuration", 0);
						if (s.getAccepted().equals(Boolean.TRUE)) {
							item.putInt("acceptedSamples", 1);
							item.putInt("declinedSamples", 0);
						}
						else {
							item.putInt("acceptedSamples", 0);
							item.putInt("declinedSamples", 1);
						}
						item.putInt("countSamples", 1);
						map.put(Long.parseLong(dateFormat.format(s.getTimestamp())), item);
					}
					else {
						item = map.get(Long.parseLong(dateFormat.format(s.getTimestamp())));
						if (s.getAccepted().equals(Boolean.TRUE)) {
							item.putInt("acceptedSamples", item.getInt("acceptedSamples") + 1);
						}
						else {
							item.putInt("declinedSamples", item.getInt("declinedSamples") + 1);
						}
						item.putInt("countSamples", item.getInt("countSamples") + 1);
					}
				}
				
				// handle uptimes
				
				Iterator<Uptime> uptimeIterator = uptimes.iterator();
				Uptime up = null;
				item = null;
				
				while (uptimeIterator.hasNext()) {
					up = uptimeIterator.next();
					
					// uptime extends over midnight, split up times for 2 different days
					if (!dateFormat.format(up.getStart()).equals(dateFormat.format(up.getEnd()))) {
						// get timestamp of midnight
						GregorianCalendar midnight = new GregorianCalendar();
						midnight.setTime(up.getEnd());
						midnight = new GregorianCalendar(midnight.get(Calendar.YEAR),
								midnight.get(Calendar.MONTH), midnight.get(Calendar.DAY_OF_MONTH));
						
						if (map.containsKey(Long.parseLong(dateFormat.format(up.getStart())))) {
							item = map.get(Long.parseLong(dateFormat.format(up.getStart())));
							long newDuration = Math.abs(midnight.getTimeInMillis() - up.getStart().getTime());
							item.putLong("uptimeDuration", item.getLong("uptimeDuration") + newDuration);
						}
						else {
							item = new Bundle();
							item.putLong("timestamp", up.getStart().getTime());
							item.putInt("acceptedSamples", 0);
							item.putInt("declinedSamples", 0);
							item.putInt("countSamples", 0);
							item.putLong("uptimeDuration", Math.abs(midnight.getTimeInMillis() - up.getStart().getTime()));
							map.put(Long.parseLong(dateFormat.format(up.getStart())), item);
						}
						
						if (map.containsKey(Long.parseLong(dateFormat.format(up.getEnd())))) {
							item = map.get(Long.parseLong(dateFormat.format(up.getEnd())));
							long newDuration = Math.abs(up.getEnd().getTime() - midnight.getTimeInMillis());
							item.putLong("uptimeDuration", item.getLong("uptimeDuration") + newDuration);
						}
						else {
							item = new Bundle();
							item.putLong("timestamp", up.getEnd().getTime());
							item.putInt("acceptedSamples", 0);
							item.putInt("declinedSamples", 0);
							item.putInt("countSamples", 0);
							item.putLong("uptimeDuration", Math.abs(up.getEnd().getTime() - midnight.getTimeInMillis()));
							map.put(Long.parseLong(dateFormat.format(up.getEnd())), item);
						}
					}
					else {
						if (map.containsKey(Long.parseLong(dateFormat.format(up.getStart())))) {
							item = map.get(Long.parseLong(dateFormat.format(up.getStart())));
							long newDuration = Math.abs(up.getEnd().getTime() - up.getStart().getTime());
							item.putLong("uptimeDuration", item.getLong("uptimeDuration") + newDuration);
						}
						else {
							item = new Bundle();
							item.putLong("timestamp", up.getStart().getTime());
							item.putInt("acceptedSamples", 0);
							item.putInt("declinedSamples", 0);
							item.putInt("countSamples", 0);
							item.putLong("uptimeDuration", Math.abs(up.getEnd().getTime() - up.getStart().getTime()));
							map.put(Long.parseLong(dateFormat.format(up.getStart())), item);
						}
					}
				}
			}
			
			return new ArrayList<Bundle>(map.values());
		}
		
		return null;
	}
	
	public static long getUptimeDurationToday(Context ctx, TimerProfile profile) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		
		return getUptimeDuration(ctx, profile, today);
	}
	
	public static long getUptimeDuration(Context ctx, TimerProfile profile, Calendar day) {
		UptimeTable upTbl = new UptimeTable(ctx.getApplicationContext(), profile);
		List<Uptime> times = upTbl.getUptimesOfDay(day);
		Uptime recent = upTbl.getMostRecentUptime();
		
		if (times != null && times.size() > 0) {
			long duration = 0;
			
			// first item could start the day before and last item could end the day after
			// so sum up 'inner' uptimes first
			if (times.size() > 2) {
				for (int i = 1; i < times.size() - 1; i++) {
					Uptime u = times.get(i);
					if (u.getEnd() != null) {
						duration += Math.abs(u.getEnd().getTime() - u.getStart().getTime());
					}
					else {
						// uptimes with missing end value are counted as minimum uptime duration
						// unless current running uptime, then the current time is used as end time
						if (u.getId() != recent.getId()) {
							duration += profile.getMinUptimeDuration();
						}
						else {
							duration += Math.abs(new Date().getTime() - u.getStart().getTime());
						}
					}
				}
			}
			
			// get start and end timestamp of day
			long startOfDay = day.getTimeInMillis();
			day.roll(Calendar.DAY_OF_MONTH, true);
			long endOfDay = day.getTimeInMillis();
			
			Uptime first = times.get(0);
			// first uptime starts the day before
			// use only time starting from midnight
			if (first.getStart().getTime() < startOfDay) {
				if (first.getEnd() != null) {
					duration += Math.abs(first.getEnd().getTime() - startOfDay);
				}
				else {
					if (first.getId() != recent.getId()) {
						duration += profile.getMinUptimeDuration();
					}
					else {
						duration += Math.abs(new Date().getTime() - first.getStart().getTime());
					}
				}
			}
			else {
				if (first.getEnd() != null) {
					duration += Math.abs(first.getEnd().getTime() - first.getEnd().getTime());
				}
				else {
					if (first.getId() != recent.getId()) {
						duration += profile.getMinUptimeDuration();
					}
					else {
						duration += Math.abs(new Date().getTime() - first.getStart().getTime());
					}
				}
			}
			
			if (times.size() > 1) {
				Uptime last = times.get(times.size() - 1);
				
				if (last.getEnd() != null) {
					// first uptime ends the day after
					// use only time until midnight
					if (last.getEnd().getTime() > endOfDay) {
						duration += Math.abs(endOfDay - last.getStart().getTime());
					}
					else {
						duration += Math.abs(last.getEnd().getTime() - last.getStart().getTime());
					}
				}
				else {
					if (last.getId() != recent.getId()) {
						duration += profile.getMinUptimeDuration();
					}
					else {
						duration += Math.abs(new Date().getTime() - last.getStart().getTime());
					}
				}
			}
			
			return duration;
		}
		
		return 0L;
	}
}
