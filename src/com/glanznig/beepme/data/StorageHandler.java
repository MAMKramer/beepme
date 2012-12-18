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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StorageHandler {
	
	private static final String TAG = "StorageHandler";
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		protected static final String DB_NAME = "beepme";
		protected static final int DB_VERSION = 12;
		
		public DatabaseHelper(Context ctx) {
			super(ctx, DB_NAME, null, DB_VERSION);
		} 

		@Override
		public void onCreate(SQLiteDatabase db) {
			SampleTable.createTable(db);
			TagTable.createTable(db);
			SampleTagTable.createTable(db);
			UptimeTable.createTable(db);
			ScheduledBeepTable.createTable(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			dropTables(db);
			onCreate(db);
		}
		
		public void dropTables(SQLiteDatabase db) {
			SampleTagTable.dropTable(db);
			SampleTable.dropTable(db);
			TagTable.dropTable(db);
			UptimeTable.dropTable(db);
			ScheduledBeepTable.dropTable(db);
		}
		
		public void truncateTables() {
			dropTables(this.getWritableDatabase());
			onCreate(this.getWritableDatabase());
		}
		
		public static String getDbName() {
			return DB_NAME;
		}
	}
	
	private static DatabaseHelper dbHelper = null;
	private Context ctx = null;
	
	public StorageHandler(Context ctx) {
		this.ctx = ctx;
		if (dbHelper == null) {
			dbHelper = new DatabaseHelper(ctx.getApplicationContext());
		}
	}
	
	public SQLiteDatabase getDb() {
		return dbHelper.getWritableDatabase();
	}
	
	public void truncateTables() {
		dbHelper.truncateTables();
	}
	
	public Context getContext() {
		return ctx;
	}
	
	public static String getDatabaseName() {
		return DatabaseHelper.getDbName();
	}
}
