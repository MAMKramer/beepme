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

package com.glanznig.beepme.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.glanznig.beepme.helper.FlowLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class TagButtonContainer extends FlowLayout {
	
	private static final String TAG = "TagViewContainer";
	
	private long vocabularyId = 0L;
	private ArrayList<String> tags = null;
	
	public TagButtonContainer(Context ctx) {
		super(ctx);
		tags = new ArrayList<String>();
	}

	public TagButtonContainer(Context ctx, long vocabularyId) {
		super(ctx);
		this.vocabularyId = vocabularyId;
		tags = new ArrayList<String>();
	}
	
	public TagButtonContainer(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		tags = new ArrayList<String>();
	}
	
	public void setVocabularyId(long vocabulary) {
		this.vocabularyId = vocabulary;
	}
	
	public long getVocabularyId() {
		return vocabularyId;
	}
	
	public void addTagButton(String name) {
		addTagButton(name, null);
	}
	
	public void addTagButton(String name, OnClickListener listener) {
		
		// each tag can only added once
		if (!tags.contains(name)) {
			TagButton button = null;
			
			button = new TagButton(vocabularyId, getContext());
			button.setText(name);
			
			if (listener != null) {
				button.setOnClickListener(listener);
			}
			
			
			//maintain sorting
			Comparator<String> compare = new Comparator<String>() {
		      public int compare(String s1, String s2) {
		        return s1.compareTo(s2);
		      }
		    };
			
			int pos = Collections.binarySearch(tags, name, compare);
			tags.add(-pos - 1, name);
			addView(button, -pos - 1);
		}
	}
	
	public void removeTagButton(TagButton button) {
		String name = button.getText().toString();
		tags.remove(name);
		removeView(button);
	}
}
