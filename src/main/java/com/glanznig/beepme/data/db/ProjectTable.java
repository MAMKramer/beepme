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
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.glanznig.beepme.data.Project;
import com.glanznig.beepme.data.Restriction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

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

    public ProjectTable(Context ctx) {
        super(ctx);
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

    public Project addProject(Project project) {
        Project newProject = null;

        if (project != null) {
            SQLiteDatabase db = getDb();

            ContentValues values = new ContentValues();
            values.put("name", project.getName());
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

            Log.i(TAG, "inserted values=" + values);
            long projectId = db.insert(getTableName(), null, values);
            newProject = new Project(projectId);
            project.copyTo(newProject);
            db.close();
        }

        return newProject;
    }

}
