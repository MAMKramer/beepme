package com.glanznig.beepme.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A MultiValue (data entered by the user) can have multiple values. Examples are tags or
 * multi-select input. These values belong to a vocabulary and represent references to
 * vocabulary items.
 */
public class MultiValue extends Value {

    private static final String DELIMITER = ".#.";

    private HashMap<String, VocabularyItem> values;

    public MultiValue() {
        super();
        values = new HashMap<String, VocabularyItem>();
    }

    public MultiValue(long uid) {
        super(uid);
        values = new HashMap<String, VocabularyItem>();
    }

    /**
     * Sets a value (vocabulary item) for this multi-value
     * @param value vocabulary item that should be part of this multi-value
     */
    public void setValue(VocabularyItem value) {
        values.put(value.getValue(), value);
    }

    /**
     * Returns whether a particular value is part of this multi-value
     * @param value vocabulary item value of referenced item
     * @return true if part of this multi-value, false otherwise
     */
    public boolean hasValue(String value) {
        return values.containsKey(value);
    } //todo translations?

    /**
     * Gets all values (vocabulary items) that are part of this multi-value
     * @return Collection of vocabulary items that are referenced by this multi-value
     */
    public Collection<VocabularyItem> getValues() {
        return values.values();
    }

    /**
     * Gets a string representation of all values referenced by this MultiValue. The delimiter character
     * is a comma (,).
     * @return comma delimited string representation of all value references
     */
    public String getValueString() {
        String valueString = "";
        Iterator<VocabularyItem> valueIterator = values.values().iterator();
        while (valueIterator.hasNext()) {
            VocabularyItem value = valueIterator.next();
            valueString += value.getValue();
            if (valueIterator.hasNext()) {
                valueString += ",";
            }
        }

        return valueString;
    }

    /**
     * Gets a string representation of this MultiValue.
     * @return string representation of all VocabularyItem references
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

        Iterator<VocabularyItem> valueIterator = values.values().iterator();
        if (valueIterator.hasNext()) {
            if (!first) {
                representation += DELIMITER;
            }
            representation += "values=";
        }
        while (valueIterator.hasNext()) {
            VocabularyItem value = valueIterator.next();
            representation += value.toString();
            if (valueIterator.hasNext()) {
                representation += ",";
            }
        }

        return representation;
    }

    /**
     * Transforms a string representation of a MultiValue object (e.g. from storage) into
     * an object.
     * @param objRepresentation string representation of MultiValue object
     * @return MultiValue object, or null if string representation was not valid
     */
    public static MultiValue fromString(String objRepresentation) {
        //todo correct regex for string validation
        if (objRepresentation.toLowerCase().matches("^uid=(edit|delete),allowed=(yes|no)(,until=\\d+)?$")) {
            String uid = "";
            String inputElementUid = "";
            String inputElementName = "";
            String momentUid = "";
            String values = "";

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
                else if (splitRep[i].startsWith("values")) {
                    values = splitRep[i].substring(7);
                }
            }

            MultiValue multiValue = null;
            if (uid.length() > 0) {
                multiValue = new MultiValue(Long.valueOf(uid));
            }
            else {
                multiValue = new MultiValue();
            }
            if (inputElementUid.length() > 0) {
                multiValue.setInputElementUid(Long.valueOf(inputElementUid));
            }
            if (inputElementName.length() > 0) {
                multiValue.setInputElementName(inputElementName);
            }
            if (momentUid.length() > 0) {
                multiValue.setMomentUid(Long.valueOf(momentUid));
            }
            if (values.length() > 0) {
                String[] split = values.split(",");
                for (int i=0; i < splitRep.length; i++) {
                    multiValue.setValue(VocabularyItem.fromString(split[i]));
                }
            }

            return multiValue;
        }
        return null;
    }

    /**
     * Copies all member variables (except uid) to a new object
     * @param copy copy object
     */
    public void copyTo(MultiValue copy) {
        copy.setMomentUid(copy.getMomentUid());
        copy.setInputElementUid(copy.getInputElementUid());
        copy.setInputElementName(copy.getInputElementName());

        Iterator<VocabularyItem> valueIterator = values.values().iterator();
        while (valueIterator.hasNext()) {
            copy.setValue(valueIterator.next());
        }
    }
}
