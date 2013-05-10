package com.mac.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;

public class SettingsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        Window w=this.getWindow();
        w.setTitle("Settings");
        
		this.addPreferencesFromResource(R.xml.preferences);
	}
}
