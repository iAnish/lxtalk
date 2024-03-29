package com.mac.lxtalk;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class XMPPService extends Service{
	private static final String TAG="XMPPService";
		
	public static final String MSG_CONNECTION_FAILED="com.mac.lxtalk.MSG_CONNECTION_FAILED";
	public static final String MSG_LOGIN_FAILED="com.mac.lxtalk.MSG_LOGIN_FAILED";
	public static final String MSG_LOGIN_SUCCEEDED="com.mac.lxtalk.MSG_LOGIN_SUCCEEDED";
	
	public static final String MSG_CONNECTION_CLOSED="com.mac.lxtalk.MSG_CONNECTION_CLOSED";
	public static final String MSG_CONNECTION_CLOSED_ERROR="com.mac.lxtalk.MSG_CONNECTION_CLOSED";

	public static final String MSG_PRESENCE_UPDATE="com.mac.lxtalk.MSG_PRESENCE_UPDATE";

	public static final String MSG_NEW_MESSAGE="com.mac.lxtalk.MSG_NEW_MESSAGE";
	
	private Presence currentPresence=new Presence(Type.unavailable);
	private LoginData currentLoginData=null;
	
	private ConnectionManager connectionManager;
	private ConversationManager conversationManager;
	
	private Notifier notifier;
	
	private IBinder binder=new LocalBinder();
	private ConnectorThread connThread=null;
	
	private ConnectionListener connectionListener=new ConnectionListener(){
		@Override
		public void connectionClosed() {
			Log.i(TAG, "Conn closed");
			
			Intent i=new Intent(MSG_CONNECTION_CLOSED);
			sendBroadcast(i);
			
			currentPresence=new Presence(Type.unavailable);
			
			notifier.notifyServiceRunning(false);
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			Log.w(TAG, "Conn closed on err "+e.getMessage());

			Intent i=new Intent(MSG_CONNECTION_CLOSED_ERROR);
			sendBroadcast(i);
			
			currentPresence=new Presence(Type.unavailable);

			notifier.notifyServiceRunning(false);
		}

		@Override
		public void reconnectingIn(int sec) {
			Log.i(TAG, "Reconnecting in "+sec);
		}

		@Override
		public void reconnectionFailed(Exception arg0) {
			Log.i(TAG, "Reconnection failed");
		}

		@Override
		public void reconnectionSuccessful() {
			Log.i(TAG, "Reconnection succ");
		}
	};

	private class ConnectorThread extends Thread{
		private final LoginData loginData;
		
		public ConnectorThread(LoginData loginData){
			super();
			this.loginData=loginData;
		}
		
		@Override
		public void run() {
			try{
				if(loginData.getUsername().length()==0 || loginData.getPassword().length()==0 || loginData.getServer().length()==0){
					throw new XMPPException("Incorrect login data");
				}
				
				connectionManager.connect(loginData.getServer());
					
			}catch(XMPPException e){
				Log.w(TAG, e);
				
				Intent i=new Intent(MSG_CONNECTION_FAILED);
				i.putExtra("exception_msg", e.getMessage());
				sendBroadcast(i);
				
				return;
			}
			
			connectionManager.addConnectionListener(connectionListener);
			
			connectionManager.addPacketListener(new PresenceListener(), new PacketTypeFilter(Presence.class));
			
			connectionManager.addPacketListener(conversationManager.getIncomingListener(), new PacketTypeFilter(Message.class));
			connectionManager.addPacketSendingListener(conversationManager.getOutcomingListener(), new PacketTypeFilter(Message.class));
			
			try{
				connectionManager.login(loginData.getUsername(), loginData.getPassword());
					
			}catch(Exception e){
				Log.w(TAG, e);
				
				Intent i=new Intent(MSG_LOGIN_FAILED);
				i.putExtra("exception_msg", e.getMessage());
				sendBroadcast(i);
				
				return;
			}
			
			Log.i(TAG, "Login OK");
			
			setPresence(loginData.getPresence());
			
			Intent i=new Intent(MSG_LOGIN_SUCCEEDED);
			i.putExtra("account", loginData.getAccountName());
			sendBroadcast(i);
			
			notifier.notifyServiceRunning(true);
		}
		
	}
	
	public Presence getPresence(){
		return currentPresence;
	}
	
	public ConversationManager getConversationManager(){
		return conversationManager;
	}

	private class PresenceListener implements PacketListener{
		@Override
		public void processPacket(Packet packet) {
			if(!(packet instanceof Presence))
				return;
			
			Presence p = (Presence)packet;
			Log.i(TAG, "NEW PACKET: "+p.getFrom());
			
			sendBroadcast(new Intent(MSG_PRESENCE_UPDATE));
		}
	}
	
	public Roster getRoster(){
		return connectionManager.getRoster();
	}
	
	public class LocalBinder extends Binder{
		public XMPPService getService(){
			return XMPPService.this;
		}
	}
	
	public void setPresence(final Presence presence){
		if(!connectionManager.isAuthenticated()){
			Log.e(TAG, "setPresence called when not authenticated");
			return;
		}
		
		new Thread(){
			@Override
			public void run(){
				connectionManager.setPresence(presence);			
			}
		}.start();
		
		currentPresence=presence;
	}
	
	public void sendMessage(String to, String body) throws Exception{

		if(!connectionManager.isAuthenticated()){
			throw new Exception("Not connected");
		}
		
		Message msg=new Message();
		msg.setTo(to);
		msg.setBody(body);
		
		connectionManager.sendMessage(msg);
	}
	
	public void connectToServer(final LoginData loginData){
		
		if(connThread!=null && connThread.isAlive()){
			
			//if connecting in progress, wait and set presence when connected
			new Thread(){
				@Override
				public void run(){
					try{
						connThread.join();
					}catch(InterruptedException e){
						Log.e(TAG, "exception", e);
					}
					
					setPresence(loginData.getPresence());
				}
			}.start();
			
			return;
		}
		
		if(currentLoginData!=null && !loginData.getAccountName().equals(currentLoginData.getAccountName())){
			conversationManager.clear();
		}

		currentLoginData=loginData;
		
		
		connThread=new ConnectorThread(loginData);
		connThread.start();
	}
	
	public boolean isOnline(){		
		return connectionManager.isAuthenticated();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		conversationManager=new ConversationManager(XMPPService.this);
		
		ConnectionManager.init(this.getApplicationContext());
		connectionManager=new ConnectionManager();
		
		notifier=(Notifier)((MyApplication)this.getApplicationContext()).getNotifier();
		notifier.notifyServiceRunning(false);
	}

	@Override
	public void onDestroy() {	
		connectionManager.disconnect();
		
		notifier.cancelAll();

		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public void disconnectFromServer(){
		
		if(connThread!=null && connThread.isAlive()){
			
			//if connecting is in progress, wait and close connection
			new Thread(){
				@Override
				public void run(){
					try{
						connThread.join();
					}catch(InterruptedException e){
						Log.e(TAG, e.getMessage());
					}

					connectionManager.disconnect();
				}
			}.start();
		}
		else{
			connectionManager.disconnect();	
		}
		
	}

}
