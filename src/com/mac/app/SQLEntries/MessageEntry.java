package com.mac.app.SQLEntries;

import android.provider.BaseColumns;

public class MessageEntry implements BaseColumns{
	
	public static final String TABLE_NAME="messages";


	public static final String COLUMN_FROM="sender";
	public static final String COLUMN_TO="receiver";
	public static final String COLUMN_BODY="body";
	public static final String COLUMN_DIRECTION="dest";

	public static final String COLUMN_CONVERSATION="conversation";
	
	public static final String COLUMN_DATE="date";
	
}
