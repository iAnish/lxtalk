package com.mac.lxtalk;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mac.lxtalk.XMPPService.LocalBinder;

public class ChatActivity extends ListActivity {

	private static final String TAG="ChatActivity";
	
	private XMPPService service=null;
	
	private EditText msgView;
	private Button sendButton;
	private ImageView statusIcon;
	
	private Conversation conversation;
	private String contact;
	private Roster roster;
	
	private Notifier notifier;
	
	private static final IntentFilter msgFilter=new IntentFilter();
	private static final IntentFilter presenceFilter=new IntentFilter();
	
	private final BroadcastReceiver msgReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context ctx, Intent intent) {
			updateConversation();
		}
		
	};
	
	private final BroadcastReceiver presenceReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context ctx, Intent intent) {
			updateIcon();
		}
		
	};
	
	private final ServiceConnection mServiceConnection=new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			service=((LocalBinder)binder).getService();
			
			roster=service.getRoster();
			
			sendButton.setClickable(true);
			
			conversation=service.getConversationManager().getConversation(contact);
			
			
			ListAdapter adapter=new MessageAdapter(ChatActivity.this, conversation.getMessages());
			ChatActivity.this.setListAdapter(adapter);

			updateIcon();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service=null;
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		msgFilter.addAction(XMPPService.MSG_NEW_MESSAGE);
		presenceFilter.addAction(XMPPService.MSG_PRESENCE_UPDATE);
		
		msgView=(EditText)this.findViewById(R.id.msgEdit);
		sendButton=(Button)this.findViewById(R.id.sendButton);
		statusIcon=(ImageView)this.findViewById(R.id.status_icon);
		
		sendButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String msgText=msgView.getText().toString();
				msgView.setText("");
			
				
				try{
					service.sendMessage(contact, msgText);
				}
				catch(Exception e){
					Toast.makeText(ChatActivity.this, "Could not send message: " +e.getMessage(), Toast.LENGTH_SHORT).show();
					Log.e(TAG, "Could not send message: "+e.getMessage());
					
				}
				
				updateConversation();
			}
		});
		
		this.getListView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		
		Bundle extras=this.getIntent().getExtras();
		contact=extras.getString("contact");
		if(contact==null){
			Log.e(TAG, "No user specified!");
			this.finish();
		}

		this.setTitle(StringUtils.parseBareAddress(contact));
		
		notifier=((MyApplication)getApplicationContext()).getNotifier();
		
		TextView title=(TextView)this.findViewById(R.id.title);
		title.setText("Conversation with "+StringUtils.parseName(contact));
		
	}
	
	@Override
	protected void onStart() {

		Intent intent=new Intent(this, XMPPService.class);
		
		this.bindService(intent, mServiceConnection, 0);
		
		this.registerReceiver(msgReceiver, msgFilter);
		this.registerReceiver(presenceReceiver, presenceFilter);
		
		notifier.setActiveChat(StringUtils.parseBareAddress(contact));
		
		super.onStart();
	}
	
	
	
	@Override
	protected void onPause() {
		if(conversation!=null){
			conversation.setRead(true);
		}
		
		super.onPause();
	}

	@Override
	protected void onStop() {
		notifier.setActiveChat(null);

		this.unregisterReceiver(msgReceiver);
		this.unregisterReceiver(presenceReceiver);
		
		this.unbindService(mServiceConnection);

		super.onStop();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case R.id.action_settings:
			this.startActivity(new Intent(this, SettingsActivity.class));
			break;
		}
		
		
		return true;
	}
	
	private void updateIcon(){

		Presence contactPresence=roster.getPresence(contact);
		if(contactPresence==null){
			Log.e(TAG, "Could not obtain contact's presence");
			return;
		}

		statusIcon.setImageResource(Utils.presenceDrawable(contactPresence));
	}
	
	private void updateConversation(){		
		((BaseAdapter)this.getListAdapter()).notifyDataSetChanged();
	}

}
