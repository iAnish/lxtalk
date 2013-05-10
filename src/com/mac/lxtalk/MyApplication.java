package com.mac.lxtalk;

import android.app.Application;

public class MyApplication extends Application {

	private SQLArchiver archiver=null;
	
	private Notifier notifier=null;
	
	public SQLArchiver getArchiver(){
		if(archiver==null)
			archiver=new SQLArchiver(this.getBaseContext());
		
		return archiver;
	}
	
	public Notifier getNotifier(){
		if(notifier==null){
			notifier=new Notifier(this.getBaseContext());
		}
		
		return notifier;
	}
	
}
