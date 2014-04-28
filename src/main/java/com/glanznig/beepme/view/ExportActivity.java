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

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.util.DataExporter;
import com.glanznig.beepme.helper.PhotoUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

public class ExportActivity extends Activity implements View.OnClickListener {

    private static class ExportHandler extends Handler {
		WeakReference<ExportActivity> activity;
		Bundle data;

		ExportHandler(ExportActivity activity) {
			this.activity = new WeakReference<ExportActivity>(activity);
		}

		@Override
		public void handleMessage(Message message) {
            if (activity.get() != null) {
                NotificationManager manager = (NotificationManager)activity.get().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(TAG, EXPORT_RUNNING_NOTIFICATION);

                BeepMeApp app = (BeepMeApp) activity.get().getApplication();
                app.getPreferences().setExportRunningSince(0L);

                ProgressBar progress = (ProgressBar)activity.get().findViewById(R.id.export_progress_bar);
                progress.setVisibility(View.GONE);
                DisplayMetrics metrics = activity.get().getResources().getDisplayMetrics();
                TextView runningText = (TextView)activity.get().findViewById(R.id.export_running_text);
                runningText.setTextSize(16);
                runningText.setPadding(0, (int)(32 * metrics.density + 0.5f), 0, 0);

				data = message.getData();
				if (data.getString("fileName") != null) {
                    // check if archive exists
                    File archive = new File(data.getString("fileName"));
                    if (archive != null && archive.exists()) {
                        runningText.setText(R.string.export_successful);
                        activity.get().createExportFinishedNotification(true);
                    }
                    else {
                        runningText.setText(R.string.export_failed);
                        activity.get().createExportFinishedNotification(false);
                    }

                    String action = data.getString("action");
                    if (action != null) {
                        if (action.equals("mail")) {
                            Uri fUri = Uri.fromFile(archive);
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, activity.get().getString(R.string.export_mail_subject));
                            sendIntent.putExtra(Intent.EXTRA_STREAM, fUri);
                            sendIntent.setType("text/rfc822");
                            try {
                                activity.get().startActivity(Intent.createChooser(sendIntent, activity.get().getString((R.string.export_mail_chooser_title))));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(activity.get(), R.string.export_no_mail_apps, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
				}
			}
		}
	}

	private static class ExportRunnable implements Runnable {
		WeakReference<ExportActivity> activity;
        Bundle opts;
        String action;

		public ExportRunnable(ExportActivity activity, Bundle opts) {
			this.activity = new WeakReference<ExportActivity>(activity);
            this.opts = opts;

            Spinner actions = (Spinner)this.activity.get().findViewById(R.id.export_post_actions);
            int pos = actions.getSelectedItemPosition();

            switch (pos) {
                case 1:
                    action = "mail";
                    break;
            }
		}
		@Override
	    public void run() {
			if (activity.get() != null) {
				DataExporter exporter = new DataExporter(activity.get().getApplicationContext());
				String fileName;
                int densityFactor = 1;
                if (opts.getBoolean("photoExport", true)) {
                    if (activity.get().densityItem > -1) {
                        densityFactor = activity.get().densityFactors.get(activity.get().densityItem);
                    }
                }
                opts.putInt("densityFactor", densityFactor);
                fileName = exporter.exportToZipFile(opts);

				if (activity.get().exportHandler != null) {
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("fileName", fileName);
                    if (action != null) {
                        bundle.putString("action", action);
                    }
					msg.setData(bundle);
					activity.get().exportHandler.sendMessage(msg);
				}
			}
	    }
	}

    private static class PhotoDimRunnable implements Runnable {
        WeakReference<ExportActivity> activity;
        WeakReference<PhotoDimHandler> handler;

        public PhotoDimRunnable(ExportActivity activity, PhotoDimHandler handler) {
            this.activity = new WeakReference<ExportActivity>(activity);
            this.handler = new WeakReference<PhotoDimHandler>(handler);
        }
        @Override
        public void run() {
            if (activity.get() != null) {

                File[] photos = PhotoUtils.getPhotos(activity.get());
                int count = 0;
                int overallDensity = 0;

                if (photos != null) {
                    for (count = 0; count < photos.length; count++) {
                        Bundle dim = PhotoUtils.getPhotoDimensions(photos[count].getAbsolutePath());
                        int width = dim.getInt("width");
                        int height = dim.getInt("height");

                        overallDensity += width * height;
                    }
                }

                float avgDensity;
                if (count > 0 ) {
                    avgDensity = ((float)overallDensity / count) / 1000000; // megapixel
                }
                else {
                    avgDensity = 0;
                }

                if (handler.get() != null) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putFloat("photoAvgDensity", avgDensity);
                    msg.setData(bundle);
                    handler.get().sendMessage(msg);
                }
            }
        }
    }

    private static class PhotoDimHandler extends Handler {
        WeakReference<ExportActivity> activity;

        PhotoDimHandler(ExportActivity activity) {
            this.activity = new WeakReference<ExportActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (activity.get() != null) {
                Bundle data = message.getData();
                if (data.containsKey("photoAvgDensity")) {
                    activity.get().photoAvgDensity = data.getFloat("photoAvgDensity");
                    TextView density = (TextView)activity.get().findViewById(R.id.export_photos_avg_size);
                    density.setText(activity.get().getString(R.string.export_photos_avg_size,
                            String.format("%.1f", activity.get().photoAvgDensity)));

                    activity.get().downscaleAdapter.add(String.format(activity.get().getString(R.string.export_downscale_original_size),
                            DataExporter.getReadableFileSize(activity.get().photoAvgSize, 0)));
                    activity.get().densityFactors.add(Integer.valueOf(1));
                    int densityFactor = 2;

                    for (; (Math.round((activity.get().photoAvgDensity / densityFactor) * 2) / 2f) > 1; densityFactor *= 2) {
                        activity.get().downscaleAdapter.add(String.format(activity.get().getString(R.string.export_downscale),
                                String.format("%.1f", Math.round((activity.get().photoAvgDensity / densityFactor) * 2) / 2f),
                                DataExporter.getReadableFileSize(activity.get().photoAvgSize / densityFactor, 0)));
                        activity.get().densityFactors.add(Integer.valueOf(densityFactor));
                    }

                    activity.get().downscaleAdapter.add(String.format(activity.get().getString(R.string.export_downscale),
                            String.valueOf(1), DataExporter.getReadableFileSize(activity.get().photoAvgSize / densityFactor, 0)));
                    activity.get().densityFactors.add(Integer.valueOf(densityFactor));

                    if (activity.get().densityItem != -1) {
                        activity.get().downscalePhotos.setSelection(activity.get().densityItem);
                    }
                }
            }
        }
    }

	private static final String TAG = "ExportActivity";
    public static final int EXPORT_RUNNING_NOTIFICATION = 523;
    private static final int EXPORT_FINISHED_NOTIFICATION = 524;
    private boolean rawExport = false;
    private boolean photoExport = true;
    private int postActionItem = -1;
    private int densityItem = -1;
    private double photoAvgSize = 0;
    private float photoAvgDensity = 0;
    private ArrayList<Integer> densityFactors;

    private DataExporter exporter;
    private ArrayAdapter<CharSequence> downscaleAdapter;
    private ExportHandler exportHandler;

    private Spinner postActions;
    private Spinner downscalePhotos;
    private TextView estimatedSize;

	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.export_data);

        exporter = new DataExporter(ExportActivity.this);
        densityFactors = new ArrayList<Integer>();

        if (savedState != null) {
            rawExport = savedState.getBoolean("rawExport");
            photoExport = savedState.getBoolean("photoExport");

            if (savedState.getInt("postActionItem") != 0) {
                postActionItem = savedState.getInt("postActionItem") - 1;
            }

            if (savedState.getInt("densityItem") != 0) {
                densityItem = savedState.getInt("densityItem") - 1;
            }

            photoAvgSize = savedState.getDouble("photoAvgSize");
            photoAvgDensity = savedState.getFloat("photoAvgDensity");
        }
	}
	
	@Override
	public void onResume() {
		super.onResume();
        populateFields();
	}

    private void populateFields() {
        BeepMeApp app = (BeepMeApp)getApplication();

        if (app.getPreferences().exportRunningSince() == 0L ||
                (Calendar.getInstance().getTimeInMillis() -
                        app.getPreferences().exportRunningSince()) >= 120000) { //2 min

            enableDisableView(findViewById(R.id.export_settings), true);

            CheckBox rawExp = (CheckBox)findViewById(R.id.export_raw);
            rawExp.setChecked(rawExport);
            rawExp.setOnClickListener(this);

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
                } else {
                    photoExp.setChecked(false);
                    photoExp.setEnabled(false);
                    enableDisableView(photoExpGroup, false);
                }

                downscalePhotos = (Spinner)findViewById(R.id.export_downscale_photos);
                downscaleAdapter = new ArrayAdapter<CharSequence>(ExportActivity.this,
                        android.R.layout.simple_spinner_item, new ArrayList<CharSequence>());
                downscaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                downscalePhotos.setAdapter(downscaleAdapter);
                downscalePhotos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        densityItem = position;
                        Bundle opts = new Bundle();
                        opts.putBoolean("photoExport", photoExport);
                        opts.putBoolean("rawExport", rawExport);
                        estimatedSize.setText(String.format(getString(R.string.export_archive_estimated_size),
                                exporter.getReadableArchiveSize(opts,
                                        densityFactors.get(downscalePhotos.getSelectedItemPosition()).intValue())));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                if (photoAvgSize == 0) {
                    File[] photoFiles = PhotoUtils.getPhotos(ExportActivity.this);
                    int count = 0;
                    int overallSize = 0;

                    if (photoFiles != null) {
                        for (count = 0; count < photoFiles.length; count++) {
                            overallSize += photoFiles[count].length();
                        }
                        if (count > 0) {
                            photoAvgSize = overallSize / count;
                        }
                    }
                }

                if (photoAvgDensity == 0) {
                    new Thread(new PhotoDimRunnable(ExportActivity.this, new PhotoDimHandler(ExportActivity.this))).start();
                }
                else {
                    TextView density = (TextView)findViewById(R.id.export_photos_avg_size);
                    density.setText(getString(R.string.export_photos_avg_size,
                            String.format("%.1f", photoAvgDensity)));

                    downscaleAdapter.add(String.format(getString(R.string.export_downscale_original_size),
                            DataExporter.getReadableFileSize(photoAvgSize, 0)));
                    densityFactors.add(Integer.valueOf(1));
                    int densityFactor = 2;

                    for (; (Math.round((photoAvgDensity / densityFactor) * 2) / 2f) > 1; densityFactor *= 2) {
                        downscaleAdapter.add(String.format(getString(R.string.export_downscale),
                                String.format("%.1f", Math.round((photoAvgDensity / densityFactor) * 2) / 2f),
                                DataExporter.getReadableFileSize(photoAvgSize / densityFactor, 0)));
                        densityFactors.add(Integer.valueOf(densityFactor));
                    }

                    downscaleAdapter.add(String.format(getString(R.string.export_downscale),
                            String.valueOf(1), DataExporter.getReadableFileSize(photoAvgSize / densityFactor, 0)));
                    densityFactors.add(Integer.valueOf(densityFactor));

                    if (densityItem != -1) {
                        downscalePhotos.setSelection(densityItem);
                    }
                }

            } else {
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
            int densityFactor = 1;

            if (densityFactors.size() > 0 && downscalePhotos.getSelectedItemPosition() >= 0) {
                densityFactor = densityFactors.get(downscalePhotos.getSelectedItemPosition()).intValue();
            }

            Bundle opts = new Bundle();
            opts.putBoolean("photoExport", photoExport);
            opts.putBoolean("rawExport", rawExport);
            estimatedSize.setText(String.format(getString(R.string.export_archive_estimated_size),
                    exporter.getReadableArchiveSize(opts, densityFactor)));

            Button start = (Button)findViewById(R.id.export_start_button);
            ProgressBar progress = (ProgressBar)findViewById(R.id.export_progress_bar);
            TextView runningText = (TextView)findViewById(R.id.export_running_text);

            start.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            runningText.setVisibility(View.GONE);
        }
        else {
            Button start = (Button)findViewById(R.id.export_start_button);
            ProgressBar progress = (ProgressBar)findViewById(R.id.export_progress_bar);
            TextView runningText = (TextView)findViewById(R.id.export_running_text);

            start.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            runningText.setVisibility(View.VISIBLE);

            enableDisableView(findViewById(R.id.export_settings), false);
        }
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
        savedState.putBoolean("rawExport", rawExport);
        savedState.putBoolean("photoExport", photoExport);
        savedState.putInt("postActionItem", postActions.getSelectedItemPosition() + 1);
        savedState.putInt("densityItem", downscalePhotos.getSelectedItemPosition() + 1);
        savedState.putDouble("photoAvgSize", photoAvgSize);
        savedState.putFloat("photoAvgDensity", photoAvgDensity);
	}

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.export_photos:
                photoExport = ((CheckBox)view).isChecked();
                enableDisableView(findViewById(R.id.export_photos_group), photoExport);
                break;

            case R.id.export_raw:
                rawExport = ((CheckBox)view).isChecked();
                break;
        }

        Bundle opts = new Bundle();
        opts.putBoolean("photoExport", photoExport);
        opts.putBoolean("rawExport", rawExport);
        estimatedSize.setText(String.format(getString(R.string.export_archive_estimated_size),
                exporter.getReadableArchiveSize(opts,
                        densityFactors.get(downscalePhotos.getSelectedItemPosition()).intValue())
        ));
    }

    public void startExport(View v) {
        Button start = (Button)findViewById(R.id.export_start_button);
        ProgressBar progress = (ProgressBar)findViewById(R.id.export_progress_bar);
        TextView runningText = (TextView)findViewById(R.id.export_running_text);

        BeepMeApp app = (BeepMeApp)getApplication();

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            enableDisableView(findViewById(R.id.export_settings), false);

            start.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            runningText.setVisibility(View.VISIBLE);

            if (app.getPreferences().exportRunningSince() == 0L ||
                    (Calendar.getInstance().getTimeInMillis() -
                    app.getPreferences().exportRunningSince()) >= 120000) { //2 min
                app.getPreferences().setExportRunningSince(Calendar.getInstance().getTimeInMillis());
                createExportRunningNotification();
                exportHandler = new ExportHandler(ExportActivity.this);

                Bundle opts = new Bundle();
                opts.putBoolean("photoExport", photoExport);
                opts.putBoolean("rawExport", rawExport);
                new Thread(new ExportRunnable(ExportActivity.this, opts)).start();
            }
        }
        else {
            Toast.makeText(ExportActivity.this, R.string.export_storage_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void createExportRunningNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.getApplicationContext());
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify);
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notificationBuilder.setContentText(this.getString(R.string.export_running_data));

        //set as ongoing, so it cannot be cleared
        notificationBuilder.setOngoing(true);

        //add progress bar (indeterminate)
        notificationBuilder.setProgress(0, 0, true);

        Intent resultIntent = new Intent(this.getApplicationContext(), ExportActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.getApplicationContext());
        stackBuilder.addParentStack(ExportActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(TAG, EXPORT_RUNNING_NOTIFICATION, notificationBuilder.build());
    }

    private void createExportFinishedNotification(boolean successful) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.getApplicationContext());
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify);
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        String notify;

        if (successful) {
            notify = this.getString(R.string.export_successful);
        }
        else {
            notify = this.getString(R.string.export_failed);
        }

        notificationBuilder.setContentText(notify);

        Intent resultIntent = new Intent(this.getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(TAG, EXPORT_FINISHED_NOTIFICATION, notificationBuilder.build());
    }
}
