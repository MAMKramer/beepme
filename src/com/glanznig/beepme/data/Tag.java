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
http://beepme.glanznig.com
*/

package com.glanznig.beepme.data;

import java.util.Locale;

public class Tag {
	
	private Long id;
	private String name;
	private Long vocabularyId;
	
	private static final String delimiter = "_#_";
	private static final String TAG = "Tag";
	
	public Tag() {
		id = null;
		name = null;
		vocabularyId = null;
	}
	
	public Tag(long id) {
		setId(id);
		name = null;
		vocabularyId = null;
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
	
	public void setVocabularyId(long vocabulary) {
		this.vocabularyId = Long.valueOf(vocabulary);
	}
	
	public long getVocabularyId() {
		long vid = 0L;
		if (vocabularyId != null) {
			vid = vocabularyId.longValue();
		}
		
		return vid;
	}
	
	public String toString() {
		if (vocabularyId != null) {
			return vocabularyId.toString() + delimiter + name;
		}
		else {
			return name;
		}
	}
	
	public static Tag valueOf(String tagStr) {
		Tag t = null;
		if (tagStr.contains(delimiter)) {
			t = new Tag();
			try {
				Long voc = Long.valueOf(tagStr.substring(0, tagStr.indexOf(delimiter)));
				t.setVocabularyId(voc.longValue());
				t.setName(tagStr.substring(tagStr.indexOf(delimiter) + delimiter.length()));
			}
			catch(Exception e) {
				t = null;
			}
		}
		
		return t;
	}
	
	public int hashCode() {
        return toString().hashCode();
    }
	
	public boolean equals(Object other) {
		boolean ret = false;
		if (other instanceof Tag) {
			Tag t = (Tag)other;
			if (name != null && name.toLowerCase(Locale.getDefault()).equals(t.getName().toLowerCase())) {
				ret = true;
				
				if (getVocabularyId() != 0L && t.getVocabularyId() != 0L) {
					if (getVocabularyId() == t.getVocabularyId()) {
						ret = true;
					}
					else {
						ret = false;
					}
				}
			}
		}
		else {
			if (other == this) {
				ret = true;
			}
		}
		
		return ret;
    }

}
