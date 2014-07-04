package com.glanznig.beepme.data;

/**
 * A single value (data entered by the user) can have a single item only. Examples are text
 * input or media URIs.
 */
public class SingleValue extends Value {

    private static final String DELIMITER = ":#:";

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
        //todo correct regex for string validation
        if (objRepresentation.toLowerCase().matches("^uid=(edit|delete),allowed=(yes|no)(,until=\\d+)?$")) {
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
                else if (splitRep[i].startsWith("values")) {
                    value = splitRep[i].substring(7);
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
