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
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class TagButtonRow extends LinearLayout {
	
	private static final String TAG = "TagButtonRow";
	
	private int holderWidth = 0;
	private int consumedWidth = 0;
	private ArrayList<String> labels = null;
	private final float scale = getResources().getDisplayMetrics().density;
	
	public TagButtonRow(Context ctx) {
		super(ctx);
		holderWidth = ((WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth()
					- (int)(20 * scale + 0.5f);
		this.setOrientation(LinearLayout.HORIZONTAL);
		labels = new ArrayList<String>();
	}
	
	public List<TagButton> addTagButtons(List<TagButton> btnList) {
		Iterator<TagButton> i = btnList.iterator();
		ArrayList<TagButton> returnList = new ArrayList<TagButton>();
		
		if (this.getChildCount() == 0) {
			while (i.hasNext()) {
				TagButton btn = i.next();
				if (hasSpace(btn)) {
					addTagButton(btn);
				}
				else {
					returnList.add(0, btn);
				}
			}
		}
		else {
			while (i.hasNext()) {
				List<TagButton> ret = addTagButton(i.next());
				if (ret != null) {
					returnList.addAll(0, ret);
				}
			}
			
			if (returnList.size() == 0) {
				returnList = null;
			}
		}
		
		return returnList;
	}
	
	public List<TagButton> addTagButton(TagButton child) {
		LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams)child.getLayoutParams());
		child.measure(0, 0);
		consumedWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
		
		int pos = Collections.binarySearch(labels, child.getText().toString());
		pos = -pos - 1;
		
		if (pos >= 0) {
			labels.add(pos, child.getText().toString());
			this.addView(child, pos);
			
			ArrayList<TagButton> removedBtns = new ArrayList<TagButton>();
			while (consumedWidth > holderWidth) {
				TagButton last = (TagButton)this.getChildAt(this.getChildCount() - 1);
				removedBtns.add(last);
				removeTagButton(last);
			}
			Collections.reverse(removedBtns);
			
			if (removedBtns.size() > 0) {
				return removedBtns;
			}
		}
		
		return null;
	}
	
	public void removeTagButton(TagButton child) {
		this.removeView(child);
		labels.remove(child.getText().toString());
		LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams)child.getLayoutParams());
		child.measure(0, 0);
		consumedWidth -= child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
	}
	
	public String getFirstLabel() {
		if (labels.size() > 0) {
			return labels.get(0);
		}
		
		return null;
	}
	
	public String getLastLabel() {
		if (labels.size() > 0) {
			return labels.get(labels.size() - 1);
		}
		
		return null;
	}
	
	public boolean hasSpace(TagButton btn) {
		LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams)btn.getLayoutParams());
		btn.measure(0, 0);
		int btnWidth = btn.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
		//Log.i(TAG, "hasSpace for '" + btn.getText() + "' btnWidth: " + btnWidth + " consumedWidth: "+ consumedWidth + " holderWidth: " + holderWidth);
		
		if ((btnWidth + consumedWidth) < holderWidth) {
			return true;
		}
		return false;
	}
}
