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
 * An uptime is a period of time when the timer is active (exists for sampling and life logging
 * projects only). It has an start and end timestamp and a reference to a project. The most recent
 * uptime does not have the end timestamp set.
 */
public class Uptime {
	
	private Long uid;
	private Date start;
	private Date end;
	private Long projectUid;
	
	public Uptime() {
		uid = null;
		start = null;
		end = null;
		projectUid = null;
	}
	
	public Uptime(long uid) {
		setUid(uid);
		start = null;
		end = null;
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
     * Get start time
     * @return start time object
     */
	public Date getStart() {
		return start;
	}

    /**
     * Set start time
     * @param start start time object
     */
	public void setStart(Date start) {
		this.start = start;
	}

    /**
     * Set end time
     * @param end end time object
     */
	public void setEnd(Date end) {
		this.end = end;
	}

    /**
     * Get end time (is null with most recent uptime)
     * @return end time object
     */
	public Date getEnd() {
		return end;
	}

    /**
     * get project uid of project where moment belongs to
     * @return project uid or 0L if not set
     */
	public long getProjectUid() {
		return projectUid.longValue();
	}

    /**
     * set project uid of project where uptime belongs to
     * @param projectUid project uid
     */
	public void setProjectUid(long projectUid) {
		this.projectUid = Long.valueOf(projectUid);
	}

    /**
     * Copies all member variables (except uid) to a new object
     * @param copy copy object
     */
    public void copyTo(Uptime copy) {
        copy.setStart(start);
        copy.setEnd(end);
        if (projectUid != null) {
            copy.setProjectUid(projectUid);
        }
    }

    @Override
	public int hashCode() {
        return uid != null ? this.getClass().hashCode() + uid.hashCode() : super.hashCode();
    }
}
