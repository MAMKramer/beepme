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

package com.glanznig.beepme.view.input;

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
import android.widget.TextView;

import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Restriction;
import com.glanznig.beepme.data.SingleValue;
import com.glanznig.beepme.data.Value;
import com.glanznig.beepme.helper.PhotoUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * A PhotoControl is a UI element that displays a photo and/or allows to take one.
 */
public class PhotoControl extends LinearLayout implements InputControl {
	
	private static final String TAG = "PhotoControl";

    private Context ctx;
    private Mode mode;
    private String name;
    private boolean mandatory;

    private ImageView photo;
    private View frameView;
    private FrameLayout frame;
    private ImageView photoTriangle;
    private PopupMenu popup;
    private TextView help;
    private TextView title;
    
    private int placeholderResId = 0;
    private int width;
    private int height;
    private boolean canDelete = true;
    private boolean canChange = true;
    private boolean hasPhotoSet = false;
    private String photoUri;

    public PhotoControl(Context context) {
        super(context);
        ctx = context;
        name = null;
        mode = Mode.CREATE;
        mandatory = false;
        
        setupView();
    }

    public PhotoControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        readStyleParameters(context, attrs);
        ctx = context;
        name = null;
        mode = Mode.CREATE;
        mandatory = false;

        setupView();
    }

    /**
     * Constructor
     * @param ctx the view context
     * @param mode the view mode
     * @param restrictions access restrictions for this photo control
     */
    public PhotoControl(Context ctx, Mode mode, Collection<Restriction> restrictions) {
        super(ctx);
        this.ctx = ctx.getApplicationContext();
        this.mode = mode;
        name = null;
        mandatory = false;

        if (restrictions != null) {
            Iterator<Restriction> restrictionIterator = restrictions.iterator();
            while (restrictionIterator.hasNext()) {
                Restriction restriction = restrictionIterator.next();
                if (restriction.getType().equals(Restriction.RestrictionType.EDIT) && restriction.getAllowed() == false) {
                    canChange = false;
                }
                if (restriction.getType().equals(Restriction.RestrictionType.DELETE) && restriction.getAllowed() == false) {
                    canDelete = false;
                }
            }
        }

        setupView();
    }
    
    private void readStyleParameters(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.PhotoControl);
        try {
            placeholderResId = a.getResourceId(R.styleable.PhotoControl_placeholder, 0);
            width = (int)a.getDimension(R.styleable.PhotoControl_imgWidth, 48);
            height = (int)a.getDimension(R.styleable.PhotoControl_imgHeight, 48);
        } finally {
            a.recycle();
        }
    }

    /**
     * Adds all the necessary sub-elements for the given view mode.
     */
    private void setupView() {
    	setOrientation(LinearLayout.HORIZONTAL);
    	setGravity(Gravity.BOTTOM);

        title = new TextView(ctx);
        addView(title);
    	
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

        if (mode.equals(Mode.CREATE) || (mode.equals(Mode.EDIT) && canChange)) {
            help = new TextView(ctx);
            addView(help);
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
            if (placeholderResId != 0) {
                photo.setImageResource(placeholderResId);
            }
            else {
                photo.setImageBitmap(null);
            }
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
    
    public boolean isPhotoSet() {
    	return hasPhotoSet;
    }
    
    public void setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener listener) {
    	popup.setOnMenuItemClickListener(listener);
    }

    @Override
    public void setValue(Value value) {
        if (value instanceof SingleValue) {
            SingleValue singleValue = (SingleValue)value;
            photoUri = singleValue.getValue();

            // todo set photo
        }
    }

    @Override
    public Value getValue() {
        SingleValue value = new SingleValue();
        value.setValue(photoUri);

        return value;
    }

    @Override
    public void setHelpText(String help) {
        this.help.setText(help);
    }

    @Override
    public void setTitle(String title) {
        this.title.setText(title);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public boolean getMandatory() {
        return mandatory;
    }
}