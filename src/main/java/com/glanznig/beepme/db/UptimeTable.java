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

package com.glanznig.beepme.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.glanznig.beepme.data.Uptime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UptimeTable extends StorageHandler {
	
	private static final String TAG = "UptimeTable";
	
	private static final String TBL_NAME = "uptime";
	private static final String TBL_CREATE = 
			"CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"start INTEGER NOT NULL UNIQUE, " +
			"end INTEGER UNIQUE, " +
			"project_id INTEGER NOT NULL, " +
			"FOREIGN KEY (project_id) REFERENCES "  + ProjectTable.getTableName() + " (_id)" +
			")";

	private Timer timerProfile;

    public UptimeTable(Context ctx) {
        super(ctx);
    }
	
	public UptimeTable(Context ctx, Timer timerProfile) {
		super(ctx);
		this.timerProfile = timerProfile;
	}

    /**
     * Returns the table name.
     * @return table name
     */
	public static String getTableName() {
		return TBL_NAME;
	}

    /**
     * Creates the table.
     * @param db database object.
     */
	public static void createTable(SQLiteDatabase db) {
		db.execSQL(TBL_CREATE);
	}

    /**
     * Drops the table.
     * @param db database object.
     */
	public static void dropTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
	}

	public long startUptime(Date start) {
		if (start != null) {
			SQLiteDatabase db = getDb();
			ContentValues values = new ContentValues();
			values.put("start", start.getTime());
			values.put("timerProfileId", timerProfile.getId());
			
			long id = db.insert(getTableName(), null, values);
			db.close();
			
			return id;
		}
	
		return 0L;
	}
	
	public boolean endUptime(long uptimeId, Date end) {
		int numRows = 0;
		long startTime = 0L;
		
		if (uptimeId != 0L && end != null) {
			SQLiteDatabase db = getDb();
			
			Cursor cursor = db.query(getTableName(), new String[] { "start" },
					"_id = ?", new String[] { String.valueOf(uptimeId) }, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				startTime = cursor.getLong(0);
				cursor.close();
			}
			
			//remove very short uptimes from statistics
			int minUptimeDuration = 60;
			if (timerProfile != null) {
				minUptimeDuration = timerProfile.getMinUptimeDuration();
			}
			if (startTime != 0L && end.getTime() - startTime > minUptimeDuration * 1000) {
				ContentValues values = new ContentValues();
				values.put("end", end.getTime());
				numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(uptimeId) });
			}
			else if (startTime != 0L) {
				numRows = db.delete(getTableName(), "_id = ?", new String[] { String.valueOf(uptimeId) });
				if (numRows == 1) {
					db.delete(BeepTable.getTableName(), "uptime_id = ?", new String[] { String.valueOf(uptimeId) });
				}
			}
		    db.close();
		}
		
		return numRows == 1;
	}
	
	public Uptime getMostRecentUptime() {
		SQLiteDatabase db = getDb();
		
		Cursor cursor = db.query(getTableName(), new String[] { "_id", "start", "end", "timerProfileId" },
				null, null, null, null, "start DESC", null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Uptime u = new Uptime(cursor.getLong(0));
			u.setStart(new Date(cursor.getLong(1)));
			if (!cursor.isNull(2)) {
				u.setEnd(new Date(cursor.getLong(2)));
			}
			u.setProjectUid(cursor.getInt(3));
			cursor.close();
			db.close();
			return u;
		}
		db.close();
		
		return null;
	}
	
	public List<Uptime> getUptimes() {
		ArrayList<Uptime> list = new ArrayList<Uptime>();
		SQLiteDatabase db = getDb();
		
		Cursor cursor = db.query(getTableName(), new String[] { "_id", "start", "end", "timerProfileId" },
				null, null, null, null, "start DESC", null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				Uptime u = new Uptime(cursor.getLong(0));
				u.setStart(new Date(cursor.getLong(1)));
				if (!cursor.isNull(2)) {
					u.setEnd(new Date(cursor.getLong(2)));
				}
				u.setProjectUid(cursor.getInt(3));
				
				list.add(u);
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		return list;
	}
	
	public List<Uptime> getUptimesOfDay(Calendar day) {
		// all uptimes that BEGIN or END on that day
		if (day.isSet(Calendar.YEAR) && day.isSet(Calendar.MONTH) && day.isSet(Calendar.DAY_OF_MONTH)) {
			ArrayList<Uptime> list = new ArrayList<Uptime>();
			
			// get start and end timestamp of day
			long startOfDay = day.getTimeInMillis();
			day.roll(Calendar.DAY_OF_MONTH, true);
			long endOfDay = day.getTimeInMillis();
			day.roll(Calendar.DAY_OF_MONTH, false);
			
			SQLiteDatabase db = getDb();
			
			// distinct start values
			Cursor cursor = db.query(true, getTableName(), new String[] { "_id", "start", "end", "timerProfileId" },
					"start between ? and ? OR end between ? and ?", new String[] { String.valueOf(startOfDay),
					String.valueOf(endOfDay), String.valueOf(startOfDay),
					String.valueOf(endOfDay) }, "start", null, "start DESC", null);
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					Uptime u = new Uptime(cursor.getLong(0));
					u.setStart(new Date(cursor.getLong(1)));
					if (!cursor.isNull(2)) {
						u.setEnd(new Date(cursor.getLong(2)));
					}
					u.setProjectUid(cursor.getInt(3));
					
					list.add(u);
				}
				while (cursor.moveToNext());
				cursor.close();
			}
			db.close();
			
			return list;
		}
		return null;
	}
	
	public long getUptimeDurToday() {
		return (long)getAvgUpDurToday("dur");
	}
	
	public int getUptimeCountToday() {
		return (int)getAvgUpDurToday("cnt");
	}
	
	public double getAvgUptimeDurToday() {
		return getAvgUpDurToday("avg");
	}
	
	private double getAvgUpDurToday(String returnType) {
		long duration = 0L;
		int count = 0;
		SQLiteDatabase db = getDb();
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		long startOfDay = today.getTimeInMillis();
		today.roll(Calendar.DAY_OF_MONTH, true);
		long endOfDay = today.getTimeInMillis();
		today.roll(Calendar.DAY_OF_MONTH, false);
		
		Cursor cursor = db.query(getTableName(), new String[] { "start", "end" },
				"start between ? and ?", new String[] { String.valueOf(startOfDay), String.valueOf(endOfDay) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				if (!cursor.isNull(0) && !cursor.isNull(1)) {
					count += 1;
					duration += cursor.getLong(1) - cursor.getLong(0);
				}
				//provided that there were no force closes, a missing end time as last row should
				//indicate the currently running uptime interval, include it with end time "now".
				//if the currently running uptime interval's duration is larger than TimerProfile.MIN_UPTIME_DURATION
				else if (cursor.isNull(1) && cursor.isLast()) {
					long nowTime = Calendar.getInstance().getTimeInMillis();
					int minUptimeDuration = 60;
					if (timerProfile != null) {
						minUptimeDuration = timerProfile.getMinUptimeDuration();
					}
					if (nowTime - cursor.getLong(0) > minUptimeDuration * 1000) {
						count += 1;
						duration += nowTime - cursor.getLong(0);
					}
				}
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		//transform from milliseconds to seconds
		duration = duration / 1000;
		
		if (returnType.equals("avg")) {
			if (count > 0) {
				return duration/count;
			}
			else {
				return 0;
			}
		}
		else if (returnType.equals("dur")) {
			return duration;
		}
		else if (returnType.equals("cnt")) {
			return count;
		}
		else {
			return 0;
		}
	}

}
