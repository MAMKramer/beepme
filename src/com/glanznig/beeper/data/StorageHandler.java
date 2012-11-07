package com.glanznig.beeper.data;

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
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StorageHandler extends SQLiteOpenHelper {
	
	private static final String TAG = "beeper";
	public static final String DB_NAME = "beeper";
	private static final int DB_VERSION = 8;
	
	private static final String SAMPLE_TBL_NAME = "sample";
	private static final String SAMPLE_TBL_CREATE =
			"CREATE TABLE " + SAMPLE_TBL_NAME + " (" +
			"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"timestamp INTEGER NOT NULL UNIQUE, " +
			"title TEXT, " +
			"description TEXT, " +
			"accepted INTEGER NOT NULL, " +
			"photoUri TEXT" +
			")";
	
	private static final String TAG_TBL_NAME = "tag";
	private static final String TAG_TBL_CREATE =
			"CREATE TABLE " + TAG_TBL_NAME + " (" +
			"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"name TEXT NOT NULL UNIQUE" +
			")";
	
	private static final String SAMPLE_TAG_TBL_NAME = "sample_tag";
	private static final String SAMPLE_TAG_TBL_CREATE =
			"CREATE TABLE " + SAMPLE_TAG_TBL_NAME + " (" +
			"sample_id INTEGER NOT NULL, " +
			"tag_id INTEGER NOT NULL, " +
			"PRIMARY KEY(sample_id, tag_id), " +
			"FOREIGN KEY(sample_id) REFERENCES sample(id), " +
			"FOREIGN KEY(tag_id) REFERENCES tag(id)" +
			")";
	
	private static final String UPTIME_TBL_NAME = "uptime";
	private static final String UPTIME_TBL_CREATE = 
			"CREATE TABLE " + UPTIME_TBL_NAME + " (" +
			"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"start INTEGER NOT NULL UNIQUE, " +
			"end INTEGER UNIQUE" +
			")";
	
	public StorageHandler(Context ctx) {
		super(ctx, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SAMPLE_TBL_CREATE);
		db.execSQL(TAG_TBL_CREATE);
		db.execSQL(SAMPLE_TAG_TBL_CREATE);
		db.execSQL(UPTIME_TBL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTables();
		onCreate(db);
	}
	
	public void dropTables() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + SAMPLE_TAG_TBL_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SAMPLE_TBL_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + TAG_TBL_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + UPTIME_TBL_NAME);
	}
	
	public void truncateTables() {
		dropTables();
		onCreate(this.getWritableDatabase());
	}
	
	public Sample getSample(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		Sample s = null;
		
		Cursor cursor = db.query(SAMPLE_TBL_NAME, new String[] {"id", "timestamp", "title", "description", "accepted", "photoUri"},
				"id=?", new String[] { String.valueOf(id) }, null, null, null);
		
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
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT t.id, t.name FROM " + TAG_TBL_NAME + " t " +
					"INNER JOIN " + SAMPLE_TAG_TBL_NAME + " st ON st.tag_id = t.id WHERE st.sample_id = ?", new String[] { String.valueOf(id) });
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				Tag t = null;
				do {
					t = new Tag(cursor.getLong(0));
					t.setName(cursor.getString(1));
					tagList.add(t);
				}
				while (cursor.moveToNext());
				
				return tagList;
			}
		}
		
		return null;
	}
	
	public List<Sample> getSamples() {
		SQLiteDatabase db = this.getWritableDatabase();
		List<Sample> sampleList = new ArrayList<Sample>();
		
		Cursor cursor = db.query(SAMPLE_TBL_NAME, new String[] {"id", "timestamp", "title", "description", "accepted", "photoUri"},
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
			SQLiteDatabase db = this.getWritableDatabase();
			 
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
		    	long sampleId = db.insert(SAMPLE_TBL_NAME, null, values);
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
		    	while (i.hasNext()) {
		    		Tag t = i.next();
		    		sCreated.addTag(addTag(t.getName(), s.getId()));
		    	}
		    }
		}
		
		return sCreated;
	}
	
	public boolean editSample(Sample s) {
		SQLiteDatabase db = this.getWritableDatabase();
		 
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
	    
	    int numRows = db.update(SAMPLE_TBL_NAME, values, "id=?", new String[] { String.valueOf(s.getId()) });
	    db.close();
	    
	    List<Tag> dbTagList = getTagsOfSample(s.getId());
	    List<Tag> sTagList = s.getTags();
	    
	    if (sTagList.size() == 0 && dbTagList != null) {
	    	//delete all
	    	Iterator<Tag> i = dbTagList.iterator();
	    	while (i.hasNext()) {
	    		removeTag(i.next().getName(), s.getId());
	    	}
	    }
	    else if (sTagList.size() > 0 && dbTagList == null) {
	    	//add all
	    	Iterator<Tag> i = sTagList.iterator();
	    	while (i.hasNext()) {
	    		addTag(i.next().getName(), s.getId());
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
	    				addTag(t.getName(), s.getId());
	    			}
	    		}
	    		
	    		//db side
	    		i = dbTagList.iterator();
	    		while (i.hasNext()) {
	    			Tag t = i.next();
	    			if (!sTagSet.contains(t.getName())) {
	    				removeTag(t.getName(), s.getId());
	    			}
	    		}
	    	}
	    }
		
		return numRows == 1 ? true : false;
	}
	
	public Tag addTag(String tagName, long sampleId) {
		
		if (tagName != null && sampleId != 0L) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = null;
			long tagId = 0L;
			boolean success = true;
			db.beginTransaction();
			
			Cursor cursor = db.query(TAG_TBL_NAME, new String[] { "id", "name" }, "name=?", new String[] { tagName.toLowerCase() }, null, null, null);
			
			if (cursor != null && cursor.getCount() == 0) {
			    values = new ContentValues();
			    values.put("name", tagName.toLowerCase());
			    tagId = db.insert(TAG_TBL_NAME, null, values);
			}
			else {
				cursor.moveToFirst();
				tagId = cursor.getLong(0);
			}
			
			if (cursor != null) {
				cursor.close();
			}
		 
		    if (success) {
		    	if (tagId != 0L) {
			    	values = new ContentValues();
			    	values.put("sample_id", sampleId);
			    	values.put("tag_id", tagId);
			    	try {
			    		db.insertOrThrow(SAMPLE_TAG_TBL_NAME, null, values);
			    	}
			    	catch (SQLiteConstraintException sce) {
			    		Log.e(TAG, "error insert tag relation", sce);
			    		success = false;
			    	}
		    	}
		    	else {
		    		success = false;
		    	}
		    }
		    
		    if (success) {
		    	db.setTransactionSuccessful();
		    }
		    
		    db.endTransaction();
		    db.close();
		    
		    if (success) {
		    	Tag t = new Tag(tagId);
		    	t.setName(tagName.toLowerCase());
		    	
		    	return t;
		    }
		}
	    
	    return null;
	}
	
	public boolean removeTag(String tagName, long sampleId) {
		boolean success = true;
		
		if (tagName != null && sampleId != 0L) {
			SQLiteDatabase db = this.getWritableDatabase();
			db.beginTransaction();
			
			//delete tag - sample relationship
			int rows = db.delete(SAMPLE_TAG_TBL_NAME,
					"tag_id = (SELECT t.id FROM " + TAG_TBL_NAME + " t WHERE t.name = ?) AND sample_id = ?",
					new String[] { tagName.toLowerCase(), String.valueOf(sampleId) });
			
			if (rows > 0) {
				Cursor cursor = db.rawQuery("SELECT sample_id FROM " + SAMPLE_TAG_TBL_NAME +
						" st INNER JOIN " + TAG_TBL_NAME + " t ON st.tag_id = t.id WHERE t.name = ?",
						new String[] { tagName.toLowerCase() });
				
				//if there are no other samples attached to this tag -> delete tag
				if (cursor != null) {
					if (cursor.getCount() == 0) {
						rows = db.delete(TAG_TBL_NAME, "name = ?", new String[] { tagName.toLowerCase() });
						if (rows == 0) {
							success = false;
						}
					}
					cursor.close();
				}
				else {
					success = false;
				}
			}
			else {
				success = false;
			}
				
			if (success) {
				db.setTransactionSuccessful();
			}
			
			db.endTransaction();
			db.close();
		}
		else {
			success = false;
		}
		
		return success;
	}
	
	public List<Tag> getTags(String search) {
		ArrayList<Tag> list = new ArrayList<Tag>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TAG_TBL_NAME, new String[] { "id", "name" }, "name like '" + search + "%'", null, null, null, "name");
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Tag t = null;
			do {
				t = new Tag(cursor.getLong(0));
				t.setName(cursor.getString(1));
				list.add(t);
			}
			while (cursor.moveToNext());
		}
		
		return list;
	}
	
	public List<Tag> getTags() {
		ArrayList<Tag> list = new ArrayList<Tag>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TAG_TBL_NAME, new String[] { "id", "name" }, null, null, null, null, "name");
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Tag t = null;
			do {
				t = new Tag(cursor.getLong(0));
				t.setName(cursor.getString(1));
				list.add(t);
			}
			while (cursor.moveToNext());
		}
		
		return list;
	}
	
	public long startUptime(Date start) {
		if (start != null) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("start", start.getTime());
			
			long id = db.insert(UPTIME_TBL_NAME, null, values);
			db.close();
			
			return id;
		}
	
		return 0L;
	}
	
	public boolean endUptime(long uptimeId, Date end) {
		int numRows = 0;
		
		if (uptimeId != 0L && end != null) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("end", end.getTime());
			numRows = db.update(UPTIME_TBL_NAME, values, "id=?", new String[] { String.valueOf(uptimeId) });
		    db.close();
		}
		
		return numRows == 1 ? true : false;
	}
	
	public long getUptimeDurToday() {
		return (long)getAvgUpDurToday(false);
	}
	
	public double getAvgUptimeDurToday() {
		return getAvgUpDurToday(true);
	}
	
	private double getAvgUpDurToday(boolean avg) {
		long duration = 0L;
		int count = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		long startOfDay = today.getTimeInMillis();
		today.roll(Calendar.DAY_OF_MONTH, true);
		long endOfDay = today.getTimeInMillis();
		
		Cursor cursor = db.query(UPTIME_TBL_NAME, new String[] { "start", "end" },
				"start between ? and ?", new String[] { String.valueOf(startOfDay), String.valueOf(endOfDay) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				if (!cursor.isNull(0) && !cursor.isNull(1)) {
					count += 1;
					duration += cursor.getLong(1) - cursor.getLong(0);
				}
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		//transform from millseconds to seconds
		duration = duration / 1000;
		
		if (avg) {
			return duration/count;
		}
		else {
			return duration;
		}
	}
	
	public float getRatioAcceptedToday() {
		int count = 0;
		int accepted = 0;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		long startOfDay = today.getTimeInMillis();
		today.roll(Calendar.DAY_OF_MONTH, true);
		long endOfDay = today.getTimeInMillis();
		
		Cursor cursor = db.query(SAMPLE_TBL_NAME, new String[] {"accepted"}, "timestamp between ? and ?",
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
		
		return accepted/count;
	}
	
	public int getNumAcceptedToday() {
		int count = 0;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		long startOfDay = today.getTimeInMillis();
		today.roll(Calendar.DAY_OF_MONTH, true);
		long endOfDay = today.getTimeInMillis();
		
		Cursor cursor = db.query(SAMPLE_TBL_NAME, new String[] {"id"}, "timestamp between ? and ? and accepted = 1",
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
		
		SQLiteDatabase db = this.getReadableDatabase();
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		long startOfDay = today.getTimeInMillis();
		today.roll(Calendar.DAY_OF_MONTH, true);
		long endOfDay = today.getTimeInMillis();
		
		Cursor cursor = db.query(SAMPLE_TBL_NAME, new String[] {"accepted"}, "timestamp between ? and ?",
				new String[] { String.valueOf(startOfDay), String.valueOf(endOfDay) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			count = cursor.getCount();
			cursor.close();
		}
		db.close();
		
		return count;
	}

}
