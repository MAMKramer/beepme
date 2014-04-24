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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A vocabulary represents a group of choice items. The user can use predefined ones for data
 * entry and he can also add own items (if allowed). Use cases for vocabularies and their items are
 * multiple or single choice selections and tags.
 */
public class Vocabulary {

    private Long uid;
    private String name;
    private Long projectUid;

    private ArrayList<VocabularyItem> items;

    public Vocabulary() {
        uid = null;
        name = null;
        projectUid = null;

        items = new ArrayList<VocabularyItem>();
    }

    public Vocabulary(long uid) {
        setUid(uid);
        name = null;
        projectUid = null;

        items = new ArrayList<VocabularyItem>();
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
     * Sets the name of this vocabulary
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this vocabulary
     * @return the name, or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the project uid where this vocabulary belongs to
     * @param projectUid project uid
     */
    public void setProjectUid(long projectUid) {
        this.projectUid = new Long(projectUid);
    }

    /**
     * Gets the project uid where this vocabulary belongs to
     * @return project uid, or 0L if not set
     */
    public long getProjectUid() {
        if (projectUid != null) {
            return projectUid.longValue();
        }
        return 0L;
    }

    /**
     * Assoicates a vocabulary item to this vocabulary.
     * @param item the item
     */
    public void addItem(VocabularyItem item) {
        items.add(item);
    }

    /**
     * Gets all vocabulary items that are associated to this vocabulary.
     * @return list of vocabulary items, or empty list if no items
     */
    public List<VocabularyItem> getItems() {
        return items;
    }

    /**
     * Copies all member variables (except uid) to a new object
     * @param copy copy object
     */
    public void copyTo(Vocabulary copy) {
        copy.setName(name);
        if (projectUid != null) {
            copy.setProjectUid(projectUid);
        }

        if (items != null) {
            Iterator<VocabularyItem> itemIterator = items.iterator();
            while (itemIterator.hasNext()) {
                copy.addItem(itemIterator.next());
            }
        }
    }

    @Override
    public int hashCode() {
        return uid != null ? this.getClass().hashCode() + uid.hashCode() : super.hashCode();
    }
}
