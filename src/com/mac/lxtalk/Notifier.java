package com.mac.lxtalk;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class Notifier {

	private static final int NOTIFICATION_NEW_MSG=0;
	private static final int NOTIFICATION_RUNNING=1;
	
	private final Context ctx;
	private final SharedPreferences sharedPrefs;
	
	private String activeChat=null;
	
	private final Bitmap largeMsgIcon;
	private final Bitmap largeRunningIcon;
	
	private final NotificationManager notificationMgr;
	
	public Notifier(Context ctx) {
		this.ctx=ctx;
		
		this.sharedPrefs=PreferenceManager.getDefaultSharedPreferences(ctx);
		
		this.largeMsgIcon=BitmapFactory.decodeResource(ctx.getResources(), R.drawable.msg_new);
		this.largeRunningIcon=BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_launcher);
		
		this.notificationMgr=(NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void setActiveChat(String activeChat){
		this.activeChat=activeChat;
		
		if(activeChat!=null)
			notificationMgr.cancel(activeChat, NOTIFICATION_NEW_MSG);
	}
	
	public void notifyNewMessage(String from){
		if(from.equals(activeChat))
			return;
		
		NotificationCompat.Builder builder=new NotificationCompat.Builder(ctx);
		builder.setLargeIcon(largeMsgIcon)
		.setSmallIcon(R.drawable.msg_new)
		.setContentTitle("New message")
		.setContentText(from)
		.setAutoCancel(true);
		
		if(sharedPrefs.getBoolean("vibrate", true)){
			builder.setVibrate(new long[]{200});
		}
		else{
			builder.setVibrate(new long[]{0});
		}
		
		TaskStackBuilder stackBuilder=TaskStackBuilder.create(ctx);
		stackBuilder.addParentStack(ChatActivity.class);
		
		Intent i=new Intent(ctx, ChatActivity.class);
		i.putExtra("contact", from);
		stackBuilder.addNextIntent(i);
		
		PendingIntent pendingIntent=stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		
		notificationMgr.notify(from, NOTIFICATION_NEW_MSG, builder.build());
	}

	public void notifyServiceRunning(boolean online){
		NotificationCompat.Builder builder=new NotificationCompat.Builder(ctx);
		builder.setLargeIcon(largeRunningIcon)
		.setContentTitle("LXTalk")
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentText(online?"Online":"Offline")
		.setOngoing(true);
		
		TaskStackBuilder stackBuilder=TaskStackBuilder.create(ctx);
		stackBuilder.addParentStack(MainActivity.class);
		
		Intent i=new Intent(ctx, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		stackBuilder.addNextIntent(i);

		PendingIntent pendingIntent=stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		
		notificationMgr.notify(NOTIFICATION_RUNNING, builder.build());
	}
	
	public void cancelAll(){
		notificationMgr.cancelAll();
	}
}
