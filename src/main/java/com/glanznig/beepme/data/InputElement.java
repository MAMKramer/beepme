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
import java.util.HashMap;

/**
 * An input element represents a method of data entry on a more abstract level and a specific UI
 * component on a more specific level. There are several possibilities: text entry, photos, tags,
 * single- and multi-selection, (Likert-)scale input, etc.
 */
public class InputElement {

    /**
     * Specifies different possible input element types.
     *
     * PHOTO -
     * TEXT - one or multi-line text input element
     * TAGS - multiple either pre-defined only or also user-defined tags (keywords), different from
     *        multi-select in terms of UI presentation
     */
    public enum InputElementType {
        PHOTO, TEXT, TAGS
    }

    private Long uid;
    private InputElementType type;
    private String name;
    private Boolean mandatory;
    private HashMap<Restriction.RestrictionType, Restriction> restrictions;
    private String title;
    private String help;
    private Bundle options;
    private Long vocabularyUid;
    private Long inputGroupUid;

    public InputElement() {
        uid = null;
        type = null;
        name = null;
        mandatory = Boolean.FALSE;
        restrictions = new HashMap<Restriction.RestrictionType, Restriction>();
        title = null;
        help = null;
        options = new Bundle();
        vocabularyUid = null;
        inputGroupUid = null;
    }

    public InputElement(long uid) {
        setUid(uid);
        type = null;
        name = null;
        mandatory = Boolean.FALSE;
        restrictions = new HashMap<Restriction.RestrictionType, Restriction>();
        title = null;
        help = null;
        options = new Bundle();
        vocabularyUid = null;
        inputGroupUid = null;
    }

    public InputElement(long uid, boolean mandatory) {
        setUid(uid);
        type = null;
        name = null;
        this.mandatory = new Boolean(mandatory);
        restrictions = new HashMap<Restriction.RestrictionType, Restriction>();
        title = null;
        help = null;
        options = new Bundle();
        vocabularyUid = null;
        inputGroupUid = null;
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
     * Sets the input element type (text, tags, photo ...)
     * @param type input element type enum
     */
    public void setType(InputElementType type) {
        this.type = type;
    }

    /**
     * Gets the input element type (text, tags, photo ...)
     * @return input element type, or null if not set
     */
    public InputElementType getType() {
        return type;
    }

    /**
     * Sets the name (string id) of this input element
     * @param name the name (string id)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name (string id) of this input element
     * @return the name (string id), or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Gets whether it is mandatory to fill in this input item.
     * @return true if mandatory, false otherwise
     */
    public boolean isMandatory() {
        return mandatory.booleanValue();
    }

    /**
     * Gets options for this input element (string value bundle)
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
     * Gets an option for this input element
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
     * Gets restrictions for this input element
     * @return collection of restrictions currently active for this project
     */
    public Collection<Restriction> getRestrictions() {
        return restrictions.values();
    }

    /**
     * Sets a title that provides a hint what is to be entered in this input element
     * @param title input hint
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the title (input hint)
     * @return title, or null if not set
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets a (short) help text that describes what the user has to do
     * @param help input help
     */
    public void setHelp(String help) {
        this.help = help;
    }

    /**
     * Gets a (short) help text that describes what the user has to do
     * @return help text, or null if not set
     */
    public String getHelp() {
        return help;
    }

    /**
     * Sets the vocabulary uid which this input item uses for available choices
     * @param vocabularyUid vocabulary uid of source of available choices
     */
    public void setVocabularyUid(long vocabularyUid) {
        this.vocabularyUid = Long.valueOf(vocabularyUid);
    }

    /**
     * Gets the vocabulary uid which this input item uses for available choices
     * @return vocabulary uid of source of available choices, or 0L if not set
     */
    public long getVocabularyUid() {
        if (vocabularyUid != null) {
            return vocabularyUid.longValue();
        }

        return 0L;
    }

    /**
     * Sets the input group uid where this input item belongs to
     * @param inputGroupUid input group uid of parent input group
     */
    public void setInputGroupUid(long inputGroupUid) {
        this.inputGroupUid = Long.valueOf(inputGroupUid);
    }

    /**
     * Gets the input group uid where this input item belongs to
     * @return input group uid of parent input group, or 0L if not set
     */
    public long getInputGroupUid() {
        if (inputGroupUid != null) {
            return inputGroupUid.longValue();
        }

        return 0L;
    }

    @Override
    public int hashCode() {
        return uid != null ? this.getClass().hashCode() + uid.hashCode() : super.hashCode();
    }
}
