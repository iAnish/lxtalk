package com.mac.lxtalk;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends Activity  {
	private static final String TAG="MainActivity";
	
	private StatusSpinner statusSpinner;
	private ExpandableListView contactsView;
	private ListView conversationsView;
	private Button settingsButton;
	
	private SharedPreferences sharedPrefs;
	
	private XMPPService xmppService=null;
	
	private ServiceConnection serviceConnection;
	
	private String description="";
	
	private static final IntentFilter connectionEventFilter=new IntentFilter();
	private static final IntentFilter presenceFilter=new IntentFilter();
	private static final IntentFilter newMessageFilter=new IntentFilter();
	
	private final BroadcastReceiver connectionEventReceiver=new BroadcastReceiver(){
		
		public void onReceive(Context ctx, Intent intent){
			
			if(intent.getAction().equals(XMPPService.MSG_CONNECTION_FAILED)){
				statusSpinner.setSelectionFixed(StatusSpinner.STATUS_POS_OFFLINE);
				Toast.makeText(MainActivity.this, "Connection failed: "+intent.getStringExtra("exception_msg"), Toast.LENGTH_SHORT).show();

				MainActivity.this.setProgressBarIndeterminateVisibility(false);
			}
			
			else if(intent.getAction().equals(XMPPService.MSG_LOGIN_FAILED)){
				statusSpinner.setSelectionFixed(StatusSpinner.STATUS_POS_OFFLINE);
				Toast.makeText(MainActivity.this, "Authentication failed: "+intent.getStringExtra("exception_msg"), Toast.LENGTH_SHORT).show();

				MainActivity.this.setProgressBarIndeterminateVisibility(false);
			}
			
			else if(intent.getAction().equals(XMPPService.MSG_LOGIN_SUCCEEDED)){
				Toast.makeText(MainActivity.this, "Login OK!", Toast.LENGTH_SHORT).show();
				
				int spinnerPos=Utils.presenceToPosition(xmppService.getPresence());
				statusSpinner.setSelectionFixed(spinnerPos);
				
				String loggedAs=intent.getStringExtra("account");
				sharedPrefs.edit().putString("loggedAs", loggedAs).commit();
				
		        populateRoster();
				MainActivity.this.setProgressBarIndeterminateVisibility(false);
			}
			else if(intent.getAction().equals(XMPPService.MSG_CONNECTION_CLOSED) || intent.getAction().equals(XMPPService.MSG_CONNECTION_CLOSED_ERROR)){
				Log.i(TAG, "Connection closed");
				statusSpinner.setSelectionFixed(StatusSpinner.STATUS_POS_OFFLINE);

			}
			
			updateRoster();
			updateConversations();
		}
	};
	
	private final BroadcastReceiver presenceUpdateReceiver=new BroadcastReceiver(){
		public void onReceive(Context ctx, Intent intent){
			updateRoster();
		}
	};
	
	private final BroadcastReceiver newMessageReceiver=new BroadcastReceiver(){
		public void onReceive(Context ctx, Intent intent){
			updateConversations();
		}
	};
	
	private void populateRoster(){
		
		Roster roster=xmppService.getRoster();
		RosterAdapter rosterAdapter=new RosterAdapter(this, roster);

		contactsView.setAdapter(rosterAdapter);
		
		expandGroups();
	}

	private void updateRoster(){
		if(contactsView.getExpandableListAdapter()!=null)
			((RosterAdapter)contactsView.getExpandableListAdapter()).update();
	}
	
	private void updateConversations(){
		((ConversationListAdapter)conversationsView.getAdapter()).notifyDataSetChanged();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        sharedPrefs=PreferenceManager.getDefaultSharedPreferences(this);

        this.setTitle("Welcome to LXTalk!");
        
        setupStatusSpinner();
        
        settingsButton=(Button)this.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.this.startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			}
		});
        
        contactsView=(ExpandableListView)findViewById(R.id.contactList);
        this.registerForContextMenu(contactsView);
        contactsView.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				
				RosterEntry entry=(RosterEntry)contactsView.getExpandableListAdapter().getChild(groupPosition, childPosition);

				Intent intent=new Intent(MainActivity.this, ChatActivity.class);
				intent.putExtra("contact", entry.getUser());
				
				MainActivity.this.startActivity(intent);
				
				return true;
			}
        	
        });
        
        conversationsView=(ListView)findViewById(R.id.conversationList);
        
        conversationsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> v, View view, int position,
					long id) {
				
				Conversation conversation=(Conversation)conversationsView.getItemAtPosition(position);
				String user=conversation.getContact();
				
				Intent intent=new Intent(MainActivity.this, ChatActivity.class);
				intent.putExtra("contact", user);
				
				MainActivity.this.startActivity(intent);
			}
        	
		});
         
        
        serviceConnection=new ServiceConnection(){
        	public void onServiceConnected(ComponentName name, IBinder service){
        		xmppService=((XMPPService.LocalBinder)service).getService();
        		Log.i(TAG, "XMPP service connected");
        		

        		ConversationListAdapter conversationAdapter=new ConversationListAdapter(MainActivity.this, xmppService.getConversationManager().getConversations());
        		conversationsView.setAdapter(conversationAdapter);
        		
        		if(xmppService.isOnline()){
    		        populateRoster();
        		}
        		
        		int spinnerPosition=Utils.presenceToPosition(xmppService.getPresence());
        		statusSpinner.setSelectionFixed(spinnerPosition);
        	}
        	
        	public void onServiceDisconnected(ComponentName name){
        		xmppService=null;
        		Log.w(TAG, "XMPP service disconnected");
        	}
        };
        
        
        this.findViewById(R.id.desc_button).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Insert status description");
				builder.setIcon(R.drawable.description);
				
				LayoutInflater inflater=(LayoutInflater)MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout=(View)inflater.inflate(R.layout.desc_dialog, null, false);
				final EditText text=(EditText)layout.findViewById(R.id.description);
				builder.setView(layout);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.description=text.getText().toString();
						
						if(xmppService==null || xmppService.isOnline()==false){
							return;
						}
						
						Presence presence=Utils.positionToPresence(statusSpinner.getSelectedItemPosition());
						if(description.length()!=0)
							presence.setStatus(description);
						
						xmppService.setPresence(presence);
					}
					
				});
				
				builder.setNegativeButton("Cancel", null);
				
				builder.show();
			}
		});
        
        

		connectionEventFilter.addAction(XMPPService.MSG_CONNECTION_FAILED);
		connectionEventFilter.addAction(XMPPService.MSG_LOGIN_FAILED);
		connectionEventFilter.addAction(XMPPService.MSG_LOGIN_SUCCEEDED);
		connectionEventFilter.addAction(XMPPService.MSG_CONNECTION_CLOSED);
		connectionEventFilter.addAction(XMPPService.MSG_CONNECTION_CLOSED_ERROR);
		
		presenceFilter.addAction(XMPPService.MSG_PRESENCE_UPDATE);
		
		newMessageFilter.addAction(XMPPService.MSG_NEW_MESSAGE);
		
        this.startService(new Intent(this.getApplicationContext(), XMPPService.class));
        
        
        TabHost tabs=(TabHost)findViewById(R.id.tabhost);
        
        tabs.setup();
        
        TabHost.TabSpec spec=tabs.newTabSpec("contactList");
        
        spec.setContent(R.id.contactListContainer);
        spec.setIndicator("Contact list", this.getResources().getDrawable(R.drawable.contacts));
        tabs.addTab(spec);
        
        spec=tabs.newTabSpec("conversations");
        spec.setContent(R.id.conversationList);
        spec.setIndicator("Conversations", this.getResources().getDrawable(R.drawable.conversations));
        tabs.addTab(spec);
    }


    @Override
	protected void onStart() {
		super.onStart();
		
		this.bindService(new Intent(this,  XMPPService.class), serviceConnection, BIND_AUTO_CREATE);
		
		this.registerReceiver(connectionEventReceiver, connectionEventFilter);
		this.registerReceiver(presenceUpdateReceiver, presenceFilter);
		this.registerReceiver(newMessageReceiver, newMessageFilter);
		
		if(sharedPrefs.getString("username", "").equals("") || 
				sharedPrefs.getString("password", "").equals("")){
			
			contactsView.setVisibility(View.GONE);
			settingsButton.setVisibility(View.VISIBLE);
			statusSpinner.setEnabled(false);
		}
		else{
			contactsView.setVisibility(View.VISIBLE);
			settingsButton.setVisibility(View.GONE);
			statusSpinner.setEnabled(true);
		}
	}

	@Override
	protected void onStop() {

		this.unregisterReceiver(newMessageReceiver);
		this.unregisterReceiver(presenceUpdateReceiver);
		this.unregisterReceiver(connectionEventReceiver);

		if(xmppService!=null){
			this.unbindService(serviceConnection);
		}
		
		super.onStop();
	}

	
	private void setupStatusSpinner(){		
        statusSpinner=(StatusSpinner)this.findViewById(R.id.status_spinner);
        statusSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				
				if(xmppService==null){
					statusSpinner.setSelectionFixed(StatusSpinner.STATUS_POS_OFFLINE);
					return;
				}
				
				if(position==StatusSpinner.STATUS_POS_OFFLINE){
					xmppService.disconnectFromServer();
					return;
				}
				
				Presence presence=Utils.positionToPresence(position);
				if(description.length()!=0){
					presence.setStatus(description);
				}
				
				if(position!=StatusSpinner.STATUS_POS_OFFLINE && !xmppService.isOnline()){
					statusSpinner.setSelectionFixed(StatusSpinner.STATUS_POS_OFFLINE);
			        setProgressBarIndeterminateVisibility(true);

					String accountName=sharedPrefs.getString("username", "");
					String password=sharedPrefs.getString("password", "");

					LoginData loginData=new LoginData(accountName, password, presence);
					
					xmppService.connectToServer(loginData);
					
					return;
				}
				
				xmppService.setPresence(presence);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
        	
        });
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case R.id.action_archive:
			ExpandableListContextMenuInfo info=(ExpandableListContextMenuInfo)item.getMenuInfo();
			
			int group=ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child=ExpandableListView.getPackedPositionChild(info.packedPosition);
			RosterEntry entry=(RosterEntry)contactsView.getExpandableListAdapter().getChild(group, child);
			
			Intent i=new Intent(this, ArchiveActivity.class);
			i.putExtra("contact", StringUtils.parseBareAddress(entry.getUser()));

			this.startActivity(i);
			
			return true;
		}
		
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		ExpandableListContextMenuInfo info=(ExpandableListContextMenuInfo)menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if(type==ExpandableListView.PACKED_POSITION_TYPE_CHILD){
			this.getMenuInflater().inflate(R.menu.contact_context_menu, menu);
			int group=ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child=ExpandableListView.getPackedPositionChild(info.packedPosition);
			RosterEntry entry=(RosterEntry)contactsView.getExpandableListAdapter().getChild(group, child);
			
			menu.setHeaderTitle( entry.getName()==null ? StringUtils.parseBareAddress(entry.getUser()) : entry.getName() );
		}
		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main, menu);
 
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case R.id.action_settings:
			this.startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_archive:
			this.startActivity(new Intent(this, ArchiveActivity.class));
			return true;
		case R.id.action_exit:
			exit();
			return true;
		}
		
		
		return super.onOptionsItemSelected(item);
	}
	
	private void exit(){
		statusSpinner.setSelection(StatusSpinner.STATUS_POS_OFFLINE);

		this.stopService(new Intent(this, XMPPService.class));
		
		this.finish();
	}
	
	private void expandGroups(){
		int c=contactsView.getExpandableListAdapter().getGroupCount();
		for(int i=0;i<c;i++)
			contactsView.expandGroup(i);
	}
}
