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

import com.glanznig.beepme.data.Project;
import com.glanznig.beepme.data.timer.RandomTimer;
import com.glanznig.beepme.data.Restriction;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * Represents the table PROJECT (logical units related to research projects)
 */
public class ProjectTable extends StorageHandler {

    private static final String TAG = "ProjectTable";

    private static final String TBL_NAME = "project";
    private static final String TBL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "type INTEGER NOT NULL, " +
                    "status INTEGER NOT NULL, " +
                    "start INTEGER, " +
                    "expire INTEGER, " +
                    "lang TEXT NOT NULL, " +
                    "restrictions TEXT, " +
                    "timer TEXT NOT NULL, " +
                    "options TEXT NOT NULL" +
                    ")";

    private static HashMap<Project.ProjectType, Integer> typeMap;
    private static HashMap<Integer, Project.ProjectType> invTypeMap;
    private static HashMap<Project.ProjectStatus, Integer> statusMap;
    private static HashMap<Integer, Project.ProjectStatus> invStatusMap;

    static {
        Integer zero = new Integer(0);
        Integer one = new Integer(1);
        Integer two = new Integer(2);
        Integer three = new Integer(3);

        Project.ProjectType sampling = Project.ProjectType.SAMPLING;
        Project.ProjectType probes = Project.ProjectType.PROBES;
        Project.ProjectType lifelog = Project.ProjectType.LIFELOG;

        Project.ProjectStatus active = Project.ProjectStatus.ACTIVE;
        Project.ProjectStatus archived = Project.ProjectStatus.ARCHIVED;

        typeMap = new HashMap<Project.ProjectType, Integer>();
        invTypeMap = new HashMap<Integer, Project.ProjectType>();
        typeMap.put(sampling, one);
        typeMap.put(probes, two);
        typeMap.put(lifelog, three);
        invTypeMap.put(one, sampling);
        invTypeMap.put(two, probes);
        invTypeMap.put(three, lifelog);

        statusMap = new HashMap<Project.ProjectStatus, Integer>();
        invStatusMap = new HashMap<Integer, Project.ProjectStatus>();
        statusMap.put(active, one);
        statusMap.put(archived, zero);
        invStatusMap.put(one, active);
        invStatusMap.put(zero, archived);
    }

    private Context ctx;

    public ProjectTable(Context ctx) {
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
     * Populates content values for the set variables of the project.
     * @param project the project
     * @return populated content values
     */
    private ContentValues getContentValues(Project project) {
        ContentValues values = new ContentValues();

        if (project.getName() != null) {
            values.put("name", project.getName());
        }
        if (project.getType() != null) {
            values.put("type", typeMap.get(project.getType()));
        }
        if (project.getStatus() != null) {
            values.put("status", statusMap.get(project.getStatus()));
        }
        if (project.getStart() != null) {
            values.put("start", String.valueOf(project.getStart().getTime()));
        }
        if (project.getExpire() != null) {
            values.put("expire", String.valueOf(project.getExpire().getTime()));
        }
        if (project.getLanguage() != null) {
            values.put("lang", project.getLanguage().getLanguage());
        }

        Collection<Restriction> restrictions = project.getRestrictions();
        if (!restrictions.isEmpty()) {
            String restrictionsStr = "";
            Iterator<Restriction> restr = restrictions.iterator();
            while (restr.hasNext()) {
                Restriction r = restr.next();
                restrictionsStr += r.toString();
                if (restr.hasNext()) {
                    restrictionsStr += ";";
                }
            }
            values.put("restrictions", restrictionsStr);
        }

        if (project.getTimer() != null) {
            if (project.getTimer() instanceof RandomTimer) {
                values.put("timer", "type=random,"+project.getTimer().toString());
            }
        }

        values.put("options", project.getOptions());

        return values;
    }

    /**
     * Populates a Project object by reading values from a cursor
     * @param cursor cursor object
     * @return populated Project object
     */
    private Project populateObject(Cursor cursor) {
        Project project = new Project(cursor.getLong(0));
        project.setName(cursor.getString(1));
        project.setType(invTypeMap.get(cursor.getInt(2)));
        project.setStatus(invStatusMap.get(cursor.getInt(3)));
        if (!cursor.isNull(4)) {
            project.setStart(new Date(cursor.getLong(4)));
        }
        if (!cursor.isNull(5)) {
            project.setExpire(new Date(cursor.getLong(5)));
        }
        project.setLanguage(new Locale(cursor.getString(6)));
        if (!cursor.isNull(7)) {
            String[] restrictions = cursor.getString(7).split(";");
            for (int i=0; i < restrictions.length; i++) {
                project.setRestriction(Restriction.fromString(restrictions[i]));
            }
        }
        String timerString = cursor.getString(8);
        if (timerString.startsWith("type=random")) {
            timerString = timerString.substring(12);
            project.setTimer(RandomTimer.fromString(ctx, timerString));
        }
        String[] options = cursor.getString(9).split(",");
        for (int i=0; i < options.length; i++) {
            String option[] = options[i].split("=");
            project.setOption(option[0], option[1]);
        }

        return project;
    }

    /**
     * Adds a new project to the database
     * @param project values to add to the project table
     * @return new project object with set values and uid, or null if an error occurred
     */
    public Project addProject(Project project) {
        Project newProject = null;

        if (project != null) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(project);

            Log.i(TAG, "inserted values="+values);
            long projectId = db.insert(getTableName(), null, values);
            closeDb();
            // only if no error occurred
            if (projectId != -1) {
                newProject = new Project(projectId);
                project.copyTo(newProject);
            }
        }

        return newProject;
    }

    /**
     * Updates a project in the database
     * @param project values to update for this project
     * @return true on success or false if an error occurred
     */
    public boolean updateProject(Project project) {
        int numRows = 0;
        if (project.getUid() != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(project);

            numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(project.getUid()) });
            closeDb();
        }

        return numRows == 1;
    }

    /**
     * Gets an project entry by its uid.
     * @return project entry, or null if not found
     */
    public Project getProject(long uid) {
        SQLiteDatabase db = getDb();
        Project project = null;

        Cursor cursor = db.query(getTableName(), new String[] { "_id", "name", "type", "status", "start",
                "expire", "lang", "restrictions", "timer", "options" },
                "_id=?", new String[] { Long.valueOf(uid).toString() }, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            project = populateObject(cursor);
            cursor.close();
        }
        closeDb();

        return project;
    }

}
