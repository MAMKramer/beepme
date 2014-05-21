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

package com.glanznig.beepme.data.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.Uptime;
import com.glanznig.beepme.data.db.MomentTable;
import com.glanznig.beepme.data.db.UptimeTable;

import android.content.Context;
import android.os.Bundle;

/**
 * This class provides statistics regarding count of (accepted or declined) moments and
 * timer uptime.
 */
public class Statistics {
	
	private static final String TAG = "Statistics";
    private static final long MIN_UPTIME_DURATION = 60L;
	
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

    /**
     * Gets the number of moments for today.
     * @param ctx context
     * @return number of moments for today
     */
	public static int getMomentCountToday(Context ctx) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		
		return getMomentCount(ctx, today);
	}

    /**
     * Gets the number of moments for a specific day.
     * @param ctx context
     * @param day calendar object for specific day
     * @return number of moments for the specified day
     */
	public static int getMomentCount(Context ctx, Calendar day) {
		MomentTable momentTable = new MomentTable(ctx.getApplicationContext());
        BeepMeApp app = (BeepMeApp)ctx.getApplicationContext();
		List<Moment> moments = momentTable.getAllMomentsOfDay(app.getCurrentProject().getUid(), day);
		
		if (moments != null && moments.size() > 0) {
			return moments.size();
		}
		return 0;
	}

    /**
     * Gets the number of declined moments for today.
     * @param ctx context
     * @return number of declined moments for today
     */
	public static int getNumMomentsDeclinedToday(Context ctx) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		
		return getNumMomentsDeclined(ctx, today);
	}

    /**
     * Gets the number of declined moments for a specific day.
     * @param ctx context
     * @param day calendar object for specific day
     * @return number of declined moments for the specified day
     */
	public static int getNumMomentsDeclined(Context ctx, Calendar day) {
		MomentTable momentTable = new MomentTable(ctx.getApplicationContext());
        BeepMeApp app = (BeepMeApp)ctx.getApplicationContext();
        List<Moment> moments = momentTable.getAllMomentsOfDay(app.getCurrentProject().getUid(), day);
		
		if (moments != null && moments.size() > 0) {
			int declined = 0;
			for (int i = 0; i < moments.size(); i++) {
				Moment moment = moments.get(i);
				if (!moment.getAccepted()) {
					declined += 1;
				}
			}
			
			return declined;
		}
		return 0;
	}

    /**
     * Gets the number of accepted moments for today.
     * @param ctx context
     * @return number of accepted moments for today
     */
	public static int getNumMomentsAcceptedToday(Context ctx) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);

		return getNumMomentsAccepted(ctx, today);
	}

    /**
     * Gets the number of accepted moments for a specific day.
     * @param ctx context
     * @param day calendar object for specific day
     * @return number of accepted moments for specified day
     */
	public static int getNumMomentsAccepted(Context ctx, Calendar day) {
		MomentTable momentTable = new MomentTable(ctx.getApplicationContext());
        BeepMeApp app = (BeepMeApp)ctx.getApplicationContext();
		List<Moment> moments = momentTable.getAllMomentsOfDay(app.getCurrentProject().getUid(), day);
		
		if (moments != null && moments.size() > 0) {
			int accepted = 0;
			for (int i = 0; i < moments.size(); i++) {
				Moment moment = moments.get(i);
				if (moment.getAccepted()) {
					accepted += 1;
				}
			}
			
			return accepted;
		}
		return 0;
	}

    /**
     * Gets combined stats (moment count, accepted, declined, uptime duration) for today.
     * @param ctx context
     * @return Bundle object containing combined stats (countMoments, acceptedMoments, declinedMoments,
     * uptimeDuration) for today
     */
	public static Bundle getStatsOfToday(Context ctx) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		
		return getStatsOfDay(ctx, today);
	}

    /**
     * Gets combined stats (moment count, accepted, declined, uptime duration) for a specific day.
     * @param ctx context
     * @param day calendar object for specific day
     * @return Bundle object containing combined stats (countMoments, acceptedMoments, declinedMoments,
     * uptimeDuration) for specified day
     */
	public static Bundle getStatsOfDay(Context ctx, Calendar day) {
		long uptimeDur = getUptimeDuration(ctx, day);
		int accepted = 0;
		int declined = 0;
		
		MomentTable momentTable = new MomentTable(ctx.getApplicationContext());
        BeepMeApp app = (BeepMeApp)ctx.getApplicationContext();
        List<Moment> moments = momentTable.getAllMomentsOfDay(app.getCurrentProject().getUid(), day);
		
		if (moments != null && moments.size() > 0) {
			for (int i = 0; i < moments.size(); i++) {
				Moment moment = moments.get(i);
				if (moment.getAccepted()) {
					accepted += 1;
				}
				else {
					declined += 1;
				}
			}
		}
		
		Bundle b = new Bundle();
		b.putLong("uptimeDuration", uptimeDur);
		b.putInt("acceptedMoments", accepted);
		b.putInt("declinedMoments", declined);
		b.putInt("countMoments", accepted+declined);
		
		return b;
	}

    /**
     * Gets combined stats (moment count, accepted, declined, uptime duration) for all active days.
     * @param ctx context
     * @return List of Bundle objects, one for each active day, containing combined stats
     * (countMoments, acceptedMoments, declinedMoments, uptimeDuration)
     */
	public static List<Bundle> getStats(Context ctx) {
		UptimeTable upTbl = new UptimeTable(ctx.getApplicationContext());
		List<Uptime> uptimes = upTbl.getUptimes();
		MomentTable momentTable = new MomentTable(ctx.getApplicationContext());
        BeepMeApp app = (BeepMeApp)ctx.getApplicationContext();
		List<Moment> moments = momentTable.getAllMoments(app.getCurrentProject().getUid());
		
		if (uptimes != null && moments != null) {
			TreeMap<Long, Bundle> map = new TreeMap<Long, Bundle>(new NegativeComparator());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Bundle item = null;
			
			if (moments.size() > 0) {
                // handle samples (accepted, declined, count)
                Iterator<Moment> momentIterator = moments.iterator();
                Moment moment = null;

                while (momentIterator.hasNext()) {
                    moment = momentIterator.next();

                    if (!map.containsKey(Long.parseLong(dateFormat.format(moment.getTimestamp())))) {
                        item = new Bundle();
                        item.putLong("timestamp", moment.getTimestamp().getTime());
                        item.putLong("uptimeDuration", 0);
                        if (moment.getAccepted()) {
                            item.putInt("acceptedMoments", 1);
                            item.putInt("declinedMoments", 0);
                        } else {
                            item.putInt("acceptedMoments", 0);
                            item.putInt("declinedMoments", 1);
                        }
                        item.putInt("countMoments", 1);
                        map.put(Long.parseLong(dateFormat.format(moment.getTimestamp())), item);
                    } else {
                        item = map.get(Long.parseLong(dateFormat.format(moment.getTimestamp())));
                        if (moment.getAccepted()) {
                            item.putInt("acceptedMoments", item.getInt("acceptedMoments") + 1);
                        } else {
                            item.putInt("declinedMoments", item.getInt("declinedMoments") + 1);
                        }
                        item.putInt("countMoments", item.getInt("countMoments") + 1);
                    }
                }
            }

            if (uptimes.size() > 0) {
                // handle uptimes
				Iterator<Uptime> uptimeIterator = uptimes.iterator();
				Uptime up = null;
				item = null;
				
				while (uptimeIterator.hasNext()) {
					up = uptimeIterator.next();
					
					if (up.getEnd() != null) {
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
								item.putInt("acceptedMoments", 0);
								item.putInt("declinedMoments", 0);
								item.putInt("countMoments", 0);
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
								item.putInt("acceptedMoments", 0);
								item.putInt("declinedMoments", 0);
								item.putInt("countMoments", 0);
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
								item.putInt("acceptedMoments", 0);
								item.putInt("declinedMoments", 0);
								item.putInt("countMoments", 0);
								item.putLong("uptimeDuration", Math.abs(up.getEnd().getTime() - up.getStart().getTime()));
								map.put(Long.parseLong(dateFormat.format(up.getStart())), item);
							}
						}
					}
					else {
						// this means that there either was an error, in this case
						// the minimum duration should be used
						// or if it is the most recent uptime, that the beeper is still running
						Uptime recent = upTbl.getMostRecentUptime();
						
						// still running
						if (recent.getUid() == up.getUid()) {
							if (map.containsKey(Long.parseLong(dateFormat.format(up.getStart())))) {
								item = map.get(Long.parseLong(dateFormat.format(up.getStart())));
								
								// current time can only be used if it lies within requested day
								// else time until midnight of this day has to be used
								Calendar day = Calendar.getInstance();
								day.setTime(up.getStart());
								day = new GregorianCalendar(day.get(Calendar.YEAR),
										day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));
								day.roll(Calendar.DAY_OF_MONTH, true);
								long endOfDay = day.getTimeInMillis();
								day.roll(Calendar.DAY_OF_MONTH, false);
								
								Date now = new Date();
								long newDuration = 0;
								if (now.getTime() <= endOfDay) {
									newDuration = Math.abs(now.getTime() - up.getStart().getTime());
								}
								else {
									newDuration = Math.abs(endOfDay - up.getStart().getTime());
								}
								item.putLong("uptimeDuration", item.getLong("uptimeDuration") + newDuration);
								map.put(Long.parseLong(dateFormat.format(up.getStart())), item);
							}
							else {
								item = new Bundle();
								item.putLong("timestamp", up.getStart().getTime());
								item.putInt("acceptedMoments", 0);
								item.putInt("declinedMoments", 0);
								item.putInt("countMoments", 0);
								
								// current time can only be used if it lies within requested day
								// else time until midnight of this day has to be used
								Calendar day = Calendar.getInstance();
								day.setTime(up.getStart());
								day = new GregorianCalendar(day.get(Calendar.YEAR),
										day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));
								day.roll(Calendar.DAY_OF_MONTH, true);
								long endOfDay = day.getTimeInMillis();
								day.roll(Calendar.DAY_OF_MONTH, false);
								
								Date now = new Date();
								if (now.getTime() <= endOfDay) {
									item.putLong("uptimeDuration", Math.abs(now.getTime() - up.getStart().getTime()));
								}
								else {
									item.putLong("uptimeDuration", Math.abs(endOfDay - up.getStart().getTime()));
								}
								
								map.put(Long.parseLong(dateFormat.format(up.getStart())), item);
							}
						}
						// due to error
						else {
							if (map.containsKey(Long.parseLong(dateFormat.format(up.getStart())))) {
								item = map.get(Long.parseLong(dateFormat.format(up.getStart())));
								item.putLong("uptimeDuration", item.getLong("uptimeDuration") + MIN_UPTIME_DURATION);
								map.put(Long.parseLong(dateFormat.format(up.getStart())), item);
							}
							else {
								item = new Bundle();
								item.putLong("timestamp", up.getStart().getTime());
								item.putInt("acceptedMoments", 0);
								item.putInt("declinedMoments", 0);
								item.putInt("countMoments", 0);
								item.putLong("uptimeDuration", MIN_UPTIME_DURATION);
								map.put(Long.parseLong(dateFormat.format(up.getStart())), item);
							}
						}
					}
				}
			}
			
			return new ArrayList<Bundle>(map.values());
		}
		
		return null;
	}

    /**
     * Gets the uptime duration for today.
     * @param ctx context
     * @return uptime duration for today in milliseconds (>= 0)
     */
	public static long getUptimeDurationToday(Context ctx) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		
		return getUptimeDuration(ctx, today);
	}

    /**
     * Gets the uptime duration for a specified day.
     * @param ctx context
     * @param day calendar object for specific day
     * @return uptime duraion for today in milliseconds (>= 0)
     */
	public static long getUptimeDuration(Context ctx, Calendar day) {
		UptimeTable upTbl = new UptimeTable(ctx.getApplicationContext());
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
						if (u.getUid() != recent.getUid()) {
							duration += MIN_UPTIME_DURATION;
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
			day.roll(Calendar.DAY_OF_MONTH, false);
			
			Uptime first = times.get(0);
			// first uptime starts the day before
			// use only time starting from midnight
			if (first.getStart().getTime() < startOfDay) {
				if (first.getEnd() != null) {
					duration += Math.abs(first.getEnd().getTime() - startOfDay);
				}
				else {
					if (first.getUid() == recent.getUid()) {
						duration += Math.abs(new Date().getTime() - first.getStart().getTime());
					}
				}
			}
			else {
				if (first.getEnd() != null) {
					duration += Math.abs(first.getEnd().getTime() - first.getStart().getTime());
				}
				else {
					if (first.getUid() != recent.getUid()) {
						duration += MIN_UPTIME_DURATION;
					}
					else {
						// current time can only be used if it lies within requested day
						// else time until midnight of this day has to be used
						Date now = new Date();
						if (now.getTime() <= endOfDay) {
							duration += Math.abs(now.getTime() - first.getStart().getTime());
						}
						else {
							duration += Math.abs(endOfDay - first.getStart().getTime());
						}
					}
				}
			}
			
			if (times.size() > 1) {
				Uptime last = times.get(times.size() - 1);
				
				if (last.getEnd() != null) {
					// if uptime extends over midnight
					// use only time that lies within requested day
					if (last.getStart().getTime() <= startOfDay && last.getEnd().getTime() >= endOfDay) {
						duration += Math.abs(endOfDay - startOfDay);
					}
					else if (last.getStart().getTime() <= startOfDay) {
						duration += Math.abs(last.getEnd().getTime() - startOfDay);
					}
					else if (last.getEnd().getTime() >= endOfDay) {
						duration += Math.abs(endOfDay - last.getStart().getTime());
					}
					else {
						duration += Math.abs(last.getEnd().getTime() - last.getStart().getTime());
					}
				}
				else {
					if (last.getUid() != recent.getUid()) {
						duration += MIN_UPTIME_DURATION;
					}
					else {
						// current time can only be used if it lies within requested day
						// else time until midnight of this day has to be used
						Date now = new Date();
						if (now.getTime() <= endOfDay) {
							duration += Math.abs(now.getTime() - first.getStart().getTime());
						}
						else {
							duration += Math.abs(endOfDay - first.getStart().getTime());
						}
					}
				}
			}
			
			return duration;
		}
		
		return 0L;
	}
}
