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

Copyright since 2012 Michael Glanznig
http://beepme.glanznig.com
*/

package com.glanznig.beepme.data;

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
        return name != null ? name.toLowerCase().hashCode() : super.hashCode();
    }
	
	public boolean equals(Object other) {
        return other instanceof Tag && (name != null) ? name.toLowerCase().equals(((Tag)other).getName().toLowerCase()) : (other == this);
    }

}
