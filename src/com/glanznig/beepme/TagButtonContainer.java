package com.glanznig.beepme;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

public class TagButtonContainer extends LinearLayout {
	
	private static final String TAG = "beeper";
	private static final int MARGIN = 1;
	private static final int BTN_HEIGHT = 40;
	
	private final float scale = getResources().getDisplayMetrics().density;
	private int lastTagId = 0;

	public TagButtonContainer(Context ctx) {
		super(ctx);
		this.setOrientation(LinearLayout.VERTICAL);
	}
	
	public TagButtonContainer(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		this.setOrientation(LinearLayout.VERTICAL);
	}
	
	public int getLastTagId() {
		return lastTagId;
	}
	
	public void setLastTagId(int id) {
		lastTagId = id;
	}
	
	public void addTagButton(String name) {
		addTagButton(name, null);
	}
	
	public void addTagButton(String name, OnClickListener listener) {
		Button btn = new Button(this.getContext());
		lastTagId += 1;
		btn.setId(lastTagId);
		btn.setText(name);
		LinearLayout.LayoutParams btnLayout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int)(BTN_HEIGHT * scale + 0.5f));
		btnLayout.setMargins((int)(MARGIN * scale + 0.5f), 0, (int)(MARGIN * scale + 0.5f), 0);
		btn.setLayoutParams(btnLayout);
		if (listener != null) {
			btn.setOnClickListener(listener);
		}
		
		int numRows = this.getChildCount();
		
		if (numRows > 0) {
			List<Button> removedBtns = null;
			boolean inserted = false;
			for (int i = 0; i < numRows; i++) {
				TagButtonRow row = (TagButtonRow)this.getChildAt(i);
				
				if (removedBtns != null && removedBtns.size() > 0) {
					removedBtns = row.addTagButtons(removedBtns);
				}
				
				if (!inserted) {
					String first = row.getFirstLabel();
					String last = row.getLastLabel();
					
					//insert in between OR at beginning 
					if ((name.compareToIgnoreCase(first) > 0 && name.compareToIgnoreCase(last) < 0)
							|| name.compareToIgnoreCase(first) < 0) {
						removedBtns = row.addTagButton(btn);
						inserted = true;
					}
					
					// insert at the end (if enough space), otherwise add new row
					if ((name.compareToIgnoreCase(last) > 0 && this.getChildAt(i + 1) == null)) {
						if (row.hasSpace(btn)) {
							removedBtns = row.addTagButton(btn);
							inserted = true;
						}
						else {
							TagButtonRow newRow = new TagButtonRow(this.getContext());
							this.addView(newRow);
							newRow.addTagButton(btn);
							inserted = true;
						}
					}
				}
			}
			
			while (removedBtns != null && removedBtns.size() > 0) {
				TagButtonRow row = new TagButtonRow(this.getContext());
				this.addView(row);
				removedBtns = row.addTagButtons(removedBtns);
			}
		}
		else {
			TagButtonRow row = new TagButtonRow(this.getContext());
			this.addView(row);
			row.addTagButton(btn);
		}
	}
	
	public void removeTagButton(Button btn) {
		int numRows = this.getChildCount();
		String name = btn.getText().toString();
		boolean deleted = false;
		
		for (int i = 0; i < numRows; i++) {
			TagButtonRow row = (TagButtonRow)this.getChildAt(i);
			
			//button is located in this row
			if (!deleted && name.compareToIgnoreCase(row.getLastLabel()) <= 0) {
				row.removeTagButton(btn);
				deleted = true;
			}
			
			if (deleted) {
				if ((i - 1) >= 0) {
					TagButtonRow prevRow = (TagButtonRow)this.getChildAt(i - 1);
					Button b = (Button)row.getChildAt(0);
					while (prevRow.hasSpace(b)) {
						row.removeTagButton(b);
						prevRow.addTagButton(b);
						b = (Button)row.getChildAt(0);
					}
				}
				if (this.getChildAt(i + 1) != null) {
					TagButtonRow nextRow = (TagButtonRow)this.getChildAt(i + 1);
					Button b = (Button)nextRow.getChildAt(0);
					while (row.hasSpace(b)) {
						nextRow.removeTagButton(b);
						row.addTagButton(b);
						b = (Button)nextRow.getChildAt(0);
					}
					
					if (nextRow.getChildCount() == 0) {
						this.removeView(nextRow);
					}
				}
			}
		}
	}
}
