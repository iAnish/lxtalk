package com.mac.lxtalk;

import java.text.DateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mac.lxtalk.SQLEntries.ConversationEntry;

public class ArchiveActivity extends ListActivity {

	private static final String TAG="ArchiveActivity";
	
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archive);
		
		listView=this.getListView();
		
		Bundle extras=this.getIntent().getExtras();
		String contact=extras.getString("contact");
		if(contact==null){
			Log.e(TAG, "No extras given");
			this.finish();
		}
		
		this.setTitle("Conversations with "+contact);
		
		TextView textEmpty=(TextView)this.findViewById(android.R.id.empty);
		textEmpty.setText("No entries for "+contact);	
		
		SharedPreferences sharedPrefs=PreferenceManager.getDefaultSharedPreferences(this);
		String account=sharedPrefs.getString("loggedAs", "");

		ArchiveLoader loader=new ArchiveLoader();
		loader.execute(account, contact);
		
		listView.setOnItemClickListener((new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				
				Cursor c=(Cursor)listView.getItemAtPosition(position);
				
				int conversationId=c.getInt(c.getColumnIndex(ConversationEntry._ID));
				int date=c.getInt(c.getColumnIndex(ConversationEntry.COLUMN_DATE));
				String contact=c.getString(c.getColumnIndex(ConversationEntry.COLUMN_CONTACT));
				
				Intent i=new Intent(ArchiveActivity.this, ArchiveEntryActivity.class);
				i.putExtra("conversationId", conversationId);
				i.putExtra("contact", contact);
				i.putExtra("date", date);
				ArchiveActivity.this.startActivity(i);
			}
		}));
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.archive, menu);
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
	
	private class ArchiveLoader extends AsyncTask<String, Void, Cursor>{

		ProgressDialog dialog=new ProgressDialog(ArchiveActivity.this);
		
		@Override
		protected Cursor doInBackground(String... arg) {
			String account=arg[0];
			String contact=arg[1];
			
			SQLArchiver archiver=(SQLArchiver)((MyApplication)ArchiveActivity.this.getApplication()).getArchiver();
			Cursor c=archiver.fetchConversations(account, contact);
			
			return c;
		}
	
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Cursor c) {
			dialog.dismiss();

			SimpleCursorAdapter adapter=new SimpleCursorAdapter(ArchiveActivity.this, R.layout.archive_row, c, 
					new String[]{ConversationEntry.COLUMN_CONTACT, ConversationEntry.COLUMN_DATE}, new int[]{R.id.contact, R.id.date}, 0);
			
			adapter.setViewBinder(new ViewBinder(){
				
				@Override
				public boolean setViewValue(View view, Cursor cursor, int column) {
					int dateColumn=cursor.getColumnIndex(ConversationEntry.COLUMN_DATE);
					if(column==dateColumn){
						TextView text=(TextView)view;
						
						Date date=new Date(cursor.getLong(dateColumn));
						DateFormat dateFormat=DateFormat.getDateInstance();
						
						String dateStr=dateFormat.format(date);
						text.setText(dateStr);
						
						return true;
					}
					return false;
				}
				
			});
			
			ArchiveActivity.this.setListAdapter(adapter);

			ArchiveActivity.this.startManagingCursor(c);
			
			super.onPostExecute(c);
		}
	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			dialog.setTitle("Loading conversations...");
			dialog.setIndeterminate(true);
			dialog.show();
		}
	}
}
