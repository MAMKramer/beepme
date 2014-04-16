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

package com.glanznig.beepme.data;

import android.os.Bundle;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * A project is an organizational unit for data collection. It defines start, end, input
 * contents (with their translations) and associated data. As a whole it represents all the
 * users options and activities on a specific topic.
 *
 * Several projects can exist, but only one can be active at a time. Previous projects have to
 * be archived before a new one can be started. However, archived projects can become active again. (?)
 */
public class Project {

    /**
     * Different project types handle the way if the user can create moments himself or not and
     * whether a timer for delivering beeps is used.
     *
     * SAMPLING - user cannot create moments, beeps are delivered
     * PROBES - user creates moments, no beeps are delivered
     * LIFELOG - user can create moments, but also beeps are delivered
     */
    public enum ProjectType {
        SAMPLING, PROBES, LIFELOG
    }

    /**
     * The project status determines whether the project is active or not. New data can be created
     * only for active project. Only one project can be active at a time.
     *
     * ACTIVE - project is active, data can be entered (if project did already start)
     * ARCHIVED - project is archived, data cannot be entered
     */
    public enum ProjectStatus {
        ACTIVE, ARCHIVED
    }

    private Long uid;
    private String name;
    private ProjectType type;
    private ProjectStatus status;
    private Date start;
    private Date expire;
    private Locale lang;
    private Bundle options;
    private HashMap<Restriction.RestrictionType, Restriction> restrictions;
    private Bundle timer;

    public Project() {
        uid = null;
        name = null;
        type = null;
        status = null;
        start = null;
        expire = null;
        lang = null;
        options = new Bundle();
        restrictions = new HashMap<Restriction.RestrictionType, Restriction>();
        timer = new Bundle();
    }

    public Project(long uid) {
        setUid(uid);
        name = null;
        type = null;
        status = null;
        start = null;
        expire = null;
        lang = null;
        options = new Bundle();
        restrictions = new HashMap<Restriction.RestrictionType, Restriction>();
        timer = new Bundle();
    }

    /**
     * get unique identifier
     * @return uid (primary key)
     */
    public long getUid() {
        if (uid != null) {
            return uid.longValue();
        }
        else {
            return 0L;
        }
    }

    /**
     * set unique identifier
     * @param uid uid (primary key)
     */
    private void setUid(long uid) {
        this.uid = Long.valueOf(uid);
    }

    /**
     * Sets the project name
     * @param name project name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the project name
     * @return project name, or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the project type (sampling, probes, life logging)
     * @param type project type enum
     */
    public void setType(ProjectType type) {
        this.type = type;
    }

    /**
     * Gets the project type (sampling, probes, life logging)
     * @return project type, or null if not set
     */
    public ProjectType getType() {
        return type;
    }

    /**
     * Sets the project status (active, archived)
     * @param status project status enum
     */
    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    /**
     * Gets the project status (active, archived)
     * @return project status, or null if not set
     */
    public ProjectStatus getStatus() {
        return status;
    }

    /**
     * Sets the start time of the project (no data can be entered before)
     * @param start start time
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * Gets the start time of the project (no data can be entered before)
     * @return start time, or null if not set
     */
    public Date getStart() {
        return start;
    }

    /**
     * Sets the expire time of the project (no data can be entered afterwards)
     * @param expire expire time
     */
    public void setExpire(Date expire) {
        this.expire = expire;
    }

    /**
     * Gets the expire time of the project (no data can be entered afterwards)
     * @return expire time, or null if not set
     */
    public Date getExpire() {
        return expire;
    }

    /**
     * Sets the default language for this project (for managing translations)
     * @param lang locale with language setting
     */
    public void setLanguage(Locale lang) {
        this.lang = lang;
    }

    /**
     * Gets the default language for this project (for managing translations)
     * @return locale with language setting, or null if not set
     */
    public Locale getLanguage() {
        return lang;
    }

    /**
     * Gets options for this project (string value bundle)
     * @return bundle containing string values, or null if not set
     */
    public Bundle getOptions() {
        return options;
    }

    /**
     * Sets a new option or replaces an existing option
     * @param key key identifier of option
     * @param value value of option
     */
    public void setOption(String key, String value) {
        if (key != null && value != null) {
            options.putString(key, value);
        }
    }

    /**
     * Gets an option for this project
     * @param key key identifier of option
     * @return option value, or null if key does not exist or is null
     */
    public String getOption(String key) {
        if (key != null) {
            return options.getString(key);
        }
        return null;
    }

    /**
     * Sets a new restriction or updates an existing one (only one restriction per type can exist)
     * @param restriction restriction to add or update
     */
    public void setRestriction(Restriction restriction) {
        restrictions.put(restriction.getType(), restriction);
    }

    /**
     * Gets a restriction according to restriction type
     * @param type restriction type (edit, delete)
     * @return restriction, or null if type is not restricted
     */
    public Restriction getRestriction(Restriction.RestrictionType type) {
        return restrictions.get(type);
    }

    /**
     * Gets restrictions for this project
     * @return collection of restrictions currently active for this project
     */
    public Collection<Restriction> getRestrictions() {
        return restrictions.values();
    }

    /**
     * Sets a new timer option or replaces an existing timer option
     * @param key key identifier of option
     * @param value value of option
     */
    public void setTimerOption(String key, String value) {
        if (key != null && value != null) {
            timer.putString(key, value);
        }
    }

    /**
     * Gets timer options for this project (string value bundle)
     * @return bundle containing string values, or null if not set
     */
    public Bundle getTimerOptions() {
        return timer;
    }

    /**
     * Gets a timer option for this project
     * @param key key identifier of option
     * @return option value, or null if key does not exist or is null
     */
    public String getTimerOption(String key) {
        if (key != null) {
            return timer.getString(key);
        }
        return null;
    }

    @Override
    public int hashCode() {
        return uid != null ? this.getClass().hashCode() + uid.hashCode() : super.hashCode();
    }
}
