package com.mac.lxtalk;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.packet.Message;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.mac.lxtalk.SQLEntries.ConversationEntry;
import com.mac.lxtalk.SQLEntries.MessageEntry;

public class ArchiveEntryActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archive_entry);
		
		int conversationId=this.getIntent().getIntExtra("conversationId", 0);
		
		this.setTitle("Archive");
		
		ConversationLoader loader=new ConversationLoader();
		loader.execute(conversationId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.archive_entry, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			this.startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class ConversationLoader extends AsyncTask<Integer, Void, List<MessageExtended>>{

		private ProgressDialog dialog;
		private Date date;
		private String contact;
		
		@Override
		protected List<MessageExtended> doInBackground(Integer... arg) {
			int conversationId=arg[0];
			
			SQLArchiver archiver=(SQLArchiver)((MyApplication)ArchiveEntryActivity.this.getApplicationContext()).getArchiver();
			Cursor cursor=archiver.fetchConversation(conversationId);
			if(!cursor.moveToFirst()){
				//TODO
			}
			contact=cursor.getString(cursor.getColumnIndex(ConversationEntry.COLUMN_CONTACT));
			date=new Date(cursor.getLong(cursor.getColumnIndex(ConversationEntry.COLUMN_DATE)));
			
			cursor.close();
			
			
			cursor=archiver.fetchMessages(conversationId);
			
			List<MessageExtended> list=new ArrayList<MessageExtended>();
			
			while(cursor.moveToNext()){
				Message msg=new Message();
				msg.setBody(cursor.getString(cursor.getColumnIndex(MessageEntry.COLUMN_BODY)));
				msg.setFrom(cursor.getString(cursor.getColumnIndex(MessageEntry.COLUMN_FROM)));
				msg.setTo(cursor.getString(cursor.getColumnIndex(MessageEntry.COLUMN_TO)));
				
				MessageExtended.Direction direction=MessageExtended.Direction.values()[cursor.getInt(cursor.getColumnIndex(MessageEntry.COLUMN_DIRECTION))];
				Date msgDate=new Date(cursor.getLong(cursor.getColumnIndex(MessageEntry.COLUMN_DATE)));
				
				MessageExtended msgExtended=new MessageExtended(msg, direction, msgDate);
				
				list.add(msgExtended);
			}
			
			cursor.close();
			
			return list;
		}

		@Override
		protected void onPostExecute(List<MessageExtended> result) {
			DateFormat format=DateFormat.getDateInstance();
			setTitle("Conversation with "+contact+", "+format.format(date));
			
			MessageAdapter adapter=new MessageAdapter(ArchiveEntryActivity.this, result);
			ArchiveEntryActivity.this.setListAdapter(adapter);
			dialog.dismiss();
			
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			dialog=new ProgressDialog(ArchiveEntryActivity.this);
			dialog.setTitle("Loading messages...");
			dialog.setIndeterminate(true);
			dialog.show();
		}
		
	}
	
}
