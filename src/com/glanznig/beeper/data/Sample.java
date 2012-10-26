package com.glanznig.beeper.data;

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
	
	public Sample() {
		id = null;
		timestamp = null;
		title = null;
		description = null;
		accepted = Boolean.FALSE;
		photoUri = null;
		tags = new ArrayList<Tag>();
	}
	
	public Sample(long id) {
		setId(id);
		timestamp = null;
		title = null;
		description = null;
		accepted = Boolean.FALSE;
		photoUri = null;
		tags = new ArrayList<Tag>();
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