package com.glanznig.beepme.data;

/**
 * A single value (data entered by the user) can have a single item only. Examples are text
 * input or media URIs.
 */
public class SingleValue extends Value {

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
}
