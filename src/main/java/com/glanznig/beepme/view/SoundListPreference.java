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

package com.glanznig.beepme.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

import com.glanznig.beepme.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundListPreference extends ListPreference {

    private static final String TAG = "SoundListPreference";

    private MediaPlayer mMediaPlayer;
    private CharSequence[] mEntries;
    private Integer[] mEntryValues;
    private int mClickedDialogEntryIndex;
    private String mValue;

    public SoundListPreference(Context ctx) {
        super(ctx);
    }

    public SoundListPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        mMediaPlayer = new MediaPlayer();

        if (mEntries != null && mEntryValues != null) {

            mClickedDialogEntryIndex = getValueIndex();
            builder.setSingleChoiceItems(mEntries, mClickedDialogEntryIndex,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mClickedDialogEntryIndex = which;

                            Integer value = mEntryValues[which];

                            try {
                                playSong(value.intValue());
                            } catch (Exception e) {
                                Log.e(TAG, "error while playing beep sound", e);
                            }

                        }
                    }
            );

            builder.setPositiveButton(R.string.done, this);
            builder.setNegativeButton(R.string.cancel, this);
        }
    }

    private void playSong(int resId) throws IllegalArgumentException,
            IllegalStateException, IOException {

        mMediaPlayer.reset();
        AssetFileDescriptor alarmSound = getContext().getResources().openRawResourceFd(resId);
        mMediaPlayer.setDataSource(alarmSound.getFileDescriptor(), alarmSound.getStartOffset(), alarmSound.getLength());

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && mClickedDialogEntryIndex >= 0 && mEntryValues != null) {
            String value = mEntryValues[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }

        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    @Override
    public void setValue(String value) {
        mValue = value;
        persistString(value);
    }

    @Override
    public void setValueIndex(int index) {
        if (mEntryValues != null) {
            setValue(mEntryValues[index].toString());
        }
    }

    @Override
    public String getValue() {
        return mValue;
    }

    @Override
    public CharSequence getEntry() {
        int index = getValueIndex();
        return index >= 0 && mEntries != null ? mEntries[index] : null;
    }

    @Override
    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(Integer.valueOf(value))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedString(mValue) : (String) defaultValue);
    }

    @Override
    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
    }

    public void setEntryValues(Integer[] entryValues) {
        mEntryValues = entryValues;
    }

    @Override
    public void setDefaultValue(Object defaultValue) {
        if( defaultValue instanceof String) {
            mValue = (String)defaultValue;
        }
    }
}
