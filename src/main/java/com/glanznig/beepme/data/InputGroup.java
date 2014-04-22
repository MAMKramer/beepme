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
 * A input group is a collection of input elements that (logically, naturally) belong together.
 * Such a input group is normally displayed on a screen together, navigating (swiping) between
 * input groups is possible. A group has a string id (name), a display name (title) and belongs
 * to a project.
 */
public class InputGroup {

    private Long uid;
    private String name;
    private String title;
    private Long projectUid;

    private ArrayList<InputElement> inputElements;

    public InputGroup() {
        uid = null;
        name = null;
        title = null;
        projectUid = null;

        inputElements = null;
    }

    public InputGroup(long uid) {
        setUid(uid);
        name = null;
        title = null;
        projectUid = null;

        inputElements = null;
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
     * Sets the name (string id) of this input group
     * @param name the name (string id)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name (string id) of this input group
     * @return the name (string id), or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name (title) of this input group
     * @param title display name
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the display name (title) of this input group
     * @return display name, or null if not set
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the project uid where this input group belongs to
     * @param projectUid project uid
     */
    public void setProjectUid(long projectUid) {
        this.projectUid = new Long(projectUid);
    }

    /**
     * Gets the project uid where this input group belongs to
     * @return project uid, or 0L if not set
     */
    public long getProjectUid() {
        if (projectUid != null) {
            return projectUid.longValue();
        }
        return 0L;
    }

    /**
     * Associates an input element with this input group
     * @param element the input element
     */
    public void addInputElement(InputElement element, int pos) {
        if (inputElements == null) {
            inputElements = new ArrayList<InputElement>();
        }
        inputElements.add(pos, element);
    }

    /**
     * Gets the associated input elements for this input group.
     * @return associated input elements, or empty list if none
     */
    public List<InputElement> getInputElements() {
        if (inputElements != null) {
            return inputElements;
        }
        return new ArrayList<InputElement>();
    }

    /**
     * Copies all member variables (except uid) to a new object
     * @param copy copy object
     */
    public void copyTo(InputGroup copy) {
        copy.setName(name);
        copy.setTitle(title);
        if (projectUid != null) {
            copy.setProjectUid(projectUid);
        }

        if (inputElements != null) {
            Iterator<InputElement> inputElementIterator = inputElements.iterator();
            int pos = 0;
            while (inputElementIterator.hasNext()) {
                copy.addInputElement(inputElementIterator.next(), pos);
                pos++;
            }
        }
    }

    @Override
    public int hashCode() {
        return uid != null ? this.getClass().hashCode() + uid.hashCode() : super.hashCode();
    }
}
