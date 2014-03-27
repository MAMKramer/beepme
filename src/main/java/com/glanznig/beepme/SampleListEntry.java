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

package com.glanznig.beepme;

import java.util.Date;

import com.glanznig.beepme.data.Sample;

public class SampleListEntry extends ListItem {
	
	Sample content = null;
	
	public SampleListEntry(Sample s) {
		content = s;
	}
	
	public String getTitle() {
		if (content != null) {
			return content.getTitle();
		}
		
		return null;
	}
	
	public String getDescription() {
		if (content != null) {
			return content.getDescription();
		}
		
		return null;
	}
	
	public String getPhoto() {
		if (content != null) {
			return content.getPhotoUri();
		}
		
		return null;
	}
	
	public Date getTimestamp() {
		if (content != null) {
			return content.getTimestamp();
		}
		
		return null;
	}
	
	public Sample getSample() {
		return content;
	}

	@Override
	public boolean isSectionHeader() {
		return false;
	}

}
