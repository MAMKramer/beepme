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

package com.glanznig.beepme.data.xml;

import android.content.Context;
import android.util.Log;

import com.glanznig.beepme.R;
import com.glanznig.beepme.data.Project;
import com.glanznig.beepme.data.RandomTimer;
import com.glanznig.beepme.data.Restriction;
import com.glanznig.beepme.data.Timer;
import com.glanznig.beepme.data.db.ProjectTable;
import com.glanznig.beepme.data.xml.datatype.DatatypeFactoryImpl;
import com.sun.msv.verifier.jarv.TheFactoryImpl;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Verifies, parses and persists data of a supplied XML project document.
 * The verification gives yes/no status only. The parser transforms XML entities to POJOs and
 * persists them to the SQLite database.
 */
public class ProjectXml {

    private static final String TAG = "ProjectParser";
    private static final int schemaResId = R.raw.project;

    private static VerifierFactory verifierFactory;
    private static SAXParserFactory parserFactory;
    private static Schema schema;
    private static XmlPullParserFactory pullParserFactory;

    private HashMap<String, Object> data;
    private Context ctx;
    private Boolean valid;

    public ProjectXml(Context ctx) {

        this.ctx = ctx;
        data = new HashMap<String, Object>();
        valid = null;

        if (verifierFactory == null) {
            verifierFactory = new TheFactoryImpl();
        }
        if (schema == null) {
            try {
                schema = verifierFactory.compileSchema(ctx.getResources().openRawResource(schemaResId));
            }
            catch(Exception e) {
                Log.e(TAG, "error compiling schema: " + e.getMessage());
            }
        }
        if (parserFactory == null) {
            parserFactory = SAXParserFactory.newInstance();
        }
        if (pullParserFactory == null) {
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
            }
            catch (XmlPullParserException xppe) {}
        }
    }

    /**
     * Verifies (yes/no only) if the supplied XML document adheres to the internally specified
     * XML Schema.
     * @param xml XML document as String
     * @return true if XML document is valid, false otherwise
     */
    public boolean validate(String xml) {
        try {
            Verifier verify = schema.newVerifier();
            VerifierHandler handler = verify.getVerifierHandler();

            SAXParser sp = parserFactory.newSAXParser();
            XMLReader reader = sp.getXMLReader();
            reader.setContentHandler(handler);

            reader.parse(new InputSource(new StringReader(xml)));

            if (handler.isValid()) {
                valid = new Boolean(true);
                return true;
            }
        }
        catch(Exception e) {
            Log.e(TAG, "error validating: " + e.getMessage());
        }

        valid = new Boolean(false);
        return false;
    }

    public void parse(String xml) {
        if (xml != null) {
            if (valid == null) {
                validate(xml);
            }

            if (valid.booleanValue()) {
                try {
                    if (pullParserFactory == null) {
                        pullParserFactory = XmlPullParserFactory.newInstance();
                    }
                    XmlPullParser parser = pullParserFactory.newPullParser();
                    parser.setInput(new StringReader(xml));
                    int eventType = parser.getEventType();

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            String tagName = parser.getName();

                            if (tagName.equals("project")) {
                                Log.i(TAG, "tag project");
                                Project project = parseProject(parser);
                                if (project != null) {
                                    data.put("project", project);
                                }
                            }
                            else if (tagName.equals("restrictions")) {
                                Log.i(TAG, "tag restrictions");
                                Project project = (Project)data.get("project");
                                if (project != null) {
                                    List<Restriction> restrictions = parseRestrictions(parser);
                                    if (!restrictions.isEmpty()) {
                                        Iterator<Restriction> restr = restrictions.iterator();
                                        while (restr.hasNext()) {
                                            project.setRestriction(restr.next());
                                        }
                                    }
                                }

                            }
                            else if (tagName.equals("timer")) {
                                Log.i(TAG, "tag timer");
                                Project project = (Project)data.get("project");
                                if (project != null) {
                                    project.setTimer(parseTimer(parser));
                                }
                            }
                        }
                        if (eventType == XmlPullParser.END_TAG) {

                        }
                        eventType = parser.next();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error parsing: " + e.getMessage());
                }
            }
        }
    }

    private Project parseProject(XmlPullParser parser) throws Exception {
        Project project = new Project();

        // attr name (required)
        project.setName(parser.getAttributeValue(null, "name"));
        // attr lang (required)
        project.setLanguage(new Locale(parser.getAttributeValue(null, "lang")));
        // attr type (required)
        String type = parser.getAttributeValue(null, "type");
        if (type.equals("sampling")) {
            project.setType(Project.ProjectType.SAMPLING);
        }
        else if (type.equals("probes")) {
            project.setType(Project.ProjectType.PROBES);
        }
        else if (type.equals("lifelog")) {
            project.setType(Project.ProjectType.LIFELOG);
        }
        if (parser.getAttributeValue(null, "starts") != null ||
                parser.getAttributeValue(null, "expires") != null) {
            DatatypeFactory typeFactory = new DatatypeFactoryImpl();
            // attr starts
            if (parser.getAttributeValue(null, "starts") != null) {
                XMLGregorianCalendar starts = typeFactory.newXMLGregorianCalendar(parser.getAttributeValue(null, "starts"));
                project.setStart(starts.toGregorianCalendar().getTime());
            }
            // attr expires
            if (parser.getAttributeValue(null, "expires") != null) {
                XMLGregorianCalendar expires = typeFactory.newXMLGregorianCalendar(parser.getAttributeValue(null, "expires"));
                project.setExpire(expires.toGregorianCalendar().getTime());
            }
        }
        project.setStatus(Project.ProjectStatus.ACTIVE);

        return project;
    }

    private List<Restriction> parseRestrictions(XmlPullParser parser) throws Exception {
        ArrayList<Restriction> restrictions = new ArrayList<Restriction>();
        Restriction edit = new Restriction(Restriction.RestrictionType.EDIT, true);
        Restriction delete = new Restriction(Restriction.RestrictionType.DELETE, true);
        int eventType = 0;
        boolean inRestrictions = true;
        DatatypeFactory typeFactory = new DatatypeFactoryImpl();

        do {
            eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                String allowed = parser.getAttributeValue(null, "allowed");
                String until = parser.getAttributeValue(null, "until");
                boolean allow = true;

                if (allowed.equals("yes")) {
                    allow = true;
                }
                else if (allowed.equals("no")) {
                    allow = false;
                }

                if (parser.getName().equals("edit")) {
                    edit = new Restriction(Restriction.RestrictionType.EDIT, allow);
                    if (until != null) {
                        Date now = new Date();
                        edit.setUntil(typeFactory.newDuration(until).getTimeInMillis(now) / 1000);
                    }
                }
                else if (parser.getName().equals("delete")) {
                    delete = new Restriction(Restriction.RestrictionType.DELETE, allow);
                    if (until != null) {
                        Date now = new Date();
                        delete.setUntil(typeFactory.newDuration(until).getTimeInMillis(now) / 1000);
                    }
                }
            }
            if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("restrictions")) {
                    inRestrictions = false;
                }
            }
        }
        while(inRestrictions);
        restrictions.add(edit);
        restrictions.add(delete);

        return restrictions;
    }

    private Timer parseTimer(XmlPullParser parser) throws Exception {
        //attr sound (default=pling)
        String sound = parser.getAttributeValue(null, "sound");

        int eventType;
        boolean inTimer = true;
        DatatypeFactory typeFactory = new DatatypeFactoryImpl();
        Timer timer = null;
        do {
            eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                String name = parser.getName();

                if (name.equals("random")) {
                    String strategy = parser.getAttributeValue(null, "strategy");
                    String min = parser.getAttributeValue(null, "min");
                    String max = parser.getAttributeValue(null, "max");
                    String avg = parser.getAttributeValue(null, "avg");

                    Date now = new Date();
                    long minVal = typeFactory.newDuration(min).getTimeInMillis(now) / 1000;
                    long maxVal = typeFactory.newDuration(max).getTimeInMillis(now) / 1000;

                    RandomTimer random = null;
                    if (strategy.equals("interval")) {
                        random = new RandomTimer(ctx, RandomTimer.TimerStrategy.INTERVAL, minVal, maxVal);
                    }
                    else if (strategy.equals("average")) {
                        random = new RandomTimer(ctx, RandomTimer.TimerStrategy.AVERAGE, minVal, maxVal);
                    }

                    if (random != null && avg != null) {
                        random.setAvg(typeFactory.newDuration(avg).getTimeInMillis(now) / 1000);
                    }

                    timer = random;
                }
            }
            if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("timer")) {
                    inTimer = false;
                }
            }
        }
        while(inTimer);

        if (timer != null) {
            timer.setSound(sound);
        }

        return timer;
    }

    public void persist() {
        Project project = (Project)data.get("project");
        if (project != null) {
            Log.i(TAG, "persisting project");
            ProjectTable projectTable = new ProjectTable(ctx);
            projectTable.addProject(project);
        }
    }
}
