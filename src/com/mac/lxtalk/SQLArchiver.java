package com.mac.lxtalk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mac.lxtalk.SQLEntries.ConversationEntry;
import com.mac.lxtalk.SQLEntries.MessageEntry;

public class SQLArchiver {
	
	private final SQLiteOpenHelper dbHelper;	
	
	public SQLArchiver(Context ctx){		
		dbHelper=new ArchiverDbHelper(ctx);
	}
	
	public void insertMessage(int conversation, MessageExtended msg){
		ContentValues values=new ContentValues();
		values.put(MessageEntry.COLUMN_FROM, msg.getFrom());
		values.put(MessageEntry.COLUMN_TO, msg.getTo());
		values.put(MessageEntry.COLUMN_BODY, msg.getBody());
		values.put(MessageEntry.COLUMN_DIRECTION, msg.getDirection().ordinal());
		values.put(MessageEntry.COLUMN_CONVERSATION, conversation);
		values.put(MessageEntry.COLUMN_DATE, msg.getDate().getTime());
		
		SQLiteDatabase db=dbHelper.getWritableDatabase();
		db.insert(MessageEntry.TABLE_NAME, null, values);
		db.close();
	}
	
	public void insertConversation(String accountName, Conversation conversation){
		ContentValues values=new ContentValues();
		values.put(ConversationEntry._ID, conversation.getId());
		values.put(ConversationEntry.COLUMN_ACCOUNT, accountName);
		values.put(ConversationEntry.COLUMN_CONTACT, conversation.getContact());
		values.put(ConversationEntry.COLUMN_DATE, conversation.getDate().getTime());
		
		SQLiteDatabase db=dbHelper.getWritableDatabase();
		db.replace(ConversationEntry.TABLE_NAME, null, values);
		db.close();
	}
	
	public Cursor fetchConversations(String accountName, String contact){
		SQLiteDatabase db=dbHelper.getReadableDatabase();
		
		String selection=ConversationEntry.COLUMN_ACCOUNT+" = '"+accountName+"'";
		
		if(contact!=null){
			selection+=" AND "+ConversationEntry.COLUMN_CONTACT+" = '"+contact+"'";
		}
		
		return db.query(ConversationEntry.TABLE_NAME, null, selection, null, null, null, ConversationEntry.COLUMN_DATE+" DESC");
	}
	
	public Cursor fetchConversation(int conversationId){
		SQLiteDatabase db=dbHelper.getReadableDatabase();
		
		String selection=ConversationEntry._ID+" = "+conversationId;
		
		return db.query(ConversationEntry.TABLE_NAME, null, selection, null, null, null, null);
	}
	
	public Cursor fetchMessages(int conversationId){
		SQLiteDatabase db=dbHelper.getReadableDatabase();
		
		String selection=MessageEntry.COLUMN_CONVERSATION+" = "+conversationId;
		
		return db.query(MessageEntry.TABLE_NAME, null, selection, null, null, null, MessageEntry.COLUMN_DATE+" ASC");
	}
}
