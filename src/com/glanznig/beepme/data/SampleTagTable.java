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
http://beepme.glanznig.com
*/

package com.glanznig.beepme.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class SampleTagTable extends StorageHandler {
	
	private static final String TAG = "SampleTagTable";
	
	private static final String TBL_NAME = "sample_tag";
	private static final String TBL_CREATE =
			"CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
			"sample_id INTEGER NOT NULL, " +
			"tag_id INTEGER NOT NULL, " +
			"PRIMARY KEY(sample_id, tag_id), " +
			"FOREIGN KEY(sample_id) REFERENCES " + SampleTable.getTableName() + "(_id), " +
			"FOREIGN KEY(tag_id) REFERENCES " + TagTable.getTableName() + "(_id)" +
			")";
	
	public SampleTagTable(Context ctx) {
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

}
