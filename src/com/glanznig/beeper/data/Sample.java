package com.glanznig.beeper.data;

import java.util.Date;

public class Sample {
	
	private long id;
	private Date timestamp;
	private String title;
	private String description;
	private Boolean accepted;
	private String photoUri;
	
	public Sample() {
		id = 0L;
		timestamp = null;
		title = null;
		description = null;
		accepted = Boolean.FALSE;
		photoUri = null;
	}
	
	public Sample(long id) {
		setId(id);
		timestamp = null;
		title = null;
		description = null;
		accepted = Boolean.FALSE;
		photoUri = null;
	}
	
	public long getId() {
		return id;
	}
	
	private void setId(long id) {
		this.id = id;
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

}