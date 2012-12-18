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
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TagTable extends StorageHandler {
	
	private static final String TAG = "TagTable";
	
	private static final String TBL_NAME = "tag";
	private static final String TBL_CREATE =
			"CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"name TEXT NOT NULL UNIQUE" +
			")";
	
	public TagTable(Context ctx) {
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
	
	public Tag addTag(String tagName, long sampleId) {
		
		if (tagName != null && sampleId != 0L) {
			SQLiteDatabase db = getDb();
			ContentValues values = null;
			long tagId = 0L;
			boolean success = true;
			db.beginTransaction();
			
			Cursor cursor = db.query(getTableName(), new String[] { "_id", "name" }, "name=?", new String[] { tagName.toLowerCase() }, null, null, null);
			
			if (cursor != null && cursor.getCount() == 0) {
			    values = new ContentValues();
			    values.put("name", tagName.toLowerCase());
			    tagId = db.insert(getTableName(), null, values);
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
			    		db.insertOrThrow(SampleTagTable.getTableName(), null, values);
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
			SQLiteDatabase db = getDb();
			db.beginTransaction();
			
			//delete tag - sample relationship
			int rows = db.delete(SampleTagTable.getTableName(),
					"tag_id = (SELECT t._id FROM " + getTableName() + " t WHERE t.name = ?) AND sample_id = ?",
					new String[] { tagName.toLowerCase(), String.valueOf(sampleId) });
			
			if (rows > 0) {
				Cursor cursor = db.rawQuery("SELECT sample_id FROM " + SampleTagTable.getTableName() +
						" st INNER JOIN " + getTableName() + " t ON st.tag_id = t._id WHERE t.name = ?",
						new String[] { tagName.toLowerCase() });
				
				//if there are no other samples attached to this tag -> delete tag
				if (cursor != null) {
					if (cursor.getCount() == 0) {
						rows = db.delete(getTableName(), "name = ?", new String[] { tagName.toLowerCase() });
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
		SQLiteDatabase db = getDb();
		Cursor cursor = db.query(getTableName(), new String[] { "_id", "name" }, "name like '" + search + "%'", null, null, null, "name");
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Tag t = null;
			do {
				t = new Tag(cursor.getLong(0));
				t.setName(cursor.getString(1));
				list.add(t);
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		return list;
	}
	
	public List<Tag> getTags() {
		ArrayList<Tag> list = new ArrayList<Tag>();
		SQLiteDatabase db = getDb();
		Cursor cursor = db.query(getTableName(), new String[] { "_id", "name" }, null, null, null, null, "name");
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Tag t = null;
			do {
				t = new Tag(cursor.getLong(0));
				t.setName(cursor.getString(1));
				list.add(t);
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		return list;
	}

}
