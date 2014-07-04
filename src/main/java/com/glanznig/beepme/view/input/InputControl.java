package com.glanznig.beepme.view.input;

import com.glanznig.beepme.data.Value;

import java.io.Serializable;

/**
 * Base interface for common methods of input controls (UI elements).
 */
public interface InputControl {

    /**
     * InputControl can be (re-)used for entering (INPUT) and viewing (VIEW) data.
     */
    public enum Mode {
        CREATE, EDIT, VIEW;
    }

    /**
     * Sets the value of this input control.
     * @param value the value
     */
    public void setValue(Value value);

    /**
     * Gets the value of this input control.
     * @return the value or null if not set
     */
    public Value getValue();

    /**
     * Sets the title (short description of what should be entered) of this input control.
     * @param title the title
     */
    public void setTitle(String title);

    /**
     * Sets the help text of this input control.
     * @param help the help text
     */
    public void setHelpText(String help);

    /**
     * Sets the name (display id) of this input control.
     * @param name name (display id)
     */
    public void setName(String name);

    /**
     * Gets the name (display id) of this input control.
     * @return name (display id) or null if not set
     */
    public String getName();

    /**
     * Sets whether this input control is mandatory for input or not.
     * @param mandatory true if mandatory, false otherwise
     */
    public void setMandatory(boolean mandatory);

    /**
     * Gets whether this input control is mandatory for input or not.
     * @return true if mandatory, false otherwise
     */
    public boolean getMandatory();
}
