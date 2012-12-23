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
			"name TEXT NOT NULL, " +
			"vocabulary_id INTEGER NOT NULL, " +
			"FOREIGN KEY(vocabulary_id) REFERENCES " + VocabularyTable.getTableName() + "(_id)" +
			")";
	
	public TagTable(Context ctx) {
		super(ctx);
	}
	
	public static String getTableName() {
		return TBL_NAME;
	}
	
	public static void createTable(SQLiteDatabase db) {
		db.execSQL(TBL_CREATE);
		
		createMoodEntries(db);
	}
	
	public static void dropTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
	}
	
	public static void truncateTable(SQLiteDatabase db) {
		dropTable(db);
		createTable(db);
	}
	
	private static void createMoodEntries(SQLiteDatabase db) {
		ContentValues values = null;
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "begeistert");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "verzückt");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "hingerissen");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "wütend");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "aufgebracht");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "böse");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "traurig");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "bekümmert");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "niedergeschlagen");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "schüchtern");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "scheu");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "zurückhaltend");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "angeekelt");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "empört");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "entrüstet");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "erfreut");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "froh");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "glücklich");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "anmaßend");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "überheblich");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "herablassend");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "überrascht");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "erstaunt");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "verblüfft");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "vorsichtig");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "behutsam");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "zaghaft");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "cool");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "gespannt");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "aufgeregt");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "erregt");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "geil");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "genervt");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "erschrocken");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "einsam");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "zuversichtlich");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "optimistisch");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "eigensinnig");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "entschlossen");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "entschieden");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "energisch");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "selbstsicher");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "großartig");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("vocabulary_id", 1);
		values.put("name", "brilliant");
		db.insert(TBL_NAME, null, values);
		values.put("vocabulary_id", 2);
		db.insert(TBL_NAME, null, values);
	}
	
	public Tag addTag(long vocabularyId, String tagName, long sampleId) {
		
		if (tagName != null && sampleId != 0L) {
			SQLiteDatabase db = getDb();
			ContentValues values = null;
			long tagId = 0L;
			boolean success = true;
			db.beginTransaction();
			
			Cursor cursor = db.query(getTableName(), new String[] { "_id", "name", "vocabulary_id" },
					"name=? AND vocabulary_id=?", new String[] { tagName.toLowerCase(), String.valueOf(vocabularyId) },
					null, null, null);
			
			if (cursor != null && cursor.getCount() == 0) {
			    values = new ContentValues();
			    values.put("name", tagName.toLowerCase());
			    values.put("vocabulary_id", vocabularyId);
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
		    	t.setVocabularyId(vocabularyId);
		    	
		    	return t;
		    }
		}
	    
	    return null;
	}
	
	public boolean removeTag(long vocabularyId, String tagName, long sampleId) {
		boolean success = true;
		
		if (vocabularyId != 0L && tagName != null && sampleId != 0L) {
			SQLiteDatabase db = getDb();
			db.beginTransaction();
			
			//delete tag - sample relationship
			int rows = db.delete(SampleTagTable.getTableName(),
					"tag_id = (SELECT t._id FROM " + getTableName() +
					" t WHERE t.name=? AND t.vocabulary_id=?) AND sample_id=?",
					new String[] { tagName.toLowerCase(), String.valueOf(vocabularyId), String.valueOf(sampleId) });
			
			if (rows > 0) {
				Cursor cursor = db.rawQuery("SELECT sample_id FROM " + SampleTagTable.getTableName() +
						" st INNER JOIN " + getTableName() + " t ON st.tag_id = t._id WHERE t.name=? AND t.vocabulary_id=?",
						new String[] { tagName.toLowerCase(), String.valueOf(vocabularyId) });
				
				//if there are no other samples attached to this tag -> delete tag
				if (cursor != null) {
					if (cursor.getCount() == 0) {
						rows = db.delete(getTableName(), "name=? AND vocabulary_id=?",
								new String[] { tagName.toLowerCase(), String.valueOf(vocabularyId) });
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
	
	public List<Tag> getTags(long vocabularyId, String search) {
		ArrayList<Tag> list = new ArrayList<Tag>();
		SQLiteDatabase db = getDb();
		Cursor cursor = db.query(getTableName(), new String[] { "_id", "name" },
				"name like '" + search + "%' AND vocabulary_id=?", new String[] { String.valueOf(vocabularyId) },
				null, null, "name");
		
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
	
	public List<Tag> getTags(long vocabularyId) {
		ArrayList<Tag> list = new ArrayList<Tag>();
		SQLiteDatabase db = getDb();
		Cursor cursor = db.query(getTableName(), new String[] { "_id", "name" }, "vocabulary_id=?",
				new String[] { String.valueOf(vocabularyId) }, null, null, "name");
		
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
