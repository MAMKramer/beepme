package com.glanznig.beepme.view.input;

import com.glanznig.beepme.data.VocabularyItem;

/**
 * Interface to notify listeners of events related to tag changes (added, removed)
 */
public interface OnTagControlChangeListener {

    public void onTagAdded(VocabularyItem item);
    public void onTagRemoved(VocabularyItem item);
}
