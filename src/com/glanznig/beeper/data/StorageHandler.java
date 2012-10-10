package com.glanznig.beeper.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StorageHandler extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "beeper";
	private static final int DB_VERSION = 1;
	
	private static final String SAMPLE_TBL_NAME = "sample";
	private static final String SAMPLE_TBL_CREATE =
			"CREATE TABLE " + SAMPLE_TBL_NAME + " (" +
			"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"timestamp INTEGER NOT NULL UNIQUE, " +
			"title TEXT, " +
			"description TEXT, " +
			"accepted INTEGER NOT NULL" +
			")";
	
	public StorageHandler(Context ctx) {
		super(ctx, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SAMPLE_TBL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + SAMPLE_TBL_NAME);
		onCreate(db);
	}
	
	public Sample getSample(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		Sample s = null;
		
		Cursor cursor = db.query(SAMPLE_TBL_NAME, new String[] {"id", "timestamp", "title", "description", "accepted"},
				"id=?", new String[] { String.valueOf(id) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			s = new Sample(Long.parseLong(cursor.getString(0)));
			long timestamp = Long.parseLong(cursor.getString(1));
			s.setTimestamp(new Date(timestamp));
			s.setTitle(cursor.getString(2));
			s.setDescription(cursor.getString(3));
			if (Integer.parseInt(cursor.getString(4)) == 0) {
				s.setAccepted(false);
			}
			else {
				s.setAccepted(true);
			}
		}
		cursor.close();
		db.close();
		
		return s;
	}
	
	public List<Sample> getSamples() {
		SQLiteDatabase db = this.getWritableDatabase();
		List<Sample> sampleList = new ArrayList<Sample>();
		
		Cursor cursor = db.query(SAMPLE_TBL_NAME, new String[] {"id", "timestamp", "title", "description", "accepted"},
				"accepted = 1", null, null, null, "timestamp DESC");
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			do {
				Sample s = new Sample(Integer.parseInt(cursor.getString(0)));
				long timestamp = Long.parseLong(cursor.getString(1));
				s.setTimestamp(new Date(timestamp));
				s.setTitle(cursor.getString(2));
				s.setDescription(cursor.getString(3));
				if (Integer.parseInt(cursor.getString(4)) == 0) {
					s.setAccepted(false);
				}
				else {
					s.setAccepted(true);
				}
				sampleList.add(s);
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		
		return sampleList;
	}
	
	public boolean addSample(Sample s) {
		SQLiteDatabase db = this.getWritableDatabase();
		boolean success = true;
		 
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
	 
	    if (success) {
	    	db.insert(SAMPLE_TBL_NAME, null, values);
	    }
	    db.close();
	    
	    return success;
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
	    
	    int numRows = db.update(SAMPLE_TBL_NAME, values, "id=?", new String[] { String.valueOf(s.getId()) });
	    db.close();
		
		return numRows == 1 ? true : false;
	}

}
