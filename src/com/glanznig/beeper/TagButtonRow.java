package com.glanznig.beeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class TagButtonRow extends LinearLayout {
	
	private static final String TAG = "beeper";
	
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
	
	public List<Button> addTagButtons(List<Button> btnList) {
		Iterator<Button> i = btnList.iterator();
		ArrayList<Button> returnList = new ArrayList<Button>();
		
		if (this.getChildCount() == 0) {
			while (i.hasNext()) {
				Button btn = i.next();
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
				List<Button> ret = addTagButton(i.next());
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
	
	public List<Button> addTagButton(Button child) {
		LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams)child.getLayoutParams());
		child.measure(0, 0);
		consumedWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
		
		int pos = Collections.binarySearch(labels, child.getText().toString());
		pos = -pos - 1;
		
		if (pos >= 0) {
			labels.add(pos, child.getText().toString());
			this.addView(child, pos);
			
			ArrayList<Button> removedBtns = new ArrayList<Button>();
			while (consumedWidth > holderWidth) {
				Button last = (Button)this.getChildAt(this.getChildCount() - 1);
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
	
	public void removeTagButton(Button child) {
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
	
	public boolean hasSpace(Button btn) {
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
