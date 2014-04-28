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
     * Gets the next scheduled beep according to timer strategy. Has to be implemented
     * by specific subclass.
     * @return next scheduled beep
     */
    public abstract Beep getNext();

    /**
     * Transforms the Timer object into a string representation (for serialization, persistance).
     * Has to be implemented by specific subclass.
     * @return string representation of Timer object
     */
    @Override
    public abstract String toString();
}
