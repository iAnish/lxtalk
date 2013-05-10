package com.mac.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class Notifier {

	private static final int NOTIFICATION_NEW_MSG=0;
	
	private final Context ctx;
	
	private String activeChat=null;
	
	private final Bitmap largeIcon;
	
	private final NotificationManager notificationMgr;
	
	public Notifier(Context ctx) {
		this.ctx=ctx;
		
		this.largeIcon=BitmapFactory.decodeResource(ctx.getResources(), R.drawable.msg);
		
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
		builder.setSmallIcon(R.drawable.msg)
		.setLargeIcon(largeIcon)
		.setContentTitle("New message")
		.setContentText(from)
		.setContentInfo("info")
		.setAutoCancel(true);
		
		TaskStackBuilder stackBuilder=TaskStackBuilder.create(ctx);
		stackBuilder.addParentStack(ChatActivity.class);
		
		Intent i=new Intent(ctx, ChatActivity.class);
		i.putExtra("contact", from);
		stackBuilder.addNextIntent(i);
		
		PendingIntent pendingIntent=stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		
		notificationMgr.notify(from, NOTIFICATION_NEW_MSG, builder.build());
	}

}
