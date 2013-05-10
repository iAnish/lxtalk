package com.mac.lxtalk;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        this.setTitle("Settings");
        
		this.addPreferencesFromResource(R.xml.preferences);
	}
}
