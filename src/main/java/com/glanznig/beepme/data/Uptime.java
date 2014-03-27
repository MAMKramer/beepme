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

public class Uptime {
	
	private Long id;
	private Date start;
	private Date end;
	private Long timerProfileId;
	
	public Uptime() {
		id = null;
		start = null;
		end = null;
		timerProfileId = null;
	}
	
	public Uptime(long id) {
		setId(id);
		start = null;
		end = null;
		timerProfileId = null;
	}
	
	public long getId() {
		if (id != null) {
			return id.longValue();
		}
		else {
			return 0L;
		}
	}
	
	private void setId(long id) {
		this.id = Long.valueOf(id);
	}
	
	public Date getStart() {
		return start;
	}
	
	public void setStart(Date start) {
		this.start = start;
	}
	
	public void setEnd(Date end) {
		this.end = end;
	}
	
	public Date getEnd() {
		return end;
	}
	
	public long getTimerProfileId() {
		return timerProfileId.longValue();
	}
	
	public void setTimerProfileId(long id) {
		this.timerProfileId = Long.valueOf(id);
	}
	
	public int hashCode() {
        return id != null ? this.getClass().hashCode() + id.hashCode() : super.hashCode();
    }
}
