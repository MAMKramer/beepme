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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.glanznig.beepme.R;
import com.glanznig.beepme.data.DataExporter;
import com.glanznig.beepme.helper.PhotoUtils;

import java.io.File;

public class ExportActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "ExportActivity";
    private boolean photoExport = true;
    private int postActionItem = -1;

    private DataExporter exporter;

    private Spinner postActions;
    private Spinner downscalePhotos;
    private TextView estimatedSize;

	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.export_data);

        exporter = new DataExporter(ExportActivity.this);

        if (savedState != null) {
            photoExport = savedState.getBoolean("photoExport");

            if (savedState.getInt("postActionItem") != 0) {
                postActionItem = savedState.getInt("postActionItem") - 1;
            }
        }
	}
	
	@Override
	public void onResume() {
		super.onResume();
        populateFields();
	}

    private void populateFields() {
        CheckBox photoExp = (CheckBox)findViewById(R.id.export_photos);
        View photoExpGroup = findViewById(R.id.export_photos_group);

        if (PhotoUtils.isEnabled(ExportActivity.this)) {
            photoExp.setVisibility(View.VISIBLE);
            photoExpGroup.setVisibility(View.VISIBLE);

            File[] photos = PhotoUtils.getPhotos(ExportActivity.this);
            if (photos != null && photos.length > 0) {
                photoExp.setEnabled(true);
                photoExp.setChecked(photoExport);
                photoExp.setOnClickListener(this);

                enableDisableView(photoExpGroup, photoExport);
            }
            else {
                photoExp.setChecked(false);
                photoExp.setEnabled(false);
                enableDisableView(photoExpGroup, false);
            }

            downscalePhotos = (Spinner)findViewById(R.id.export_downscale_photos);
            ArrayAdapter<CharSequence> downscaleAdapter = ArrayAdapter.createFromResource(this,
                    R.array.export_downscale_photos, android.R.layout.simple_spinner_item);
            downscaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            downscalePhotos.setAdapter(downscaleAdapter);
        }
        else {
            photoExp.setVisibility(View.GONE);
            photoExpGroup.setVisibility(View.GONE);
        }

        postActions = (Spinner)findViewById(R.id.export_post_actions);
        ArrayAdapter<CharSequence> actionsAdapter = ArrayAdapter.createFromResource(this,
                R.array.post_export_actions, android.R.layout.simple_spinner_item);
        actionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postActions.setAdapter(actionsAdapter);

        if (postActionItem != -1) {
            postActions.setSelection(postActionItem);
        }

        estimatedSize = (TextView)findViewById(R.id.export_estimated_size);
        estimatedSize.setText(exporter.getReadableArchiveSize(photoExport));
    }

    private void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;

            for (int i = 0 ; i < group.getChildCount(); i++) {
                enableDisableView(group.getChildAt(i), enabled);
            }
        }
    }

    @Override
	public void onSaveInstanceState(Bundle savedState) {
        savedState.putBoolean("photoExport", photoExport);
        savedState.putInt("postActionItem", postActions.getSelectedItemPosition() + 1);
	}

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.export_photos:
                photoExport = ((CheckBox)view).isChecked();
                enableDisableView(findViewById(R.id.export_photos_group), photoExport);
                estimatedSize.setText(exporter.getReadableArchiveSize(photoExport));

                break;
        }
    }
}
