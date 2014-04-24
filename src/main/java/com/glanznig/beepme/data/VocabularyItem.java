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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * A vocabulary item represents an item of choice. This can be related to single or multiple
 * choice selections or to tags. Vocabulary items can be pre- or user-defined and can be
 * translated. They belong to a vocabulary (group of choice items).
 */
public class VocabularyItem {

    private static final String delimiter = "_#_";
    private static final String TAG = "VocabularyItem";

    private Long uid;
    private String name;
    private Locale lang;
    private String value;
    private Boolean predefined;
    private Long translationOfUid;
    private Long vocabularyUid;

    private HashMap<String, VocabularyItem> translations;

    public VocabularyItem() {
        uid = null;
        name = null;
        lang = null;
        value = null;
        predefined = Boolean.FALSE;
        translationOfUid = null;
        vocabularyUid = null;

        translations = null;
    }

    public VocabularyItem(long uid) {
        setUid(uid);
        name = null;
        lang = null;
        value = null;
        predefined = Boolean.FALSE;
        translationOfUid = null;
        vocabularyUid = null;

        translations = null;
    }

    public VocabularyItem(boolean predefined) {
        uid = null;
        name = null;
        lang = null;
        value = null;
        this.predefined = new Boolean(predefined);
        translationOfUid = null;
        vocabularyUid = null;

        translations = null;
    }

    public VocabularyItem(long uid, boolean predefined) {
        setUid(uid);
        name = null;
        lang = null;
        value = null;
        this.predefined = new Boolean(predefined);
        translationOfUid = null;
        vocabularyUid = null;

        translations = null;
    }

    /**
     * set unique identifier
     * @param uid uid (primary key)
     */
    private void setUid(long uid) {
        this.uid = Long.valueOf(uid);
    }

    /**
     * get unique identifier
     * @return uid (primary key)
     */
    public long getUid() {
        if (uid != null) {
            return uid.longValue();
        }
        return 0L;
    }

    /**
     * Sets the name (string id) of this vocabulary item
     * @param name name (string id)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name (string id) of this vocabulary item
     * @return name (string id), or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the language of this vocabulary item (for translations)
     * @param lang locale of language
     */
    public void setLanguage(Locale lang) {
        this.lang = lang;
    }

    /**
     * Gets the language of this vocabulary item (for translations)
     * @return locale of language, or null if not set
     */
    public Locale getLanguage() {
        return lang;
    }

    /**
     * Sets the value of this vocabulary item
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of this vocabulary item
     * @return the value, or null if not set
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets whether this vocabulary item was predefined in the project XML.
     * Predefined items should never be deleted, even if they are no longer associated to any values.
     * @param predefined true if predefined, false otherwise
     */
    public void setPredefined(boolean predefined) {
        this.predefined = predefined;
    }

    /**
     * Gets whether this vocabulary item was predefined in the project XML.
     * Predefined items should never be deleted, even if they are no longer associated to any values.
     * @return true if predefined, false otherwise
     */
    public boolean isPredefined() {
        return predefined.booleanValue();
    }

    /**
     * Sets the uid of the vocabulary item this item translates
     * @param uid uid of translated vocabulary item
     */
    public void setTranslationOfUid(long uid) {
        this.translationOfUid = Long.valueOf(uid);
    }

    /**
     * Gets the uid of the vocabulary item this item translates
     * @return uid of translated vocabulary item, or 0L if not set
     */
    public long getTranslationOfUid() {
        if (translationOfUid != null) {
            return translationOfUid.longValue();
        }

        return 0L;
    }

    /**
     * Sets the vocabulary uid where this item belongs to
     * @param vocabularyUid vocabulary uid of parent vocabulary
     */
    public void setVocabularyUid(long vocabularyUid) {
        this.vocabularyUid = Long.valueOf(vocabularyUid);
    }

    /**
     * Gets the vocabulary uid where this item belongs to
     * @return vocabulary uid of parent vocabulary, or 0L if not set
     */
    public long getVocabularyUid() {
        if (vocabularyUid != null) {
            return vocabularyUid.longValue();
        }

        return 0L;
    }

    /**
     * Sets a translation for this vocabulary item
     * @param item vocabulary item that translates this item
     */
    public void setTranslation(VocabularyItem item) {
        if (translations == null) {
            translations = new HashMap<String, VocabularyItem>();
        }
        translations.put(item.getLanguage().getLanguage(), item);
    }

    /**
     * Gets the translation of this vocabulary item for the specified language.
     * @param language language code of translation
     * @return vocabulary item that translates this item, or null if no translation
     */
    public VocabularyItem getTranslation(String language) {
        if (translations != null) {
            if (translations.containsKey(language)) {
                return translations.get(language);
            }
        }
        return null;
    }

    /**
     * Gets the translations of this vocabulary item.
     * @return collection of vocabulary items that translate this item, or empty collection if no translation
     */
    public Collection<VocabularyItem> getTranslations() {
        if (translations == null) {
            translations = new HashMap<String, VocabularyItem>();
        }
        return translations.values();
    }

    /**
     * Copies all member variables (except uid) to a new object
     * @param copy copy object
     */
    public void copyTo(VocabularyItem copy) {
        copy.setName(name);
        copy.setLanguage(lang);
        copy.setValue(value);
        copy.setPredefined(predefined);
        if (translationOfUid != null) {
            copy.setTranslationOfUid(translationOfUid);
        }
        if (vocabularyUid != null) {
            copy.setVocabularyUid(vocabularyUid);
        }

        if (translations != null) {
            Iterator<VocabularyItem> itemIterator = translations.values().iterator();
            while (itemIterator.hasNext()) {
                copy.setTranslation(itemIterator.next());
            }
        }
    }

    @Override
    public String toString() {
        String representation = "";

        if (vocabularyUid != null) {
            representation += vocabularyUid.toString() + delimiter;
        }
        if (name != null) {
            representation += name + delimiter;
        }
        if (value != null) {
            representation += value;
        }

        return representation;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /*@Override
	public boolean equals(Object other) {
		boolean ret = false;
		if (other instanceof VocabularyItem) {
			VocabularyItem t = (VocabularyItem)other;
			if (name != null && name.toLowerCase(Locale.getDefault()).equals(t.getName().toLowerCase())) {
				ret = true;
				
				if (getVocabularyUid() != 0L && t.getVocabularyUid() != 0L) {
					if (getVocabularyUid() == t.getVocabularyUid()) {
						ret = true;
					}
					else {
						ret = false;
					}
				}
			}
		}
		else {
			if (other == this) {
				ret = true;
			}
		}
		
		return ret;
    }

    public static VocabularyItem valueOf(String tagStr) {
        VocabularyItem t = null;
        if (tagStr.contains(delimiter)) {
            t = new VocabularyItem();
            try {
                Long voc = Long.valueOf(tagStr.substring(0, tagStr.indexOf(delimiter)));
                t.setVocabularyUid(voc.longValue());
                t.setName(tagStr.substring(tagStr.indexOf(delimiter) + delimiter.length()));
            }
            catch(Exception e) {
                t = null;
            }
        }

        return t;
    }*/

}
