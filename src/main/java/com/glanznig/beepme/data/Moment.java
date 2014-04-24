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

import java.util.Date;

/**
 * A moment is a defined point in time (timestamp), where something happened, something was felt -
 * it had a quality. A moment is associated to a certain project. For sampling and life logging
 * projects it is associated to a timer uptime and has been either accepted or declined.
 * A moment has several values of different form, where the user added some data.
 */
public class Moment {

    private Long uid;
    private Date timestamp;
    private Boolean accepted;
    private Long uptimeUid;
    private Long projectUid;

    public Moment() {
        uid = null;
        timestamp = null;
        accepted = Boolean.FALSE;
        uptimeUid = null;
        projectUid = null;
    }

    public Moment(long uid) {
        setUid(uid);
        timestamp = null;
        accepted = Boolean.FALSE;
        uptimeUid = null;
        projectUid = null;
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
     * get when moment occurred
     * @return timestamp (unix time in milliseconds), or null if not set
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * set when moment occurred
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * get if moment was accepted (makes sense only in sampling and life logging projects)
     * @return true when moment was accepted, false otherwise
     */
    public boolean getAccepted() {
        return accepted.booleanValue();
    }

    /**
     * set if moment was accepted (makes sense only in sampling and life logging projects)
     * @param accepted true if accepted, false otherwise
     */
    public void setAccepted(boolean accepted) {
        this.accepted = new Boolean(accepted);
    }

    /**
     * set timer uptime uid (chunk of time where timer is active and beeps are possible)
     * @param uptimeUid uptime uid
     */
    public void setUptimeUid(long uptimeUid) {
        this.uptimeUid = Long.valueOf(uptimeUid);
    }

    /**
     * get timer uptime uid (chunk of time where timer is active and beeps are possible)
     * @return uptime uid or 0L if not set
     */
    public long getUptimeUid() {
        if (uptimeUid != null) {
            return uptimeUid.longValue();
        }

        return 0L;
    }

    /**
     * set project uid of project where moment belongs to
     * @param projectUid project uid
     */
    public void setProjectUid(long projectUid) {
        this.projectUid = Long.valueOf(projectUid);
    }

    /**
     * get project uid of project where moment belongs to
     * @return project uid or 0L if not set
     */
    public long getProjectUid() {
        if (projectUid != null) {
            return projectUid.longValue();
        }

        return 0L;
    }

    @Override
    public int hashCode() {
        return uid != null ? this.getClass().hashCode() + uid.hashCode() : super.hashCode();
    }

}