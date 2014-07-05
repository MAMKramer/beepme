package com.glanznig.beepme.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.xml.ProjectXml;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by michael on 04.07.14.
 */
public class WelcomeActivity extends Activity {

    private static final String TAG = "WelcomeActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BeepMeApp app = (BeepMeApp)getApplication();
        if (app.getPreferences().getProjectId() == 0L) {
            // create a project
            ProjectXml parser = new ProjectXml(this);
            InputStream xml = this.getResources().openRawResource(R.raw.project_sampling);
            Scanner scanner = new Scanner(xml);
            String lineSeparator = System.getProperty("line.separator");
            scanner.useDelimiter(lineSeparator);
            StringBuilder content = new StringBuilder();

            try {
                while(scanner.hasNextLine()) {
                    content.append(scanner.nextLine() + lineSeparator);
                }
            } finally {
                scanner.close();
            }

            if (parser.validate(content.toString())) {
                parser.parse(content.toString());
                parser.persist();

                if (parser.getProject() != null) {
                    app.getPreferences().setProjectId(parser.getProject().getUid());
                }
            }
            else {
                Log.e(TAG, "Project XML is not valid. Aborting.");
            }
        }

        /*if (app.getPreferences().getProjectId() != 0L) {
            Intent intent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            stackBuilder.startActivities();
        }*/
    }
}
