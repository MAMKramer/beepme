package com.glanznig.beepme.view.input;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.glanznig.beepme.data.Restriction;
import com.glanznig.beepme.data.SingleValue;
import com.glanznig.beepme.data.Value;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by michael on 09.05.14.
 */
public class TextControl extends LinearLayout implements InputControl {

    private static final String TAG = "TextControl";

    private Context ctx;
    private Mode mode;
    private String name;
    private boolean mandatory;
    private boolean restrictEdit;

    private EditText textInput;
    private TextView textDisplay;
    private TextView help;
    private TextView title;

    public TextControl(Context ctx, Mode mode, Collection<Restriction> restrictions) {
        super(ctx);
        this.ctx = ctx.getApplicationContext();
        this.mode = mode;
        name = null;
        mandatory = false;

        if (restrictions != null) {
            restrictEdit = false;

            Iterator<Restriction> restrictionIterator = restrictions.iterator();
            while (restrictionIterator.hasNext()) {
                Restriction restriction = restrictionIterator.next();
                // todo if delete allowed button to delete text content ??
                if (restriction.getType().equals(Restriction.RestrictionType.EDIT) && restriction.getAllowed() == false) {
                    restrictEdit = true;
                }
                if (restriction.getType().equals(Restriction.RestrictionType.DELETE) && restriction.getAllowed() == false) {
                    restrictEdit = true;
                }
            }
        }
        else {
            restrictEdit = false;
        }

        setupView();
    }

    public TextControl(Context ctx) {
        super(ctx);
        this.ctx = ctx.getApplicationContext();
        this.mode = Mode.CREATE;
        name = null;
        mandatory = false;
        restrictEdit = false;

        setupView();
    }

    public TextControl(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        this.ctx = ctx.getApplicationContext();
        this.mode = Mode.CREATE;
        name = null;
        mandatory = false;
        restrictEdit = false;

        setupView();
    }

    private void setupView() {
        setOrientation(LinearLayout.VERTICAL);
        help = new TextView(ctx);
        if (mode.equals(Mode.CREATE) || (mode.equals(Mode.EDIT) && !restrictEdit)) {
            textInput = new EditText(ctx);

            addView(textInput);
            addView(help);
        }
        else if (mode.equals(Mode.VIEW) || (mode.equals(Mode.EDIT) && restrictEdit)) {
            title = new TextView(ctx);
            textDisplay = new TextView(ctx);

            addView(textDisplay);
        }
    }

    public void setLines(int lines) {
        if (mode.equals(Mode.CREATE) || (mode.equals(Mode.EDIT) && !restrictEdit)) {
            textInput.setLines(lines);
        }
    }

    @Override
    public void setValue(Value value) {
        if (value instanceof SingleValue) {
            SingleValue singleValue = (SingleValue)value;

            if (mode.equals(Mode.CREATE) || (mode.equals(Mode.EDIT) && !restrictEdit)) {
                textInput.setText(singleValue.getValue());
            } else if (mode.equals(Mode.VIEW) || (mode.equals(Mode.EDIT) && restrictEdit)) {
                textDisplay.setText(singleValue.getValue());
            }
        }
    }

    @Override
    public Value getValue() {
        SingleValue value = new SingleValue();
        if (mode.equals(Mode.CREATE) || (mode.equals(Mode.EDIT) && !restrictEdit)) {
            value.setValue(textInput.getText().toString());
        }
        else if (mode.equals(Mode.VIEW) || (mode.equals(Mode.EDIT) && restrictEdit)) {
                value.setValue(textDisplay.getText().toString());
        }
        return value;
    }

    @Override
    public void setHelpText(String help) {
        this.help.setText(help);
    }

    @Override
    public void setTitle(String title) {
        if (mode.equals(Mode.CREATE) || (mode.equals(Mode.EDIT) && !restrictEdit)) {
            textInput.setHint(title);
        }
        else if (mode.equals(Mode.VIEW) || (mode.equals(Mode.EDIT) && restrictEdit)) {
            this.title.setText(title);
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public boolean getMandatory() {
        return mandatory;
    }
}
