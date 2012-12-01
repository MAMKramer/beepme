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

package com.glanznig.beepme.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SampleTable extends StorageHandler {
	
	private static final String TAG = "SampleTable";
	
	private static final String TBL_NAME = "sample";
	private static final String TBL_CREATE =
			"CREATE TABLE " + TBL_NAME + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"timestamp INTEGER NOT NULL UNIQUE, " +
			"title TEXT, " +
			"description TEXT, " +
			"accepted INTEGER NOT NULL, " +
			"photoUri TEXT" +
			")";
	
	public SampleTable(Context ctx) {
		super(ctx);
	}
	
	public static String getTableName() {
		return TBL_NAME;
	}
	
	public static void createTable(SQLiteDatabase db) {
		db.execSQL(TBL_CREATE);
	}
	
	public static void dropTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
	}
	
	public static void truncateTable(SQLiteDatabase db) {
		dropTable(db);
		createTable(db);
	}
	
	public Sample getSample(long id) {
		SQLiteDatabase db = getDb();
		Sample s = null;
		
		Cursor cursor = db.query(TBL_NAME, new String[] {"_id", "timestamp", "title", "description", "accepted", "photoUri"},
				"_id=?", new String[] { String.valueOf(id) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			s = new Sample(cursor.getLong(0));
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
		}
		cursor.close();
		db.close();
		
		return s;
	}
	
	public Sample getSampleWithTags(long id) {
		Sample s = getSample(id);
		if (s != null) {
			List<Tag> tagList = getTagsOfSample(s.getId());
			if (tagList != null) {
				Iterator<Tag> i = tagList.iterator();
				while (i.hasNext()) {
					s.addTag(i.next());
				}
			}
		}
		return s;
	}
	
	public List<Tag> getTagsOfSample(long id) {
		if (id != 0L) {
			ArrayList<Tag> tagList = new ArrayList<Tag>();
			SQLiteDatabase db = getDb();
			Cursor cursor = db.rawQuery("SELECT t._id, t.name FROM " + TagTable.getTableName() + " t " +
					"INNER JOIN " + SampleTagTable.getTableName() + " st ON st.tag_id = t._id WHERE st.sample_id = ?",
					new String[] { String.valueOf(id) });
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				Tag t = null;
				do {
					t = new Tag(cursor.getLong(0));
					t.setName(cursor.getString(1));
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
	
	public List<Sample> getSamples() {
		SQLiteDatabase db = getDb();
		List<Sample> sampleList = new ArrayList<Sample>();
		
		Cursor cursor = db.query(getTableName(), new String[] {"_id", "timestamp", "title", "description", "accepted", "photoUri"},
				"accepted = 1", null, null, null, "timestamp DESC");
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			do {
				Sample s = new Sample(cursor.getLong(0));
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
				sampleList.add(s);
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		return sampleList;
	}
	
	public Sample addSample(Sample s) {
		Sample sCreated = null;
		
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
		 
		    if (success) {
		    	long sampleId = db.insert(getTableName(), null, values);
		    	sCreated = new Sample(sampleId);
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
		    }
		    db.close();
		    
		    if (s.getTags().size() > 0) {
		    	Iterator<Tag> i = s.getTags().iterator();
		    	TagTable tt = new TagTable(this.getContext());
		    	while (i.hasNext()) {
		    		Tag t = i.next();
		    		sCreated.addTag(tt.addTag(t.getName(), s.getId()));
		    	}
		    }
		}
		
		return sCreated;
	}
	
	public boolean editSample(Sample s) {
		SQLiteDatabase db = getDb();
		TagTable tt = new TagTable(this.getContext());
		 
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
	    
	    int numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(s.getId()) });
	    db.close();
	    
	    List<Tag> dbTagList = getTagsOfSample(s.getId());
	    List<Tag> sTagList = s.getTags();
	    
	    if (sTagList.size() == 0 && dbTagList != null) {
	    	//delete all
	    	Iterator<Tag> i = dbTagList.iterator();
	    	while (i.hasNext()) {
	    		tt.removeTag(i.next().getName(), s.getId());
	    	}
	    }
	    else if (sTagList.size() > 0 && dbTagList == null) {
	    	//add all
	    	Iterator<Tag> i = sTagList.iterator();
	    	while (i.hasNext()) {
	    		tt.addTag(i.next().getName(), s.getId());
	    	}
	    }
	    else if (sTagList.size() > 0 && dbTagList != null) {
	    	//sync, if changes
	    	if (!sTagList.equals(dbTagList)) {
	    		HashSet<String> sTagSet = new HashSet<String>();
	    		Iterator<Tag> i = sTagList.iterator();
	    		while (i.hasNext()) {
	    			Tag t = i.next();
	    			sTagSet.add(t.getName());
	    		}
	    		
	    		HashSet<String> dbTagSet = new HashSet<String>();
	    		i = dbTagList.iterator();
	    		while (i.hasNext()) {
	    			Tag t = i.next();
	    			dbTagSet.add(t.getName());
	    		}
	    		
	    		//sample side
	    		i = sTagList.iterator();
	    		while (i.hasNext()) {
	    			Tag t = i.next();
	    			if (!dbTagSet.contains(t.getName())) {
	    				tt.addTag(t.getName(), s.getId());
	    			}
	    		}
	    		
	    		//db side
	    		i = dbTagList.iterator();
	    		while (i.hasNext()) {
	    			Tag t = i.next();
	    			if (!sTagSet.contains(t.getName())) {
	    				tt.removeTag(t.getName(), s.getId());
	    			}
	    		}
	    	}
	    }
		
		return numRows == 1 ? true : false;
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
