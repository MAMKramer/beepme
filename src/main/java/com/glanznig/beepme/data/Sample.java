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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Sample {
	
	private Long id;
	private Date timestamp;
	private String title;
	private String description;
	private Boolean accepted;
	private String photoUri;
	private ArrayList<Tag> tags;
	private Long uptimeId;
	
	public Sample() {
		id = null;
		timestamp = null;
		title = null;
		description = null;
		accepted = Boolean.FALSE;
		photoUri = null;
		tags = new ArrayList<Tag>();
		uptimeId = null;
	}
	
	public Sample(long id) {
		setId(id);
		timestamp = null;
		title = null;
		description = null;
		accepted = Boolean.FALSE;
		photoUri = null;
		tags = new ArrayList<Tag>();
		uptimeId = null;
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

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getAccepted() {
		return accepted;
	}

	public void setAccepted(Boolean accepted) {
		this.accepted = accepted;
	}
	
	public void setPhotoUri(String photoUri) {
		this.photoUri = photoUri;
	}
	
	public String getPhotoUri() {
		return photoUri;
	}
	
	public void setUptimeId(long uptimeId) {
		this.uptimeId = Long.valueOf(uptimeId);
	}
	
	public long getUptimeId() {
		long upId = 0L;
		if (uptimeId != null) {
			upId = uptimeId.longValue();
		}
		
		return upId;
	}
	
	public boolean addTag(Tag tag) {
		if (!tags.contains(tag)) {
			//maintain sorting
			Comparator<Tag> compare = new Comparator<Tag>() {
		      public int compare(Tag t1, Tag t2) {
		        return t1.getName().compareTo(t2.getName());
		      }
		    };
			
			int pos = Collections.binarySearch(tags, tag, compare);
			tags.add(-pos - 1, tag);
			
			return true;
		}
		
		return false;
	}
	
	public boolean removeTag(Tag tag) {
		if (tags.contains(tag)) {
			return tags.remove(tag);
		}
		
		return false;
	}
	
	public List<Tag> getTags() {
		return tags;
	}
	
	public int hashCode() {
        return id != null ? this.getClass().hashCode() + id.hashCode() : super.hashCode();
    }

}