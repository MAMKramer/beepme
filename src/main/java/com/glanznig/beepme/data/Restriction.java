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
 * A restriction defines an allowance or prohibition (as deviation from the default baseline).
 * Restrictions are related to edit and delete actions and can be valid for a certain time
 * period only. Such a time period is normally related to the creation of a moment.
 */
public class Restriction {

    public enum RestrictionType {
        EDIT, DELETE
    }

    private RestrictionType type;
    private Boolean allowed;
    private Long until; // in seconds

    public Restriction(RestrictionType type, boolean allowed) {
        this.type = type;
        this.allowed = new Boolean(allowed);
        until = null;
    }

    /**
     * Sets the type of the restriction (edit, delete)
     * @param type restriction type enumeration
     */
    public void setType(RestrictionType type) {
        this.type = type;
    }

    /**
     * Returns the restriction type of this Restriction
     * @return RestrictionType enum (edit, delete)
     */
    public RestrictionType getType() {
        return type;
    }

    /**
     * Sets whether this restriction is a permission or a prohibition
     * @param allowed true if allowed, false if denied
     */
    public void setAllowed(boolean allowed) {
        this.allowed = new Boolean(allowed);
    }

    /**
     * Returns whether this restriction is a permission or a prohibition
     * @return true if allowed, false if denied
     */
    public boolean getAllowed() {
        return allowed.booleanValue();
    }

    /**
     * Sets the time validity of this restriction in terms of seconds
     * @param until amount of seconds how long this restriction is valid (normally amount of seconds
     *              from the creation of a moment)
     */
    public void setUntil(long until) {
        this.until = new Long(until);
    }

    /**
     * Gets the time validity of this restriction in terms of seconds
     * @return amount of seconds how long this restriction is valid (normally amount of seconds
     * from the creation of a moment)
     */
    public long getUntil() {
        return until;
    }

    /**
     * Transforms the Restriction object into a string representation (for serialization, persistance).
     * The string has the form 'type={edit|delete},allowed={yes|no}[,until=SEC]'.
     * @return string representation of Restriction object
     */
    @Override
    public String toString() {
        String strRep = "type=";

        switch (type) {
            case EDIT:
                strRep += "edit";
                break;
            case DELETE:
                strRep += "delete";
                break;
        }

        strRep += ",allowed=";
        if (allowed) {
            strRep += "yes";
        }
        else {
            strRep += "no";
        }
        if (until != null) {
            strRep += ",until="+until;
        }
        return strRep;
    }

    /**
     * Transforms a string representation of a Restriction object (e.g. from storage) into
     * an object. The string has to have the form 'type={edit|delete},allowed={yes|no}[,until=SEC]'.
     * @param objRepresentation string representation of Restriction object
     * @return Restriction object, or null if string representation was not valid
     */
    public static Restriction fromString(String objRepresentation) {
        if (objRepresentation.toLowerCase().matches("^type=(edit|delete),allowed=(yes|no)(,until=\\d+)?$")) {
            String type = "";
            String allowed = "";
            String until = "";

            String[] splitRep = objRepresentation.split(",");
            for (int i=0; i < splitRep.length; i++) {
                if (splitRep[i].startsWith("type")) {
                    type = splitRep[i].substring(5);
                }
                else if (splitRep[i].startsWith("allowed")) {
                    allowed = splitRep[i].substring(8);
                }
                else if (splitRep[i].startsWith("until")) {
                    until = splitRep[i].substring(6);
                }
            }

            RestrictionType resType = RestrictionType.EDIT;
            boolean allow = true;

            if (type.equals("edit")) {
                resType = RestrictionType.EDIT;
            }
            else if (type.equals("delete")) {
                resType = RestrictionType.DELETE;
            }

            if (allowed.equals("yes")) {
                allow = true;
            }
            else if (allowed.equals("no")) {
                allow = false;
            }

            Restriction restriction = new Restriction(resType, allow);
            if (until != null) {
                restriction.setUntil(Long.valueOf(until));
            }

            return restriction;
        }
        return null;
    }
}
