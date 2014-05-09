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

import android.content.Context;

import java.util.Date;

import com.glanznig.beepme.data.InputElement;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.Project;
import com.glanznig.beepme.data.SingleValue;
import com.glanznig.beepme.data.Value;
import com.glanznig.beepme.data.db.InputElementTable;
import com.glanznig.beepme.data.db.ValueTable;

/**
 * Contains data of a list entry that is displayed in the list of moments.
 */
public class MomentListEntry extends ListItem {

    private Context ctx;
	private Moment content;
    private String title;
    private String description;
    private String photoUri;
	
	public MomentListEntry(Context ctx, Moment moment) {
        this.ctx = ctx.getApplicationContext();
		content = moment;

        Project project = ((BeepMeApp)ctx).getCurrentProject();
        InputElementTable inputElementTable = new InputElementTable(ctx);

        InputElement listTitle = inputElementTable.getInputElementByName(moment.getProjectUid(), project.getOption("listTitle"));
        if (listTitle != null) {
            Value value = new ValueTable(ctx).getValue(moment.getUid(), listTitle.getUid());
            if (value instanceof SingleValue) {
                String valueContent = ((SingleValue) value).getValue();
                if (valueContent != null && valueContent.length() > 0) {
                    title = valueContent;
                }
            }
        }

        if (project.hasOption("listSummary")) {
            InputElement listSummary = inputElementTable.getInputElementByName(moment.getProjectUid(), project.getOption("listSummary"));
            if (listSummary != null) {
                Value value = new ValueTable(ctx).getValue(moment.getUid(), listSummary.getUid());
                if (value instanceof SingleValue) {
                    String valueContent = ((SingleValue) value).getValue();
                    if (valueContent != null && valueContent.length() > 0) {
                        description = valueContent;
                    }
                }
            }
        }

        InputElement photo = inputElementTable.getPhotoInputElement(moment.getProjectUid());
        if (photo != null) {
            Value value = new ValueTable(ctx).getValue(moment.getUid(), photo.getUid());
            if (value instanceof SingleValue) {
                String valueContent = ((SingleValue) value).getValue();
                if (valueContent != null && valueContent.length() > 0) {
                    photoUri = valueContent;
                }
            }
        }
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getPhoto() {
		return photoUri;
	}
	
	public Date getTimestamp() {
		if (content != null) {
			return content.getTimestamp();
		}
		
		return null;
	}

    public long getMomentUid() {
        if (content != null) {
            return content.getUid();
        }

        return 0L;
    }

	@Override
	public boolean isSectionHeader() {
		return false;
	}

}
