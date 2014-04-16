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

import com.glanznig.beepme.BeeperApp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages access to the SQLite database and handles table creations and
 * database upgrades.
 */
public class StorageHandler {

    /**
     * singleton inner class for actual database access
     */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		protected static final int DB_VERSION = 19; //todo: move to 20
		
		public DatabaseHelper(Context ctx, String dbName) {
			super(ctx, dbName, null, DB_VERSION);
		}

        /**
         * Create all the tables.
         * @param db database object
         */
		@Override
		public void onCreate(SQLiteDatabase db) {
			MomentTable.createTable(db);
			ValueVocabularyItemTable.createTable(db);
			VocabularyItemTable.createTable(db);
			UptimeTable.createTable(db);
			BeepTable.createTable(db);
			VocabularyTable.createTable(db);
			ProjectTable.createTable(db);
		}

        /**
         * On database schema upgrade make incremental changes.
         * @param db database object
         * @param oldVersion old schema version
         * @param newVersion new schema version
         */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// legacy schema "upgrade"
			if (newVersion < 17 || oldVersion < 16) {
				dropTables(db);
				onCreate(db);
			}
            else {
                // do incremental db upgrades
                for (int vers = oldVersion; vers < newVersion; vers++) {
                    switch (vers) {
                        case 19:
                            break;
                    }
                }
            }
		}

        /**
         * Drops all tables.
         * @param db database object
         */
		public void dropTables(SQLiteDatabase db) {
			VocabularyItemTable.dropTable(db);
			MomentTable.dropTable(db);
			ValueVocabularyItemTable.dropTable(db);
			UptimeTable.dropTable(db);
			BeepTable.dropTable(db);
			VocabularyTable.dropTable(db);
			ProjectTable.dropTable(db);
		}

        /**
         * Truncates all tables (actually dropping and re-creating them).
         */
		public void truncateTables() {
			dropTables(this.getWritableDatabase());
			onCreate(this.getWritableDatabase());
		}
	}
	
	private static DatabaseHelper mDbHelperProduction;
	private static DatabaseHelper mDbHelperTestMode;
	private Context mCtx;

    private static final String TAG = "StorageHandler";

    public static final String DB_OLD_NAME = "beepme"; // can be removed in future versions
	private static final String DB_NAME_PRODUCTION = "beepme";
	private static final String DB_NAME_TESTMODE = "beepme_testmode";

    /**
     * Creates a new database object depending on the mode the app is currently in.
     * @param ctx context of the calling activity
     */
	public StorageHandler(Context ctx) {
		mCtx = ctx;
		BeeperApp app = (BeeperApp)ctx.getApplicationContext();
		
		if (app.getPreferences().isTestMode()) {
			if (mDbHelperTestMode == null) {
				mDbHelperTestMode = new DatabaseHelper(ctx.getApplicationContext(), DB_NAME_TESTMODE);
			}
		}
		else {
			if (mDbHelperProduction == null) {
				mDbHelperProduction = new DatabaseHelper(ctx.getApplicationContext(), DB_NAME_PRODUCTION);
			}
		}
	}

    /**
     * Returns a database object associated to the current app mode.
     * @return database object associated to this mode
     */
	public SQLiteDatabase getDb() {
		BeeperApp app = (BeeperApp) mCtx.getApplicationContext();
		if (app.getPreferences().isTestMode()) {
			return mDbHelperTestMode.getWritableDatabase();
		}
		return mDbHelperProduction.getWritableDatabase();
	}

    /**
     * Truncates all tables of the database of the current app mode.
     */
	public void truncateTables() {
		BeeperApp app = (BeeperApp) mCtx.getApplicationContext();
		if (app.getPreferences().isTestMode()) {
			mDbHelperTestMode.truncateTables();
		}
		else {
			mDbHelperProduction.truncateTables();
		}
	}
	
	public Context getContext() {
		return mCtx;
	}

    /**
     * Returns the database name of the active db.
     * @return the database name
     */
	public String getDatabaseName() {
		BeeperApp app = (BeeperApp) mCtx.getApplicationContext();
		if (app.getPreferences().isTestMode()) {
			return DB_NAME_TESTMODE;
		}
		return DB_NAME_PRODUCTION;
	}

    /**
     * Returns the database name of the production database.
     * @return db name of the production database
     */
	public static String getProductionDatabaseName() {
		return DB_NAME_PRODUCTION;
	}

    /**
     * Returns the database name of the test mode database.
     * @return db name of the test mode database
     */
	public static String getTestModeDatabaseName() {
		return DB_NAME_TESTMODE;
	}
}
