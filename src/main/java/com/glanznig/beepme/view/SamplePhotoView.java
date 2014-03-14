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

package com.glanznig.beepme.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.glanznig.beepme.R;
import com.glanznig.beepme.helper.PhotoUtils;

public class SamplePhotoView extends LinearLayout {
	
	private static final String TAG = "SamplePhotoView";

    private ImageView photo;
    private View frameView;
    private FrameLayout frame;
    private ImageView photoTriangle;
    private PopupMenu popup;
    private Context ctx;
    
    private int placeholderResId = 0;
    private int width;
    private int height;
    private boolean canDelete = true;
    private boolean canChange = true;
    private boolean hasPhotoSet = false;

    public SamplePhotoView(Context context) {
        super(context);
        ctx = context;
        
        setupComponents();
    }

    public SamplePhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readStyleParameters(context, attrs);
        ctx = context;
        
        setupComponents();
    }
    
    private void readStyleParameters(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.SamplePhotoView);
        try {
            placeholderResId = a.getResourceId(R.styleable.SamplePhotoView_placeholder, 0);
            width = (int)a.getDimension(R.styleable.SamplePhotoView_imgWidth, 48);
            height = (int)a.getDimension(R.styleable.SamplePhotoView_imgHeight, 48);
        } finally {
            a.recycle();
        }
    }
    
    private void setupComponents() {
    	setOrientation(LinearLayout.HORIZONTAL);
    	setGravity(Gravity.BOTTOM);
    	
    	frame = new FrameLayout(ctx);
    	LayoutParams params = new LayoutParams(width, height);
    	frame.setLayoutParams(params);
    	addView(frame);
    	
    	photo = new ImageView(ctx, null, R.style.SamplePhotoView_Photo);
    	params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.START);
    	photo.setLayoutParams(params);
    	photo.setImageResource(placeholderResId);
    	photo.setScaleType(ScaleType.CENTER_CROP);
    	frame.addView(photo);
    	
    	int[] attrs = new int[] { android.R.attr.selectableItemBackground };
    	TypedArray a = ctx.getTheme().obtainStyledAttributes(attrs);
    	Drawable frameBg = a.getDrawable(0);
    	a.recycle();
    	
    	frameView = new View(ctx);
    	params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    	frameView.setLayoutParams(params);
    	frameView.setBackgroundDrawable(frameBg);
    	frame.addView(frameView);
    	
    	popup = new PopupMenu(ctx, frameView);
	    popup.inflate(R.menu.photoview);
	    
	    if (!canChange) {
	    	popup.getMenu().findItem(R.id.action_take_photo).setVisible(false);
	    	popup.getMenu().findItem(R.id.action_change_photo).setVisible(false);
	    }
	    
	    if (!canDelete) {
	    	popup.getMenu().findItem(R.id.action_delete_photo).setVisible(false);
	    }
    	
    	frameView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	popup.show();
            }
        });
    	
    	photoTriangle = new ImageView(ctx);
    	params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	photoTriangle.setLayoutParams(params);
    	photoTriangle.setBackgroundResource(R.drawable.ic_spinner);
    	addView(photoTriangle);
    	
    	if (!canChange && !canDelete) { // == read only
    		frameView.setClickable(false);
    		frameView.setFocusable(false);
    		photoTriangle.setVisibility(View.GONE);
    	}
    	else {
    		if (hasPhotoSet) {
				frameView.setClickable(true);
	        	frameView.setFocusable(true);
	        	photoTriangle.setVisibility(View.VISIBLE);
	        	
	        	popup.getMenu().findItem(R.id.action_take_photo).setVisible(false);
	        	if (!canChange) {
    		    	popup.getMenu().findItem(R.id.action_change_photo).setVisible(false);
	        	}
	        	else {
	        		popup.getMenu().findItem(R.id.action_change_photo).setVisible(true);
	        	}
	        	
	        	if (!canDelete) {
	        		popup.getMenu().findItem(R.id.action_delete_photo).setVisible(false);
	        	}
	        	else {
	        		popup.getMenu().findItem(R.id.action_delete_photo).setVisible(true);
	        	}
    		}
    		else {
    			// no photo - delete and change is senseless
    			popup.getMenu().findItem(R.id.action_delete_photo).setVisible(false);
    			popup.getMenu().findItem(R.id.action_change_photo).setVisible(false);
    			
    			if (canChange) {
    				frameView.setClickable(true);
    	        	frameView.setFocusable(true);
    	        	photoTriangle.setVisibility(View.VISIBLE);
    	        	
    	        	popup.getMenu().findItem(R.id.action_take_photo).setVisible(true);
    			}
    			else {
    				frameView.setClickable(false);
    	    		frameView.setFocusable(false);
    	    		photoTriangle.setVisibility(View.GONE);
    			}
    		}
    	}
    }
    
    public void setPhoto(Bitmap photoBitmap) {
    	if (photoBitmap != null) {
    		photo.setImageBitmap(photoBitmap);
    		hasPhotoSet = true;
    		updateAppearance();
    	}
    }
    
    public void setPhoto(String uri) {
    	if (uri != null) {
    		setPhoto(PhotoUtils.getBitmap(ctx, uri));
    	}
    }
    
    public void unsetPhoto() {
    	if (hasPhotoSet) {
    		hasPhotoSet = false;
    		photo.setImageResource(placeholderResId);
    		updateAppearance();
    	}
    }
    
    public void setFrameDimensions(int width, int height) {
    	if ((width > 0 || width == LayoutParams.MATCH_PARENT) && (height > 0 || height == LayoutParams.MATCH_PARENT)) {
    		this.width = width;
    		this.height = height;
	    	LayoutParams params = new LayoutParams(width, height);
	    	frame.setLayoutParams(params);
	    	updateAppearance();
    	}
    }
    
    public void setFrameWidth(int width) {
    	if (width > 0 || width == LayoutParams.MATCH_PARENT) {
	    	this.width = width;
	    	LayoutParams params = new LayoutParams(width, height);
	    	frame.setLayoutParams(params);
	    	updateAppearance();
    	}
    }
    
    public void setFrameHeight(int height) {
    	if (height > 0 || height == LayoutParams.MATCH_PARENT) {
	    	this.height = height;
	    	LayoutParams params = new LayoutParams(width, height);
	    	frame.setLayoutParams(params);
	    	updateAppearance();
    	}
    }
    
    private void updateAppearance() {
    	if (!canChange && !canDelete) { // == read only
    		frameView.setClickable(false);
    		frameView.setFocusable(false);
    		photoTriangle.setVisibility(View.GONE);
    	}
    	else {
    		if (hasPhotoSet) {
				frameView.setClickable(true);
	        	frameView.setFocusable(true);
	        	photoTriangle.setVisibility(View.VISIBLE);
	        	
	        	popup.getMenu().findItem(R.id.action_take_photo).setVisible(false);
	        	if (!canChange) {
    		    	popup.getMenu().findItem(R.id.action_change_photo).setVisible(false);
	        	}
	        	else {
	        		popup.getMenu().findItem(R.id.action_change_photo).setVisible(true);
	        	}
	        	
	        	if (!canDelete) {
	        		popup.getMenu().findItem(R.id.action_delete_photo).setVisible(false);
	        	}
	        	else {
	        		popup.getMenu().findItem(R.id.action_delete_photo).setVisible(true);
	        	}
    		}
    		else {
    			// no photo - delete and change is senseless
    			popup.getMenu().findItem(R.id.action_delete_photo).setVisible(false);
    			popup.getMenu().findItem(R.id.action_change_photo).setVisible(false);
    			
    			if (canChange) {
    				frameView.setClickable(true);
    	        	frameView.setFocusable(true);
    	        	photoTriangle.setVisibility(View.VISIBLE);
    	        	
    	        	popup.getMenu().findItem(R.id.action_take_photo).setVisible(true);
    			}
    			else {
    				frameView.setClickable(false);
    	    		frameView.setFocusable(false);
    	    		photoTriangle.setVisibility(View.GONE);
    			}
    		}
    	}
    	
    	invalidate();
    }
    
    public void setRights(boolean canChange, boolean canDelete) {
    	this.canChange = canChange;
    	this.canDelete = canDelete;
    	updateAppearance();
    }
    
    public void setCanChange(boolean canChange) {
    	this.canChange = canChange;
    	updateAppearance();
    }
    
    public void setCanDelete(boolean canDelete) {
    	this.canDelete = canDelete;
    	updateAppearance();
    }
    
    public boolean canChange() {
    	return canChange;
    }
    
    public boolean canDelete() {
    	return canDelete;
    }
    
    public boolean isReadOnly() {
    	if (!canChange && !canDelete) {
    		return true;
    	}
    	return false;
    }
    
    public boolean isPhotoSet() {
    	return hasPhotoSet;
    }
    
    public void setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener listener) {
    	popup.setOnMenuItemClickListener(listener);
    }
}