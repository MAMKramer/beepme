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

package com.glanznig.beepme.data.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.VocabularyItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Represents the table MOMENT (basic information about moment with/out values).
 */
public class MomentTable extends StorageHandler {
	
	private static final String TAG = "MomentTable";
	
	private static final String TBL_NAME = "moment";
	private static final String TBL_CREATE =
			"CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"timestamp INTEGER NOT NULL UNIQUE, " +
			"accepted INTEGER NOT NULL, " +
			"uptime_id INTEGER, " +
            "project_id INTEGER NOT NULL, " +
			"FOREIGN KEY (uptime_id) REFERENCES "  + UptimeTable.getTableName() + " (_id), " +
            "FOREIGN KEY (project_id) REFERENCES "  + ProjectTable.getTableName() + " (_id)" +
			")";
	
	public MomentTable(Context ctx) {
		super(ctx);
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
	
	public Moment getSample(long id) {
		SQLiteDatabase db = getDb();
		Moment s = null;
		
		Cursor cursor = db.query(TBL_NAME, new String[] {"_id", "timestamp", "title", "description", "accepted",
				"photoUri", "uptimeId"},
				"_id=?", new String[] { String.valueOf(id) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			s = new Moment(cursor.getLong(0));
			long timestamp = cursor.getLong(1);
			s.setTimestamp(new Date(timestamp));
			if (!cursor.isNull(2)) {
				s.setTitle(cursor.getString(2));
			}
			if (!cursor.isNull(3)) {
				s.setDescription(cursor.getString(3));
			}
			if (cursor.getInt(4) == 0) {
				s.setAccepted(false);
			}
			else {
				s.setAccepted(true);
			}
			if (!cursor.isNull(5)) {
				s.setPhotoUri(cursor.getString(5));
			}
			if (!cursor.isNull(6)) {
				s.setUptimeId(cursor.getLong(6));
			}
		}
		cursor.close();
		db.close();
		
		return s;
	}
	
	public Moment getSampleWithTags(long id) {
		Moment s = getSample(id);
		if (s != null) {
			List<VocabularyItem> tagList = getTagsOfSample(s.getId());
			if (tagList != null) {
				Iterator<VocabularyItem> i = tagList.iterator();
				while (i.hasNext()) {
					s.addTag(i.next());
				}
			}
		}
		return s;
	}
	
	public List<VocabularyItem> getTagsOfSample(long id) {
		if (id != 0L) {
			ArrayList<VocabularyItem> tagList = new ArrayList<VocabularyItem>();
			SQLiteDatabase db = getDb();
			Cursor cursor = db.rawQuery("SELECT t._id, t.name, t.vocabulary_id FROM " + ValueVocabularyItemTable.getTableName() + " t " +
					"INNER JOIN " + VocabularyItemTable.getTableName() + " st ON st.tag_id = t._id WHERE st.sample_id = ?",
					new String[] { String.valueOf(id) });
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				VocabularyItem t = null;
				do {
					t = new VocabularyItem(cursor.getLong(0));
					t.setName(cursor.getString(1));
					t.setVocabularyUid(cursor.getLong(2));
					tagList.add(t);
				}
				while (cursor.moveToNext());
				cursor.close();
				db.close();
				
				return tagList;
			}
			else if (cursor != null) {
				cursor.close();
			}
			db.close();
		}
		
		return null;
	}
	
	public List<Moment> getSamples() {
		return getSamples(false);
	}
	
	public List<Moment> getSamples(boolean declined) {
		SQLiteDatabase db = getDb();
		List<Moment> sampleList = new ArrayList<Moment>();
		
		String where = null;
		if (declined == false) {
			where = "accepted = 1";
		}
		
		Cursor cursor = db.query(getTableName(), new String[] {"_id", "timestamp", "title", "description",
				"accepted", "photoUri", "uptimeId"}, where, null, null, null, "timestamp DESC");
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			do {
				Moment s = new Moment(cursor.getLong(0));
				long timestamp = cursor.getLong(1);
				s.setTimestamp(new Date(timestamp));
				if (!cursor.isNull(2)) {
					s.setTitle(cursor.getString(2));
				}
				if (!cursor.isNull(3)) {
					s.setDescription(cursor.getString(3));
				}
				if (cursor.getInt(4) == 0) {
					s.setAccepted(false);
				}
				else {
					s.setAccepted(true);
				}
				if (!cursor.isNull(5)) {
					s.setPhotoUri(cursor.getString(5));
				}
				if (!cursor.isNull(6)) {
					s.setUptimeId(cursor.getLong(6));
				}
				sampleList.add(s);
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		return sampleList;
	}
	
	public List<Long> getSampleIds() {
		SQLiteDatabase db = getDb();
		List<Long> idList = new ArrayList<Long>();
		
		Cursor cursor = db.query(getTableName(), new String[] {"_id"}, "accepted = 1", null, null, null, "timestamp DESC");
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			do {
				idList.add(cursor.getLong(0));
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		return idList;
	}
	
	public Moment addSample(Moment s) {
		Moment sCreated = null;
		
		if (s != null) {
			boolean success = true;
			SQLiteDatabase db = getDb();
			 
		    ContentValues values = new ContentValues();
		    if (s.getTimestamp() != null) {
		    	values.put("timestamp", String.valueOf(s.getTimestamp().getTime()));
		    }
		    else {
		    	success = false;
		    }
		    values.put("title", s.getTitle());
		    values.put("description", s.getDescription());
		    if (s.getAccepted()) {
		    	values.put("accepted", "1");
		    }
		    else {
		    	values.put("accepted", "0");
		    }
		    values.put("photoUri", s.getPhotoUri());
		    values.put("uptimeId", s.getUptimeId());
		 
		    if (success) {
		    	long sampleId = db.insert(getTableName(), null, values);
		    	sCreated = new Moment(sampleId);
		    	sCreated.setAccepted(s.getAccepted());
		    	if (s.getDescription() != null) {
		    		sCreated.setDescription(s.getDescription());
		    	}
		    	if (s.getPhotoUri() != null) {
		    		sCreated.setPhotoUri(s.getPhotoUri());
		    	}
		    	if (s.getTimestamp() != null) {
		    		sCreated.setTimestamp(s.getTimestamp());
		    	}
		    	if (s.getTitle() != null) {
		    		sCreated.setTitle(s.getTitle());
		    	}
		    	if (s.getUptimeId() != 0L) {
		    		sCreated.setUptimeId(s.getUptimeId());
		    	}
		    }
		    db.close();
		    
		    if (s.getTags().size() > 0) {
		    	Iterator<VocabularyItem> i = s.getTags().iterator();
		    	ValueVocabularyItemTable tt = new ValueVocabularyItemTable(this.getContext());
		    	while (i.hasNext()) {
		    		VocabularyItem t = i.next();
		    		sCreated.addTag(tt.addTag(t.getVocabularyUid(), t.getName(), s.getId()));
		    	}
		    }
		}
		
		return sCreated;
	}
	
	public boolean editSample(Moment s) {
		SQLiteDatabase db = getDb();
		ValueVocabularyItemTable tt = new ValueVocabularyItemTable(this.getContext());
		 
	    ContentValues values = new ContentValues();
	    values.put("title", s.getTitle());
	    values.put("description", s.getDescription());
	    if (s.getAccepted()) {
	    	values.put("accepted", "1");
	    }
	    else {
	    	values.put("accepted", "0");
	    }
	    values.put("photoUri", s.getPhotoUri());
	    values.put("uptimeId", s.getUptimeId());
	    
	    int numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(s.getId()) });
	    db.close();
	    
	    List<VocabularyItem> dbTagList = getTagsOfSample(s.getId());
	    List<VocabularyItem> sTagList = s.getTags();
	    
	    if (sTagList.size() == 0 && dbTagList != null) {
	    	//delete all
	    	Iterator<VocabularyItem> i = dbTagList.iterator();
	    	while (i.hasNext()) {
	    		VocabularyItem t = i.next();
	    		tt.removeTag(t.getVocabularyUid(), t.getName(), s.getId());
	    	}
	    }
	    else if (sTagList.size() > 0 && dbTagList == null) {
	    	//add all
	    	Iterator<VocabularyItem> i = sTagList.iterator();
	    	while (i.hasNext()) {
	    		VocabularyItem t = i.next();
	    		tt.addTag(t.getVocabularyUid(), t.getName(), s.getId());
	    	}
	    }
	    else if (sTagList.size() > 0 && dbTagList != null) {
	    	//sync, if changes
	    	if (!sTagList.equals(dbTagList)) {
	    		HashSet<String> sTagSet = new HashSet<String>();
	    		Iterator<VocabularyItem> i = sTagList.iterator();
	    		while (i.hasNext()) {
	    			VocabularyItem t = i.next();
	    			sTagSet.add(t.getName());
	    		}
	    		
	    		HashSet<String> dbTagSet = new HashSet<String>();
	    		i = dbTagList.iterator();
	    		while (i.hasNext()) {
	    			VocabularyItem t = i.next();
	    			dbTagSet.add(t.getName());
	    		}
	    		
	    		//sample side
	    		i = sTagList.iterator();
	    		while (i.hasNext()) {
	    			VocabularyItem t = i.next();
	    			if (!dbTagSet.contains(t.getName())) {
	    				tt.addTag(t.getVocabularyUid(), t.getName(), s.getId());
	    			}
	    		}
	    		
	    		//db side
	    		i = dbTagList.iterator();
	    		while (i.hasNext()) {
	    			VocabularyItem t = i.next();
	    			if (!sTagSet.contains(t.getName())) {
	    				tt.removeTag(t.getVocabularyUid(), t.getName(), s.getId());
	    			}
	    		}
	    	}
	    }
		
		return numRows == 1;
	}
	
	public List<Moment> getSamplesOfDay(Calendar day) {
		if (day.isSet(Calendar.YEAR) && day.isSet(Calendar.MONTH) && day.isSet(Calendar.DAY_OF_MONTH)) {
			ArrayList<Moment> list = new ArrayList<Moment>();
			SQLiteDatabase db = getDb();
			long startOfDay = day.getTimeInMillis();
			day.roll(Calendar.DAY_OF_MONTH, true);
			long endOfDay = day.getTimeInMillis();
			day.roll(Calendar.DAY_OF_MONTH, false);
			
			Cursor cursor = db.query(getTableName(), new String[] {"_id", "timestamp", "title", "description",
				"accepted", "photoUri", "uptimeId"}, "timestamp between ? and ?",
					new String[] { String.valueOf(startOfDay), String.valueOf(endOfDay) }, null, null, "timestamp DESC");
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				
				do {
					Moment s = new Moment(cursor.getLong(0));
					long timestamp = cursor.getLong(1);
					s.setTimestamp(new Date(timestamp));
					if (!cursor.isNull(2)) {
						s.setTitle(cursor.getString(2));
					}
					if (!cursor.isNull(3)) {
						s.setDescription(cursor.getString(3));
					}
					if (cursor.getInt(4) == 0) {
						s.setAccepted(false);
					}
					else {
						s.setAccepted(true);
					}
					if (!cursor.isNull(5)) {
						s.setPhotoUri(cursor.getString(5));
					}
					if (!cursor.isNull(6)) {
						s.setUptimeId(cursor.getLong(6));
					}
					list.add(s);
				}
				while (cursor.moveToNext());
				cursor.close();
				
				return list;
			}
		}
		
		return null;
	}
	
	public int getNumAcceptedToday() {
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
		
		Cursor cursor = db.query(getTableName(), new String[] {"_id"}, "timestamp between ? and ? and accepted = 1",
				new String[] { String.valueOf(startOfDay), String.valueOf(endOfDay) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			count = cursor.getCount();
			cursor.close();
		}
		db.close();
		
		return count;
	}
	
	public int getSampleCountToday() {
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
		
		Cursor cursor = db.query(getTableName(), new String[] {"accepted"}, "timestamp between ? and ?",
				new String[] { String.valueOf(startOfDay), String.valueOf(endOfDay) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			count = cursor.getCount();
			cursor.close();
		}
		db.close();
		
		return count;
	}
	
	public double getRatioAcceptedToday() {
		int count = 0;
		int accepted = 0;
		
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
		
		Cursor cursor = db.query(getTableName(), new String[] {"accepted"}, "timestamp between ? and ?",
				new String[] { String.valueOf(startOfDay), String.valueOf(endOfDay) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			do {
				count += 1;
				if (cursor.getInt(0) == 1) {
					accepted += 1;
				}
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		if (count == 0) {
			return 0;
		}
		
		return accepted/count;
	}

}
