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
import com.sun.msv.verifier.jarv.TheFactoryImpl;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by michael on 16.04.14.
 */
public class ProjectParser {

    private static final String TAG = "ProjectParser";
    private static final int schemaResId = R.raw.project;
    private Context ctx;

    private static VerifierFactory factory;
    private static SAXParserFactory parserFactory;
    private static Schema schema;

    public ProjectParser(Context ctx) {
        this.ctx = ctx;

        if (factory == null) {
            factory = new TheFactoryImpl();
        }
        if (schema == null) {
            try {
                schema = factory.compileSchema(ctx.getResources().openRawResource(schemaResId));
            }
            catch(Exception e) {
                Log.e(TAG, "error compiling schema: " + e.getMessage());
            }
        }
        if (parserFactory == null) {
            parserFactory = SAXParserFactory.newInstance();
        }
    }

    public boolean validate(String xml) {
        try {
            Verifier verify = schema.newVerifier();
            VerifierHandler handler = verify.getVerifierHandler();

            SAXParser sp = parserFactory.newSAXParser();
            XMLReader reader = sp.getXMLReader();
            reader.setContentHandler(handler);

            reader.parse(new InputSource(new StringReader(xml)));

            if (handler.isValid()) {
                return true;
            }
        }
        catch(Exception e) {
            Log.e(TAG, "error validating: " + e.getMessage());
        }

        return false;
    }

    public void parse(String xml) {
        if (xml != null) {
        }
    }

    public void persist() {

    }
}
