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

import android.util.Log;

/**
 * A single value (data entered by the user) can have a single item only. Examples are text
 * input or media URIs.
 */
public class SingleValue extends Value {

    private static final String DELIMITER = "\u0081";
    private static final String TAG = "SingleValue";

    private String value;

    public SingleValue() {
        super();
        value = null;
    }

    public SingleValue(long uid) {
        super(uid);
        value = null;
    }

    /**
     * Sets the (string) value of this single value
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the (string) value of this single value
     * @return value, or null if not set
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets a string representation of all VocabularyItems referenced by this MultiValue. The delimiter character
     * is a comma (,).
     * @return comma delimited string representation of all VocabularyItem references
     */
    public String toString() {
        String representation = "";
        boolean first = true;

        if (getUid() != 0L) {
            representation += "uid=" + getUid();
            first = false;
        }
        if (getInputElementUid() != 0L) {
            if (!first) {
                representation += DELIMITER;
            }
            representation += "inputElementUid=" + getInputElementUid();
            first = false;
        }
        if (getInputElementName() != null) {
            if (!first) {
                representation += DELIMITER;
            }
            representation += "inputElementName=" + getInputElementName();
            first = false;
        }
        if (getMomentUid() != 0L) {
            if (!first) {
                representation += DELIMITER;
            }
            representation += "momentUid=" + getMomentUid();
            first = false;
        }

        if (value != null) {
            if (!first) {
                representation += DELIMITER;
            }
            representation += "value=" + value;
        }

        return representation;
    }

    /**
     * Transforms a string representation of a SingleValue object (e.g. from storage) into
     * an object.
     * @param objRepresentation string representation of SingleValue object
     * @return SingleValue object, or null if string representation was not valid
     */
    public static SingleValue fromString(String objRepresentation) {
        String regex = "^(uid=\\d+"+DELIMITER+")?inputElementUid=\\d+("+DELIMITER+"inputElementName=\\w+)?("+DELIMITER+"momentUid=\\d+)?"+DELIMITER+
                "value=.*$";
        if (objRepresentation.matches(regex)) {
            String uid = "";
            String inputElementUid = "";
            String inputElementName = "";
            String momentUid = "";
            String value = "";

            String[] splitRep = objRepresentation.split(DELIMITER);
            for (int i=0; i < splitRep.length; i++) {
                if (splitRep[i].startsWith("uid")) {
                    uid = splitRep[i].substring(4);
                }
                else if (splitRep[i].startsWith("inputElementUid")) {
                    inputElementUid = splitRep[i].substring(16);
                }
                else if (splitRep[i].startsWith("inputElementName")) {
                    inputElementName = splitRep[i].substring(17);
                }
                else if (splitRep[i].startsWith("momentUid")) {
                    momentUid = splitRep[i].substring(10);
                }
                else if (splitRep[i].startsWith("value")) {
                    value = splitRep[i].substring(6);
                }
            }

            SingleValue singleValue = null;
            if (uid.length() > 0) {
                singleValue = new SingleValue(Long.valueOf(uid));
            }
            else {
                singleValue = new SingleValue();
            }
            if (inputElementUid.length() > 0) {
                singleValue.setInputElementUid(Long.valueOf(inputElementUid));
            }
            if (inputElementName.length() > 0) {
                singleValue.setInputElementName(inputElementName);
            }
            if (momentUid.length() > 0) {
                singleValue.setMomentUid(Long.valueOf(momentUid));
            }
            if (value.length() > 0) {
                singleValue.setValue(value);
            }

            return singleValue;
        }
        return null;
    }

    /**
     * Copies all member variables (except uid) to a new object
     * @param copy copy object
     */
    public void copyTo(SingleValue copy) {
        copy.setMomentUid(copy.getMomentUid());
        copy.setInputElementUid(copy.getInputElementUid());
        copy.setInputElementName(copy.getInputElementName());
        copy.setValue(copy.getValue());
    }
}
