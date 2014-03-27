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

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateListSectionHeader extends ListItem {
	
	Date content = null;
	
	public DateListSectionHeader(Date date) {
		content = date;
	}
	
	public Date getDate() {
		return content;
	}
	
	public boolean isSameDay(Date d) {
		if (content != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			return format.format(d).equals(format.format(content));
		}
		
		return false;
	}

	@Override
	public boolean isSectionHeader() {
		return true;
	}

}
