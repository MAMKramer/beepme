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

import java.util.Calendar;
import java.util.Date;

/**
 * A beep is a scheduled alert (usually occurring in the future). It transforms into a moment if
 * it has been received by the user (and subsequently been accepted or declined). Before being
 * received it can also be cancelled if the timer is turned off or expire if some error occurs.
 */
public class Beep {

    private static String TAG = "Beep";

    public enum BeepStatus {
        CANCELLED, EXPIRED, ACTIVE, RECEIVED;
    }

    private Long uid;
    private Date timestamp;
    private Date created;
    private Date received;
    private Date updated;
    private BeepStatus status;
    private Long uptimeUid;

    public Beep() {
        uid = null;
        timestamp = null;
        created = null;
        received = null;
        updated = null;
        status = null;
        uptimeUid = null;
    }

    public Beep(long uid) {
        setUid(uid);
        timestamp = null;
        created = null;
        received = null;
        updated = null;
        status = null;
        uptimeUid = null;
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
     * Sets the timestamp (time when the beep shall occur)
     * @param timestamp time when the beep shall occur
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the timestamp (time when the beep shall occur)
     * @return time when the beep shall occur, or null if not set
     */
    public Date getTimestamp(){
        return timestamp;
    }

    /**
     * Sets the timestamp when this beep was created
     * @param created timestamp when beep was created
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Gets the timestamp when this beep was created
     * @return timestamp when beep was created, or null if not set
     */
    public Date getCreated(){
        return created;
    }

    /**
     * Sets the time when this beep was received by the user
     * @param received time when beep was received by the user
     */
    public void setReceived(Date received) {
        this.received = received;
    }

    /**
     * Gets the time when this beep was received by the user
     * @return time when beep was received by the user, or null if not set
     */
    public Date getReceived(){
        return received;
    }

    /**
     * Sets the time when this beep was last updated
     * @param updated time when beep was last updated
     */
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    /**
     * Gets the time when this beep was last updated
     * @return time when beep was last updated, or null if not set
     */
    public Date getUpdated(){
        return updated;
    }

    /**
     * Sets the status (CANCELLED, EXPIRED, ACTIVE, RECEIVED) of this beep
     * @param status status (CANCELLED, EXPIRED, ACTIVE, RECEIVED)
     */
    public void setStatus(BeepStatus status) {
        this.status = status;
    }

    /**
     * Gets the status (CANCELLED, EXPIRED, ACTIVE, RECEIVED) of this beep
     * @return status (CANCELLED, EXPIRED, ACTIVE, RECEIVED), or null if not set
     */
    public BeepStatus getStatus() {
        return status;
    }

    /**
     * set uptime uid of uptime where this beep belongs to
     * @param uptimeUid uptime uid
     */
    public void setUptimeUid(long uptimeUid) {
        this.uptimeUid = Long.valueOf(uptimeUid);
    }

    /**
     * get uptime uid of uptime where this beep belongs to
     * @return uptime uid or 0L if not set
     */
    public long getUptimeUid() {
        if (uptimeUid != null) {
            return uptimeUid.longValue();
        }

        return 0L;
    }

    /**
     * Copies all member variables (except uid) to a new object
     * @param copy copy object
     */
    public void copyTo(Beep copy) {
        copy.setTimestamp(timestamp);
        copy.setCreated(created);
        copy.setReceived(received);
        copy.setUpdated(updated);
        copy.setStatus(status);
        if (uptimeUid != null) {
            copy.setUptimeUid(uptimeUid);
        }
    }

    /**
     * Returns if beep is overdue (should have been received at least one minute ago)
     * @return true if overdue, false otherwise (also if unknown)
     */
    public boolean isOverdue() {
        if (timestamp != null) {
            if ((Calendar.getInstance().getTimeInMillis() - timestamp.getTime()) >= 60000) { //difference 1 min
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return uid != null ? this.getClass().hashCode() + uid.hashCode() : super.hashCode();
    }
}
