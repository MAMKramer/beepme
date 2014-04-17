package com.glanznig.beepme.data;

import java.util.HashMap;
import java.util.Set;

/**
 * A MultiValue (data entered by the user) can have multiple values. Examples are tags or
 * multi-select input. These values belong to a vocabulary and represent references to
 * vocabulary items.
 */
public class MultiValue extends Value {

    private HashMap<Long, Long> values;

    public MultiValue() {
        super();
        values = new HashMap<Long, Long>();
    }

    public MultiValue(long uid) {
        super(uid);
        values = new HashMap<Long, Long>();
    }

    /**
     * Sets a value reference for this multi-value
     * @param valueUid vocabulary item uid of referenced item
     */
    public void setValue(long valueUid) {
        values.put(new Long(valueUid), null);
    }

    /**
     * Returns whether a particular value reference (vocabulary item uid) is part of this multi-value
     * @param valueUid vocabulary item uid of referenced item
     * @return true if part of this multi-value, false otherwise
     */
    public boolean hasValue(long valueUid) {
        return values.containsKey(new Long(valueUid));
    }

    /**
     * Gets all value references that are part of this multi-value
     * @return Set of Long containing all vocabulary item uids that are referenced by this multi-value
     */
    public Set<Long> getValues() {
        return values.keySet();
    }
}
