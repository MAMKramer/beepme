package com.glanznig.beepme.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.data.InputElement;
import com.glanznig.beepme.data.InputGroup;
import com.glanznig.beepme.data.Project;
import com.glanznig.beepme.data.db.InputElementTable;
import com.glanznig.beepme.data.db.InputGroupTable;
import com.glanznig.beepme.view.input.InputControl;
import com.glanznig.beepme.view.input.TagControl;
import com.glanznig.beepme.view.input.TextControl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by michael on 13.05.14.
 */
public class ViewManager {

    private Context ctx;
    private HashMap<String, InputControl> controls;

    public ViewManager(Context ctx) {
        this.ctx = ctx;
        controls = new HashMap<String, InputControl>();
    }

    public View getLayout(InputControl.Mode mode) {
        View rootView = null;
        BeepMeApp app = (BeepMeApp)ctx.getApplicationContext();
        Project project = app.getCurrentProject();
        List<InputGroup> inputGroupList = new InputGroupTable(ctx).getInputGroups(project.getUid());

        if (inputGroupList.size() > 1) {
            // we have several input groups and need a ViewPager
            ViewPager pager = new ViewPager(ctx);
            Iterator<InputGroup> inputGroupIterator = inputGroupList.iterator();

            while (inputGroupIterator.hasNext()) {
                pager.addView(addElements(mode, inputGroupIterator.next()));
            }

            rootView = pager;
        }
        else {
            // we have only one input group and can leave out the ViewPager
            rootView = addElements(mode, inputGroupList.get(0));
        }

        return rootView;
    }

    public InputControl getInputControl(String name) {
        return controls.get(name);
    }

    private ScrollView addElements(InputControl.Mode mode, InputGroup inputGroup) {
        ScrollView scrollView = new ScrollView(ctx);
        LinearLayout linearLayout = new LinearLayout(ctx);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout);

        // todo get tanslations
        List<InputElement> inputElements = new InputElementTable(ctx).getInputElementsByGroup(inputGroup.getUid());
        Iterator<InputElement> inputElementIterator = inputElements.iterator();

        View control = null;
        while (inputElementIterator.hasNext()) {
            InputElement inputElement = inputElementIterator.next();

            switch (inputElement.getType()) {
                case PHOTO:
                    break;

                case TAGS:
                    control = createTagControl(inputElement, mode);
                    break;

                case TEXT:
                    control = createTextControl(inputElement, mode);
                    break;
            }

            controls.put(inputElement.getName(), (InputControl)control);
            linearLayout.addView(control);
        }

        return scrollView;
    }

    private TextControl createTextControl(InputElement inputElement, InputControl.Mode mode) {
        Locale locale = Locale.getDefault();
        TextControl textControl = null;
        if (inputElement.getType().equals(InputElement.InputElementType.TEXT)) {
            textControl = new TextControl(ctx, mode, inputElement.getRestrictions());
            textControl.setName(inputElement.getName());
            textControl.setMandatory(inputElement.isMandatory());
            if (inputElement.getOption("lines") != null) {
                textControl.setLines(Integer.valueOf(inputElement.getOption("lines")).intValue());
            }
            // todo fallback to default language
            textControl.setTitle(inputElement.getTitle(locale.getLanguage()));
            textControl.setHelpText(inputElement.getHelp(locale.getLanguage()));
        }

        return  textControl;
    }

    private TagControl createTagControl(InputElement inputElement, InputControl.Mode mode) {
        Locale locale = Locale.getDefault();
        TagControl tagControl = null;
        if (inputElement.getType().equals(InputElement.InputElementType.TAGS)) {
            tagControl = new TagControl(ctx, mode, inputElement.getRestrictions());
            tagControl.setName(inputElement.getName());
            tagControl.setMandatory(inputElement.isMandatory());
            tagControl.setVocabularyUid(inputElement.getVocabularyUid());
            // todo fallback to default language
            tagControl.setTitle(inputElement.getTitle(locale.getLanguage()));
            tagControl.setHelpText(inputElement.getHelp(locale.getLanguage()));
        }
        return tagControl;
    }
}
