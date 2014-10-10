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

import android.content.Context;

import com.glanznig.beepme.BeepMeApp;

/**
 * A timer represents a form of automatically generating alarms. This can take several forms e.g.
 * randomly or within specified intervals. The main function here is to schedule a new beep with
 * some sort of timer strategy.
 */
public abstract class Timer {

    private int soundResId;
    private Context ctx;

    public Timer(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        soundResId = 0;
    }

    /**
     * Sets the sound resource of the beep by id
     * @param resourceId sound resource id
     */
    public void setSound(int resourceId) {
        soundResId = resourceId;
    }

    /**
     * Sets the sound resource of the beep by resource name
     * @param resource resource name
     */
    public void setSound(String resource) {
        BeepMeApp app = (BeepMeApp)ctx.getApplicationContext();
        soundResId = ctx.getResources().getIdentifier(resource, "raw", app.getPackageName());
    }

    /**
     * Gets the sound resource id of the beep
     * @return sound resource id, or 0 if not set
     */
    public int getSound() {
        return soundResId;
    }

    /**
     * Gets the application context (for resource conversions)
     * @return application context
     */
    protected Context getContext() {
        return ctx;
    }

    /**
     * Gets the next scheduled beep (positive offset from now in milliseconds) according to timer
     * strategy. Has to be implemented by specific subclass.
     * @return next scheduled beep (positive offset from now in milliseconds)
     */
    public abstract long getNext();

    /**
     * Transforms the Timer object into a string representation (for serialization, persistance).
     * Has to be implemented by specific subclass.
     * @return string representation of Timer object
     */
    @Override
    public abstract String toString();
}
