package com.glanznig.beeper.data;

public class Tag {
	
	private Long id;
	private String name;
	
	public Tag() {
		id = null;
		name = null;
	}
	
	public Tag(long id) {
		setId(id);
		name = null;
	}
	
	private void setId(long id) {
		this.id = Long.valueOf(id);
	}
	
	public long getId() {
		return id.longValue();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	public int hashCode() {
        return name != null ? this.getClass().hashCode() + name.toLowerCase().hashCode() : super.hashCode();
    }
	
	public boolean equals(Object other) {
        return other instanceof Tag && (name != null) ? name.toLowerCase().equals(((Tag)other).getName().toLowerCase()) : (other == this);
    }

}
