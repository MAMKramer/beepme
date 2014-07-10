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

package com.glanznig.beepme.view;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.MultiValue;
import com.glanznig.beepme.data.SingleValue;
import com.glanznig.beepme.data.Value;
import com.glanznig.beepme.data.db.ValueTable;
import com.glanznig.beepme.view.input.InputControl;
import com.glanznig.beepme.view.input.PhotoControl;
import com.glanznig.beepme.data.Moment;
import com.glanznig.beepme.data.VocabularyItem;
import com.glanznig.beepme.data.db.MomentTable;
import com.glanznig.beepme.helper.AsyncImageScaler;
import com.glanznig.beepme.helper.PhotoUtils;
import com.glanznig.beepme.view.input.TagControl;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

public class ChangeMomentActivity extends Activity implements PopupMenu.OnMenuItemClickListener, Callback {
	
	private static final String TAG = "ChangeMomentActivity";
	
	private Moment moment;
    private ViewManager viewManager;
    private InputControl.Mode mode;

	private PhotoControl photoView = null;
	
	private static class ImgLoadHandler extends Handler {
		WeakReference<PhotoControl> view;
		
		ImgLoadHandler(PhotoControl view) {
			this.view = new WeakReference<PhotoControl>(view);
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == PhotoUtils.MSG_PHOTO_LOADED) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		
	    		if (view.get() != null && imageBitmap != null) {
	    			view.get().setPhoto(imageBitmap);
	    		}
	    	}
	    }
	}
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        View customActionBarView = null;
        BeepMeApp app = (BeepMeApp)getApplication();
        viewManager = new ViewManager(ChangeMomentActivity.this, app.getCurrentProject());
        MomentTable momentTable = new MomentTable(this.getApplicationContext());
        Bundle intentExtras = getIntent().getExtras();
        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        if (savedState != null) {
            if (savedState.containsKey("ChangeMomentActivity_mode")) {
                switch (savedState.getInt("ChangeMomentActivity_mode")) {
                    case 1:
                        mode = InputControl.Mode.CREATE;
                        break;
                    case 2:
                        mode = InputControl.Mode.EDIT;
                        break;
                }
                savedState.remove("ChangeMomentActivity_mode");
            }
        }
        else {
            if (intentExtras != null) {
                // we are in create mode
                if (intentExtras.containsKey(getApplication().getClass().getPackage().getName() + ".Timestamp")) {
                    mode = InputControl.Mode.CREATE;
                }
                // we are in edit mode
                else if (intentExtras.containsKey(getApplication().getClass().getPackage().getName() + ".MomentUid")) {
                    mode = InputControl.Mode.EDIT;
                }
            }
        }

        if (mode == InputControl.Mode.CREATE) {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_done, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickDone(v);
                        }
                    }
            );
        }
        else if (mode == InputControl.Mode.EDIT) {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_done_cancel, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickDone(v);
                        }
                    });
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickCancel(v);
                        }
                    });
        }

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView);

        //todo add title and timestamp to view manager
        if (mode == InputControl.Mode.CREATE) {
            setContentView(viewManager.getLayout(InputControl.Mode.CREATE));
            setTitle(R.string.new_sample);
        }
        else if (mode == InputControl.Mode.EDIT) {
            setContentView(viewManager.getLayout(InputControl.Mode.EDIT));
            //todo title?
        }
		
		if (savedState != null) {
            if (savedState.getLong("ChangeMomentActivity_momentId") != 0L) {
                moment = momentTable.getMomentWithValues(savedState.getLong("ChangeMomentActivity_momentId"));
                savedState.remove("ChangeMomentActivity_momentId");
            }

            viewManager.deserializeValues(savedState);
		}
		else {
			if (intentExtras != null) {
                // we are in create mode
				if (intentExtras.containsKey(getApplication().getClass().getPackage().getName() + ".Timestamp")) {
					long timestamp = intentExtras.getLong(getApplication().getClass().getPackage().getName() + ".Timestamp");
                    moment = new Moment();
                    moment.setProjectUid(app.getPreferences().getProjectId());
					moment.setTimestamp(new Date(timestamp));
					moment.setAccepted(true);
                    moment.setUptimeUid(app.getCurrentUptime().getUid());

					moment = momentTable.addMoment(moment);
				}
                // we are in edit mode
                else if (intentExtras.containsKey(getApplication().getClass().getPackage().getName() + ".MomentUid")) {
                    long momentUid = intentExtras.getLong(getApplication().getClass().getPackage().getName() + ".MomentUid");
                    moment = momentTable.getMomentWithValues(momentUid);

                    ValueTable valueTable = new ValueTable(this.getApplicationContext());
                    Iterator<Value> valueIterator = valueTable.getValues(moment.getUid()).iterator();

                    while (valueIterator.hasNext()) {
                        Value value = valueIterator.next();

                        InputControl inputControl = viewManager.getInputControl(value.getInputElementName());
                        inputControl.setValue(value);
                    }
                }
			}
		}
	}
	
	/*@Override
	public void onResume() {
		super.onResume();
		//populateFields();
	}

    private void populateFields() {
		photoView = (PhotoControl)findViewById(R.id.new_sample_photoview);
		//check if device has camera feature
		if (!PhotoUtils.isEnabled(ChangeMomentActivity.this)) {
			photoView.setVisibility(View.GONE);
		}
		else {
			photoView.setVisibility(View.VISIBLE);
			photoView.setOnMenuItemClickListener(ChangeMomentActivity.this);
			
			String thumbnailUri = PhotoUtils.getThumbnailUri(moment.getPhotoUri(), 48);
			if (thumbnailUri != null) {
				File thumb = new File(thumbnailUri);
				if (thumb.exists()) {
					ImgLoadHandler handler = new ImgLoadHandler(photoView);
					PhotoUtils.getAsyncBitmap(ChangeMomentActivity.this, thumbnailUri, handler);
				}
			}
		}
        
        if (moment.getTimestamp() != null) {
        	TextView timestamp = (TextView)findViewById(R.id.new_sample_timestamp);
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			timestamp.setText(dateFormat.format(moment.getTimestamp()));
        }
		
        if (moment.getTitle() != null) {
        	EditText titleWidget = (EditText)findViewById(R.id.new_sample_title);
        	titleWidget.setText(moment.getTitle());
        }
		
        if (moment.getDescription() != null) {
        	EditText descriptionWidget = (EditText)findViewById(R.id.new_sample_description);
        	descriptionWidget.setText(moment.getDescription());
        }
        
        AutoCompleteTextView autocompleteTags = (AutoCompleteTextView)findViewById(R.id.new_sample_add_keyword);
        TagAutocompleteAdapter adapterKeywords = new TagAutocompleteAdapter(ChangeMomentActivity.this, R.layout.tag_autocomplete_list_row, 1);
    	autocompleteTags.setAdapter(adapterKeywords);
    	//after how many chars should auto-complete list appear?
    	autocompleteTags.setThreshold(2);
    	
    	TagControl keywordHolder = (TagControl)findViewById(R.id.new_sample_keyword_container);
    	keywordHolder.setVocabularyUid(1);
    	Iterator<VocabularyItem> i = moment.getTags().iterator();
		VocabularyItem tag = null;
		
		while (i.hasNext()) {
			tag = i.next();
			if (tag.getVocabularyUid() == 1) {
				keywordHolder.addTagButton(tag.getName(), this);
			}
		}
	}*/
	
	public void onClickDone(View view) {
        if (mode == InputControl.Mode.EDIT) {
            saveMoment();
        }

        if (mode == InputControl.Mode.CREATE) {
            BeepMeApp app = (BeepMeApp)getApplication();
            app.scheduleBeep();
        }

		finish();
	}

    public void onClickCancel(View view) {
        finish();
    }
	
	public void saveMoment() {
        ValueTable valueTable = new ValueTable(this.getApplicationContext());
        Iterator<InputControl> controlIterator = viewManager.getInputControls().iterator();
        while (controlIterator.hasNext()) {
            InputControl control = controlIterator.next();
            Value momentValue = moment.getValue(control.getName());
            Value controlValue = control.getValue();

            if (momentValue != null) {
                // value of moment needs to be updated - if there have been changes
                if (momentValue instanceof SingleValue) {
                    if (!((SingleValue)momentValue).getValue().equals(((SingleValue)controlValue).getValue())) {
                        ((SingleValue)momentValue).setValue(((SingleValue)controlValue).getValue());
                        valueTable.updateValue(momentValue);
                    }
                }
                else if (momentValue instanceof MultiValue) {
                    ((MultiValue)momentValue).resetValue(); // too much work to check for changes, reset value
                    Iterator<VocabularyItem> valueIterator = ((MultiValue)controlValue).getValues().iterator();
                    while (valueIterator.hasNext()) {
                        VocabularyItem vocabularyItem = valueIterator.next();
                        ((MultiValue)momentValue).setValue(vocabularyItem);
                    }
                    valueTable.updateValue(momentValue);
                }
            }
            else {
                // new value has to be added to moment
                controlValue.setMomentUid(moment.getUid());
                controlValue = valueTable.addValue(controlValue);
                moment.setValue(control.getName(), controlValue);
            }
        }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		/*switch (requestCode) {
			case PhotoUtils.TAKE_PHOTO_INTENT:
				if (resultCode == Activity.RESULT_OK) {
					Handler handler = new Handler(ChangeMomentActivity.this);
					PhotoUtils.generateThumbnails(ChangeMomentActivity.this, moment.getPhotoUri(), handler);
				}
				else {
					moment.setPhotoUri(null);
				}
				break;
			
			case PhotoUtils.CHANGE_PHOTO_INTENT:
				if (resultCode == Activity.RESULT_OK) {
					if (PhotoUtils.swapPhoto(ChangeMomentActivity.this, moment.getTimestamp())) {
						Handler handler = new Handler(ChangeMomentActivity.this);
						PhotoUtils.regenerateThumbnails(ChangeMomentActivity.this, moment.getPhotoUri(), handler);
					}
				}
				else {
					PhotoUtils.deleteSwapPhoto(ChangeMomentActivity.this);
				}
				break;
		}*/
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
        viewManager.serializeValues(savedState);
        savedState.putLong("ChangeMomentActivity_momentId", moment.getUid());

        if (mode == InputControl.Mode.CREATE) {
            savedState.putInt("ChangeMomentActivity_mode", 1);
        }
        else if (mode == InputControl.Mode.EDIT) {
            savedState.putInt("ChangeMomentActivity_mode", 2);
        }
	}

	/*@Override
	public void onClick(View v) {
		if (v.getParent() instanceof TagControl) {
			onClickRemoveTag(v);
		}
	}*/
	
	@Override
	public void onBackPressed() {
        if (mode == InputControl.Mode.CREATE) {
            // todo respect edit permissions
            AlertDialog.Builder momentSavedBuilder = new AlertDialog.Builder(ChangeMomentActivity.this);
            momentSavedBuilder.setTitle(R.string.new_sample_back_warning_title);
            momentSavedBuilder.setMessage(R.string.new_sample_back_warning_msg);
            momentSavedBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ChangeMomentActivity.this.saveMoment();
                    ChangeMomentActivity.this.finish();
                }
            });
            momentSavedBuilder.setNegativeButton(R.string.no, null);

            momentSavedBuilder.create().show();
        }
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (ChangeMomentActivity.this.isFinishing() && mode == InputControl.Mode.CREATE) {
			ChangeMomentActivity.this.saveMoment();
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_take_photo:

				Intent takePhoto = PhotoUtils.getTakePhotoIntent(ChangeMomentActivity.this, moment.getTimestamp());

				if (takePhoto != null) {
					Bundle extras = takePhoto.getExtras();
					Uri photoUri = (Uri)extras.get(PhotoUtils.EXTRA_KEY);

                    SingleValue value = new SingleValue();
                    value.setInputElementUid(photoView.getInputElementUid());
                    value.setMomentUid(moment.getUid());
                    value.setValue(photoUri.getPath());
                    photoView.setValue(value);

					startActivityForResult(takePhoto, PhotoUtils.TAKE_PHOTO_INTENT);
				}
				break;

			case R.id.action_change_photo:

				Intent changePhoto = PhotoUtils.getChangePhotoIntent(ChangeMomentActivity.this);

				if (changePhoto != null) {
					startActivityForResult(changePhoto, PhotoUtils.CHANGE_PHOTO_INTENT);
				}
				break;

			case R.id.action_delete_photo:

				AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(ChangeMomentActivity.this);
		        deleteBuilder.setTitle(R.string.photo_delete_warning_title);
		        deleteBuilder.setMessage(R.string.photo_delete_warning_msg);
		        deleteBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
                        SingleValue value = (SingleValue)photoView.getValue();
		            	// delete photo on storage
		            	PhotoUtils.deletePhoto(ChangeMomentActivity.this, value.getValue());

                        value.setValue("");
		            	photoView.setValue(value);
		            	photoView.unsetPhoto(); // todo still needed?
		            }
		        });
		        deleteBuilder.setNegativeButton(R.string.no, null);
		        deleteBuilder.create().show();
		        break;
		}
		return true;
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == AsyncImageScaler.MSG_SUCCESS) {
			Bitmap photoBitmap = (Bitmap)msg.obj;
			if (photoBitmap != null && msg.arg1 == 48) {
				photoView.setPhoto(photoBitmap);
				return true;
			}
		}
		
		if (msg.what == AsyncImageScaler.MSG_ERROR) {
			// error handling
		}
		
		return false;
	}
}
