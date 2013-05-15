package com.mac.lxtalk;

import java.io.File;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;
import android.os.Build;
import android.util.Log;


public class ConnectionManager {
	private static final String TAG="ConnectionManager";
	
	private XMPPConnection connection=null;
	
	
	public ConnectionManager() {
	}
	
	public static void init(Context ctx){
		SmackAndroid.init(ctx);

		System.setProperty("smack.debugEnabled", "true");
		XMPPConnection.DEBUG_ENABLED = true;
	}
	
	
	public void connect(String server) throws XMPPException{
		if(connection!=null)
			connection.disconnect();
		
		
		AndroidConnectionConfiguration config=new AndroidConnectionConfiguration(server, 5222);
		
		fixConfig(config);
		config.setSendPresence(false);
		config.setRosterLoadedAtLogin(true);
		config.setReconnectionAllowed(false);
		
		connection=new XMPPConnection(config);
		connection.connect();
		
		if(!connection.isConnected())
			throw new XMPPException("Unknown connection error");
	}
	
	
	public void login(String username, String password) throws Exception{
		if(connection==null || !connection.isConnected()){
			Log.e(TAG, "login called when not connected");
			return;
		}
		
		connection.login(username, password);
		
		if(!connection.isAuthenticated())
			throw new IllegalStateException("Unknown login error");
	}
	
	
	public void setPresence(Presence presence){
		if(connection==null || !connection.isAuthenticated()){
			Log.e(TAG, "setPresence called when not authenticated");
			return;
		}

		connection.sendPacket(presence);
	}
	
	public void sendMessage(Message msg) throws XMPPException{
		Chat chat=connection.getChatManager().createChat(msg.getTo(), null);
		chat.sendMessage(msg);
	}
	
	public void disconnect(){
		if(connection==null)
			return;
		
		connection.disconnect();
	}
	
	public Roster getRoster(){
		if(connection==null)
			return null;
		
		return connection.getRoster();
	}
	
	public void addConnectionListener(ConnectionListener listener){
		if(connection!=null)
			connection.addConnectionListener(listener);
		
	}
	
	public void addPacketListener(PacketListener listener, PacketFilter filter){
		if(connection!=null)
			connection.addPacketListener(listener, filter);
		
	}
	
	public void addPacketSendingListener(PacketListener listener, PacketFilter filter){
		if(connection!=null)
			connection.addPacketSendingListener(listener, filter);
		
	}
	
	public boolean isAuthenticated(){
		return connection!=null && connection.isAuthenticated();
	}
	
	private static void fixConfig(AndroidConnectionConfiguration connectionConfiguration){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    connectionConfiguration.setTruststoreType("AndroidCAStore");
		    connectionConfiguration.setTruststorePassword(null);
		    connectionConfiguration.setTruststorePath(null);
		} else {
		    connectionConfiguration.setTruststoreType("BKS");
		    String path = System.getProperty("javax.net.ssl.trustStore");
		    if (path == null)
		        path = System.getProperty("java.home") + File.separator + "etc"
		            + File.separator + "security" + File.separator
		            + "cacerts.bks";
		    connectionConfiguration.setTruststorePath(path);
		}
	}
}
