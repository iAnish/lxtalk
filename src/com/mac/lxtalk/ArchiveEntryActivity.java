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

import com.mac.lxtalk.SQLEntries.MessageEntry;

public class ArchiveEntryActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archive_entry);
		
		int conversationId=this.getIntent().getIntExtra("conversationId", 0);
		String contact=this.getIntent().getStringExtra("contact");
		Date date=new Date(this.getIntent().getIntExtra("date", 0));
		
		DateFormat format=DateFormat.getDateInstance();
		this.setTitle("Conversation with "+contact+", "+format.format(date));
		
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
		
		@Override
		protected List<MessageExtended> doInBackground(Integer... arg) {
			SQLArchiver archiver=(SQLArchiver)((MyApplication)ArchiveEntryActivity.this.getApplicationContext()).getArchiver();
			Cursor c=archiver.fetchMessages(arg[0]);
			
			List<MessageExtended> list=new ArrayList<MessageExtended>();
			
			while(c.moveToNext()){
				Message msg=new Message();
				msg.setBody(c.getString(c.getColumnIndex(MessageEntry.COLUMN_BODY)));
				msg.setFrom(c.getString(c.getColumnIndex(MessageEntry.COLUMN_FROM)));
				msg.setTo(c.getString(c.getColumnIndex(MessageEntry.COLUMN_TO)));
				
				MessageExtended.Direction direction=MessageExtended.Direction.values()[c.getInt(c.getColumnIndex(MessageEntry.COLUMN_DIRECTION))];
				Date date=new Date(c.getInt(c.getColumnIndex(MessageEntry.COLUMN_DATE)));
				
				MessageExtended msgExtended=new MessageExtended(msg, direction, date);
				
				list.add(msgExtended);
			}
			
			c.close();
			
			return list;
		}

		@Override
		protected void onPostExecute(List<MessageExtended> result) {
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
