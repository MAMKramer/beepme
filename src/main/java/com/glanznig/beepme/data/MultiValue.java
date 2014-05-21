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
    }

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
}
