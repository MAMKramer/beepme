package com.glanznig.beeper.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	
	public Sample getSample(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		Sample s = null;
		
		Cursor cursor = db.query(SAMPLE_TBL_NAME, new String[] {"id", "timestamp", "title", "description", "accepted"},
				"id=?", new String[] { String.valueOf(id) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			s = new Sample(Integer.parseInt(cursor.getString(0)));
			int timestamp = Integer.parseInt(cursor.getString(1));
			s.setTimestamp(new Date((long)timestamp * 1000));
			s.setTitle(cursor.getString(2));
			s.setDescription(cursor.getString(3));
			if (Integer.parseInt(cursor.getString(4)) == 0) {
				s.setAccepted(false);
			}
			else {
				s.setAccepted(true);
			}
		}
		
		return s;
	}
	
	public List<Sample> getSamples() {
		SQLiteDatabase db = this.getWritableDatabase();
		List<Sample> sampleList = new ArrayList<Sample>();
		
		Cursor cursor = db.query(SAMPLE_TBL_NAME, new String[] {"id", "timestamp", "title", "description", "accepted"},
				null, null, null, null, "timestamp DESC");
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			do {
				Sample s = new Sample(Integer.parseInt(cursor.getString(0)));
				int timestamp = Integer.parseInt(cursor.getString(1));
				s.setTimestamp(new Date((long)timestamp * 1000));
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
		
		return sampleList;
	}

}
