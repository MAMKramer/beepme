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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.data.Vocabulary;
import com.glanznig.beepme.data.VocabularyItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents the table VOCABULARY_ITEM (content of vocabularies)
 */
public class VocabularyItemTable extends StorageHandler {

    private static final String TAG = "VocabularyItemTable";

    private static final String TBL_NAME = "vocabulary_item";
    private static final String TBL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "lang TEXT NOT NULL, " +
                    "value TEXT NOT NULL, " +
                    "predefined INTEGER NOT NULL, " +
                    "vocabulary_id INTEGER NOT NULL, " +
                    "translation_of INTEGER, " +
                    "FOREIGN KEY(vocabulary_id) REFERENCES " + VocabularyTable.getTableName() + "(_id), " +
                    "FOREIGN KEY(translation_of) REFERENCES " + VocabularyItemTable.getTableName() + "(_id)" +
                    ")";

    private Context ctx;

    public VocabularyItemTable(Context ctx) {
        super(ctx);
        this.ctx = ctx.getApplicationContext();
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

    /**
     * Truncates (deletes the content of) the table.
     */
    public void truncate() {
        SQLiteDatabase db = getDb();
        dropTable(db);
        createTable(db);
    }

    /**
     * Populates content values for the set variables of the vocabulary item.
     * @param vocabularyItem the vocabulary item
     * @return populated content values
     */
    private ContentValues getContentValues(VocabularyItem vocabularyItem) {
        ContentValues values = new ContentValues();

        if (vocabularyItem.getName() != null) {
            values.put("name", vocabularyItem.getName());
        }
        if (vocabularyItem.getLanguage() != null) {
            values.put("lang", vocabularyItem.getLanguage().getLanguage());
        }
        if (vocabularyItem.getValue() != null) {
            values.put("value", vocabularyItem.getValue());
        }
        if (vocabularyItem.isPredefined()) {
            values.put("predefined", 1);
        }
        else {
            values.put("predefined", 0);
        }
        if (vocabularyItem.getVocabularyUid() != 0L) {
            values.put("vocabulary_id", vocabularyItem.getVocabularyUid());
        }
        if (vocabularyItem.getTranslationOfUid() != 0L) {
            values.put("translation_of", vocabularyItem.getTranslationOfUid());
        }

        return values;
    }

    /**
     * Populates a vocabulary item object by reading values from a cursor
     * @param cursor cursor object
     * @return populated vocabulary item object
     */
    private VocabularyItem populateObject(Cursor cursor) {
        VocabularyItem item = new VocabularyItem(cursor.getLong(0));
        if (!cursor.isNull(1)) {
            item.setName(cursor.getString(1));
        }
        item.setLanguage(new Locale(cursor.getString(2)));
        item.setValue(cursor.getString(3));
        if (cursor.getInt(4) == 1) {
            item.setPredefined(true);
        }
        else {
            item.setPredefined(false);
        }
        item.setVocabularyUid(cursor.getLong(5));
        if (!cursor.isNull(6)) {
            item.setTranslationOfUid(cursor.getLong(6));
        }

        return item;
    }

    /**
     * Adds a new vocabulary item to the database
     * @param vocabularyItem values to add to the vocabulary item table
     * @return new vocabulary item object with set values and uid, or null if an error occurred
     */
    public VocabularyItem addVocabularyItem(VocabularyItem vocabularyItem) {
        VocabularyItem newVocabularyItem = null;

        if (vocabularyItem != null) {
            SQLiteDatabase db = getDb();

            ContentValues values = getContentValues(vocabularyItem);

            Log.i(TAG, "inserted values=" + values);
            long vocabularyItemId = db.insert(getTableName(), null, values);
            db.close();

            // if no error occurred
            if (vocabularyItemId != -1) {
                newVocabularyItem = new VocabularyItem(vocabularyItemId);
                vocabularyItem.copyTo(newVocabularyItem);
            }
        }

        return newVocabularyItem;
    }

    /**
     * Updates a vocabulary item in the database
     * @param vocabularyItem values to update for this vocabulary item
     * @return true on success or false if an error occurred
     */
    public boolean updateVocabularyItem(VocabularyItem vocabularyItem) {
        int numRows = 0;
        if (vocabularyItem.getUid() != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(vocabularyItem);

            numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(vocabularyItem.getUid()) });
            db.close();
        }

        return numRows == 1;
    }

    /**
     * Get a vocabulary item based on its vocabulary id, language and value.
     * @param vocabularyUid uid of the vocabulary where the items belong to
     * @param lang language of the item
     * @param value value of the item
     * @return a vocabulary item, or null if not found
     */
    public VocabularyItem getVocabularyItem(long vocabularyUid, Locale lang, String value) {
        SQLiteDatabase db = getDb();
        VocabularyItem vocabularyItem = null;

        Cursor cursor = db.query(getTableName(), new String[] { "_id", "name", "lang", "value",
                        "predefined", "vocabulary_id", "translation_of" },
                "vocabulary_id=? AND lang=? AND value=?",
                new String[] { Long.valueOf(vocabularyUid).toString(), lang.getLanguage(), value },
                null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            vocabularyItem = populateObject(cursor);
            cursor.close();
        }
        db.close();

        return vocabularyItem;
    }

    /**
     * Gets a list of vocabulary items of the specified language and the specified vocabulary. For
     * predefined items the base version is returned if there is no translation available.
     * @param vocabularyUid uid of the vocabulary where the items belong to
     * @param locale language of the items
     * @return list of vocabulary items in the specified language and for predefined items also
     * items of the base language if no translation exists
     */
    public List<VocabularyItem> getVocabularyItems(long vocabularyUid, Locale locale) {
        return getVocabularyItems(vocabularyUid, locale, "");
    }

    /**
     * Gets a list of vocabulary items of the specified language and the specified vocabulary. For
     * predefined items the base version is returned if there is no translation available. The output
     * is filtered according to the last parameter, that is only items that start with the value of search
     * are contained in the output.
     * @param vocabularyUid uid of the vocabulary where the items belong to
     * @param locale language of the items
     * @param search filter string, items are selected if they start with this string
     * @return list of vocabulary items in the specified language and for predefined items also
     * items of the base language if no translation exists
     */
    public List<VocabularyItem> getVocabularyItems(long vocabularyUid, Locale locale, String search) {
        ArrayList<VocabularyItem> list = new ArrayList<VocabularyItem>();
        SQLiteDatabase db = getDb();
        String baseLang = ((BeepMeApp)ctx).getCurrentProject().getLanguage().getLanguage();

        // todo test query with high number of items, may need performance optimization
        String fields = "_id, name, lang, value, predefined, vocabulary_id, translation_of";
        String itemWhere = "WHERE vocabulary_id=? AND lang=? AND predefined=?";
        String query = "SELECT " + fields + " FROM" +
                "(SELECT " + fields + " FROM " + getTableName() + " " + itemWhere + " AND " +
                "_id NOT IN (SELECT translation_of FROM " + getTableName() + " " + itemWhere + ") " +
                "UNION SELECT " + fields + " FROM " + getTableName() + " " + itemWhere + " " +
                "UNION SELECT " + fields + " FROM " + getTableName() + " " + itemWhere + ") ";
        String filterQuery = query + "WHERE value LIKE '?%' ORDER BY value";
        query += "ORDER BY value";

        String vocabularyUidStr = Long.valueOf(vocabularyUid).toString();
        String[] args = new String[] { vocabularyUidStr, baseLang, "1",
                vocabularyUidStr, locale.getLanguage(), "1",
                vocabularyUidStr, locale.getLanguage(), "1",
                vocabularyUidStr, locale.getLanguage(), "0" };

        Cursor cursor = null;
        if (search.length() > 0) {
            args[args.length] = search;
            cursor = db.rawQuery(filterQuery, args);
        }
        else {
            cursor = db.rawQuery(query, args);
        }

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            VocabularyItem vocabularyItem = null;
            do {
                vocabularyItem = populateObject(cursor);
                list.add(vocabularyItem);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        return list;
    }

    /**
     * Gets a list of vocabulary items of the specified language associated to a specific value. For
     * predefined items the base version is returned if there is no translation available.
     * @param valueUid uid of the value where the items are associated to
     * @param locale language of the items
     * @return list of vocabulary items in the specified language and for predefined items also
     * items of the base language if no translation exists
     */
    public List<VocabularyItem> getVocabularyItemsOfValue(long valueUid, Locale locale) {
        ArrayList<VocabularyItem> list = new ArrayList<VocabularyItem>();
        SQLiteDatabase db = getDb();
        String baseLang = ((BeepMeApp)ctx).getCurrentProject().getLanguage().getLanguage();

        // todo test query with high number of items, may need performance optimization
        String fields = "vo._id, vo.name, vo.lang, vo.value, vo.predefined, vo.vocabulary_id, vo.translation_of";
        String itemWhere = "WHERE va.value_id=? AND vo.lang=? AND vo.predefined=?";
        String tableJoin = "FROM " + getTableName() + " vo INNER JOIN " + ValueVocabularyItemTable.getTableName() +
                " va ON va.vocabulary_item_id=vo._id";

        String query = "SELECT " + fields + " FROM" +
                "(SELECT " + fields + " " + tableJoin + " " + itemWhere + " AND " +
                "_id NOT IN (SELECT translation_of " + tableJoin + " " + itemWhere + ") " +
                "UNION SELECT " + fields + " " + tableJoin + " " + itemWhere + " " +
                "UNION SELECT " + fields + " " + tableJoin + " " + itemWhere + ") " +
                "ORDER BY value";

        String valueUidStr = Long.valueOf(valueUid).toString();
        String[] args = new String[] { valueUidStr, baseLang, "1",
                valueUidStr, locale.getLanguage(), "1",
                valueUidStr, locale.getLanguage(), "1",
                valueUidStr, locale.getLanguage(), "0" };

        Cursor cursor = db.rawQuery(query, args);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            VocabularyItem vocabularyItem = null;
            do {
                vocabularyItem = populateObject(cursor);
                list.add(vocabularyItem);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        return list;
    }
}