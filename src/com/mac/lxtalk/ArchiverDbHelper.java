package com.mac.lxtalk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mac.lxtalk.SQLEntries.ConversationEntry;
import com.mac.lxtalk.SQLEntries.MessageEntry;

public class ArchiverDbHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION=1;
	private static final String DATABASE_NAME="archive.db";
	
	private static final String CREATE_TABLE_MESSAGES="CREATE TABLE "+MessageEntry.TABLE_NAME+" ("+
			MessageEntry._ID+" INTEGER PRIMARY KEY, "+
			MessageEntry.COLUMN_FROM+" TEXT, "+
			MessageEntry.COLUMN_TO+" TEXT, "+
			MessageEntry.COLUMN_BODY+" TEXT, "+
			MessageEntry.COLUMN_DIRECTION+" INTEGER, "+
			MessageEntry.COLUMN_CONVERSATION+" INTEGER, "+
			MessageEntry.COLUMN_DATE+" INTEGER)";
	
	private static final String CREATE_TABLE_CONVERSATIONS="CREATE TABLE "+ConversationEntry.TABLE_NAME+" ("+
			ConversationEntry._ID+" INTEGER UNIQUE, "+
			ConversationEntry.COLUMN_ACCOUNT+" TEXT, "+
			ConversationEntry.COLUMN_CONTACT+" TEXT, "+
			ConversationEntry.COLUMN_DATE+" INTEGER)";
	
	private static final String DELETE_MESSAGES="DROP TABLE IF EXIST "+MessageEntry.TABLE_NAME;
	private static final String DELETE_CONVERSATIONS="DROP TABLE IF EXIST "+ConversationEntry.TABLE_NAME;
	
	public ArchiverDbHelper(Context ctx){
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_MESSAGES);
		db.execSQL(CREATE_TABLE_CONVERSATIONS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL(DELETE_MESSAGES);
		db.execSQL(DELETE_CONVERSATIONS);
		onCreate(db);
	}

}
