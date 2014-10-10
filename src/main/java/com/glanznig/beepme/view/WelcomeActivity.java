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
                while (scanner.hasNextLine()) {
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
            } else {
                Log.e(TAG, "Project XML is not valid. Aborting.");
            }
        }
    }
}
