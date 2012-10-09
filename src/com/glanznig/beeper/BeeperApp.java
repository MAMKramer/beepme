package com.glanznig.beeper;

import java.util.ArrayList;
import java.util.List;

import com.glanznig.beeper.data.Sample;

import android.app.Application;

public class BeeperApp extends Application {
	
	public List<Sample> getSamples() {
		return new ArrayList<Sample>();
	}

}

