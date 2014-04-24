package com.glanznig.beepme.data;

import java.util.Locale;

/**
 * A translation element translates a specific portion of text of the user interface. Normally, this
 * are help texts of specific input components. Each translation element specifies the language (locale),
 * the translation content, the associated input element and which text (which translation element) it
 * translates.
 */
public class TranslationElement {

    /**
     * specifies which part of an input element is targeted
     * CONTENT - content of the input element (e.g. text field)
     * TITLE - short hint of the input element
     * HELP -  longer hint (help text) of the input element
     */
    public enum Target {
        CONTENT, TITLE, HELP;
    }

    Long uid;
    Locale lang;
    String content;
    Target target;
    Long inputElementUid;
    Long translationOfUId;

    public TranslationElement() {
        uid = null;
        lang = null;
        content = null;
        target = null;
        inputElementUid = null;
        translationOfUId = null;
    }

    public TranslationElement(long uid) {
        setUid(uid);
        lang = null;
        content = null;
        target = null;
        inputElementUid = null;
        translationOfUId = null;
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
     * Sets the language (locale) of this translation element
     * @param lang the locale
     */
    public void setLang(Locale lang) {
        this.lang = lang;
    }

    /**
     * Gets the language (locale) of this translation element
     * @return locale, or null if not set
     */
    public Locale getLang() {
        return lang;
    }

    /**
     * Sets the translation content of this translation element
     * @param content translation content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the translation content of this translation element
     * @return translation content, or null if not set
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the targeted part of the associated input element
     * @param target targeted part
     */
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * Gets the targeted part of the associated input element
     * @return targeted part, or null if not set
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Sets the input element uid where this translation element is associated to
     * @param inputElementUid input element uid
     */
    public void setInputElementUid(long inputElementUid) {
        this.inputElementUid = Long.valueOf(inputElementUid);
    }

    /**
     * Gets the input element uid where this translation element is associated to
     * @return input element uid, or 0L if not set
     */
    public long getInputElementUid() {
        if (inputElementUid != null) {
            return inputElementUid.longValue();
        }

        return 0L;
    }

    /**
     * Sets the uid of the element which is translated by this element
     * @param translationOfUId translation element uid of translated element
     */
    public void setTranslationOfUId(long translationOfUId) {
        this.translationOfUId = Long.valueOf(translationOfUId);
    }

    /**
     * Gets the uid of the element which is translated by this element
     * @return translation element uid of translated element, or 0L if not set
     */
    public long getTranslationOfUid() {
        if (translationOfUId != null) {
            return translationOfUId.longValue();
        }

        return 0L;
    }

    /**
     * Copies all member variables (except uid) to a new object
     * @param copy copy object
     */
    public void copyTo(TranslationElement copy) {
        copy.setLang(lang);
        copy.setContent(content);
        copy.setTarget(target);
        if (inputElementUid != null) {
            copy.setInputElementUid(inputElementUid);
        }
        if (translationOfUId != null) {
            copy.setTranslationOfUId(translationOfUId);
        }
    }

    @Override
    public int hashCode() {
        return uid != null ? this.getClass().hashCode() + uid.hashCode() : super.hashCode();
    }
}
