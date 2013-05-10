package com.mac.lxtalk;

import java.util.ArrayList;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mac.lxtalk.MessageExtended.Direction;


public class ConversationManager{
	private static final String TAG="ConversationManager";
	
	private final ArrayList<Conversation> conversations=new ArrayList<Conversation>();
	
	private final Context ctx;
	private final SharedPreferences sharedPrefs;
	
	public ConversationManager(Context ctx){
		this.ctx=ctx;
		this.sharedPrefs=PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	public ArrayList<Conversation> getConversations(){
		return conversations;
	}
	
	public PacketListener getIncomingListener(){
		return incomingListener;
	}

	public PacketListener getOutcomingListener(){
		return outcomingListner;
	}
	
	public void clear(){
		conversations.clear();
	}
	
	public Conversation getConversation(String user){
		int index=conversations.indexOf(new Conversation(user));
		
		if(index==-1){
			Conversation newConversation=new Conversation(user);
			conversations.add(0, newConversation);
			return newConversation;
		}
		
		return conversations.get(index);
	}
	
	private PacketListener incomingListener=new PacketListener(){
		@Override
		public void processPacket(Packet p){
			if(!(p instanceof Message)){
				return;
			}

			MessageExtended msg=new MessageExtended((Message)p, Direction.INCOMING);
			processMessage(msg);
		}
	};

	private PacketListener outcomingListner=new PacketListener(){
		@Override
		public void processPacket(Packet p){
			if(!(p instanceof Message)){
				return;
			}

			MessageExtended msg=new MessageExtended((Message)p, Direction.OUTCOMING);
			processMessage(msg);
		}
	};
	
	
	//store message in a proper conversation
	//if it's an incoming message, mark it as unread and send notification
	private void processMessage(MessageExtended msg){
		if(msg.getType()==Type.error){
			Log.w(TAG, "Error message: "+msg.getError().getMessage());
			return;
		}
		
		String user;
		boolean markUnread=false;
		if(msg.getDirection()==Direction.INCOMING){
			user=msg.getFrom();
			markUnread=true;
		}
		else{
			user=msg.getTo();
		}
		
		Conversation conversation=this.getConversation(user);
		conversation.getMessages().add(msg);
		bringToFront(conversation);
		
		if(markUnread)
			conversation.setRead(false);
		

		boolean archivingEnabled=sharedPrefs.getBoolean("archiving", true);
		
		if(archivingEnabled){
			SQLArchiver archiver=((MyApplication)ctx.getApplicationContext()).getArchiver();
			archiver.insertMessage(conversation.getId(), msg);
			
			String currentAcc=sharedPrefs.getString("loggedAs", "");
			archiver.insertConversation(currentAcc, conversation);
		}
		
		if(msg.getDirection()==Direction.INCOMING){
			ctx.sendBroadcast(new Intent(XMPPService.MSG_NEW_MESSAGE));
			
			Notifier notifier=((MyApplication)ctx.getApplicationContext()).getNotifier();
			notifier.notifyNewMessage(StringUtils.parseBareAddress(msg.getFrom()));
		}
	}

	private void bringToFront(Conversation newConversation){
		if(conversations.contains(newConversation)){
			conversations.remove(newConversation);
			conversations.add(0, newConversation);
		}
		else{
			conversations.add(0, newConversation);
		}
	}
	
}
