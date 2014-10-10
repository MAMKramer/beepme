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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Project;
import com.glanznig.beepme.data.Restriction;

/**
 * Displays information about a project. This serves as overview and introduction at the same
 * time. It displays what can and what cannot be done within this project.
 */
public class ProjectInfoActivity extends Activity {

    private static final String TAG = "WelcomeActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.project_info);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        BeepMeApp app = (BeepMeApp)getApplication();
        Project project = app.getCurrentProject();
        if (project != null) {
            ((TextView)findViewById(R.id.project_info_project_name)).setText(project.getName());

            Restriction editRestriction = project.getRestriction(Restriction.RestrictionType.EDIT);
            if (editRestriction != null) {
                // if either allowed (maybe until) or forbidden, but later allowed
                if (editRestriction.getAllowed() || (!editRestriction.getAllowed() && editRestriction.getUntil() != null )) {
                    if (editRestriction.getUntil() != null) {

                        // todo format and localize
                        String duration = editRestriction.getUntil().toString();

                        if (editRestriction.getAllowed()) {
                            ((TextView)findViewById(R.id.project_info_allowed_edit_duration)).setText(String.format(getString(R.string.project_info_duration_until), duration));
                            ((TextView)findViewById(R.id.project_info_forbidden_edit_duration)).setText(String.format(getString(R.string.project_info_duration_after), duration));
                        }
                        else {
                            ((TextView)findViewById(R.id.project_info_allowed_edit_duration)).setText(String.format(getString(R.string.project_info_duration_after), duration));
                            ((TextView)findViewById(R.id.project_info_forbidden_edit_duration)).setText(String.format(getString(R.string.project_info_duration_until), duration));
                        }
                    }
                    else {
                        findViewById(R.id.project_info_allowed_edit_duration).setVisibility(View.GONE);
                        findViewById(R.id.project_info_forbidden_edit_duration).setVisibility(View.GONE);
                        // hide forbidden info element
                        findViewById(R.id.project_info_forbidden_edit).setVisibility(View.GONE);
                    }
                }
                else {
                    findViewById(R.id.project_info_forbidden_edit_duration).setVisibility(View.GONE);
                    // hide allowed info element
                    findViewById(R.id.project_info_allowed_edit).setVisibility(View.GONE);
                }
            }

            Restriction deleteRestriction = project.getRestriction(Restriction.RestrictionType.DELETE);
            if (deleteRestriction != null) {
                // if either allowed (maybe until) or forbidden, but later allowed
                if (deleteRestriction.getAllowed() || (!deleteRestriction.getAllowed() && deleteRestriction.getUntil() != null )) {
                    if (deleteRestriction.getUntil() != null) {

                        // todo format and localize
                        String duration = deleteRestriction.getUntil().toString();

                        if (deleteRestriction.getAllowed()) {
                            ((TextView)findViewById(R.id.project_info_allowed_delete_duration)).setText(String.format(getString(R.string.project_info_duration_until), duration));
                            ((TextView)findViewById(R.id.project_info_forbidden_delete_duration)).setText(String.format(getString(R.string.project_info_duration_after), duration));
                        }
                        else {
                            ((TextView)findViewById(R.id.project_info_allowed_delete_duration)).setText(String.format(getString(R.string.project_info_duration_after), duration));
                            ((TextView)findViewById(R.id.project_info_forbidden_delete_duration)).setText(String.format(getString(R.string.project_info_duration_until), duration));
                        }
                    }
                    else {
                        findViewById(R.id.project_info_allowed_delete_duration).setVisibility(View.GONE);
                        // hide forbidden info element
                        findViewById(R.id.project_info_forbidden_edit).setVisibility(View.GONE);
                    }
                }
                else {
                    findViewById(R.id.project_info_forbidden_delete_duration).setVisibility(View.GONE);
                    // hide allowed info element
                    findViewById(R.id.project_info_allowed_delete).setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
