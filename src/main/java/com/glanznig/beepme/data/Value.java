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

/**
 * A value represents data entered by the user for a specific moment. It is associated to an
 * input element and a moment. There exist SingleValues and MultiValues.
 */
public class Value {

    private Long uid;
    private Long inputElementUid;
    private Long momentUid;

    private String inputElementName;

    public Value() {
        uid = null;
        inputElementUid = null;
        momentUid = null;

        inputElementName = null;
    }

    public Value(long uid) {
        setUid(uid);
        inputElementUid = null;
        momentUid = null;

        inputElementName = null;
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
    protected void setUid(long uid) {
        this.uid = Long.valueOf(uid);
    }

    /**
     * Sets the input element uid where this value is associated to
     * @param inputElementUid input element uid of associated input element
     */
    public void setInputElementUid(long inputElementUid) {
        this.inputElementUid = Long.valueOf(inputElementUid);
    }

    /**
     * Gets the input element uid where this value is associated to
     * @return input element uid of associated element, or 0L if not set
     */
    public long getInputElementUid() {
        if (inputElementUid != null) {
            return inputElementUid.longValue();
        }

        return 0L;
    }

    /**
     * Sets the moment uid where this value is associated to
     * @param momentUid moment uid of associated moment
     */
    public void setMomentUid(long momentUid) {
        this.momentUid = Long.valueOf(momentUid);
    }

    /**
     * Gets the moment uid where this value is associated to
     * @return moment uid of associated moment, or 0L if not set
     */
    public long getMomentUid() {
        if (momentUid != null) {
            return momentUid.longValue();
        }

        return 0L;
    }

    /**
     * Sets the name (display id) of the associated input element.
     * @param name name (display id) of input element
     */
    public void setInputElementName(String name) {
        inputElementName = name;
    }

    /**
     * Gets the name (display id) of the associated input element
     * @return name (display id), or null if not set
     */
    public String getInputElementName() {
        return inputElementName;
    }

    @Override
    public int hashCode() {
        return uid != null ? this.getClass().hashCode() + uid.hashCode() : super.hashCode();
    }
}
