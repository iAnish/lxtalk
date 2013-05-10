package com.mac.app;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends Activity  {
	private static final String TAG="MainActivity";
	
	private StatusSpinner mStatusSpinner;
	private ExpandableListView mContactsView;
	private ListView mConversationsView;
	private Button mSettingsButton;
	
	
	private SharedPreferences sharedPrefs;
	
	private XMPPService mService=null;
	
	private ServiceConnection mServiceConnection;
	
	private static final IntentFilter mConnectionEventFilter=new IntentFilter();
	private static final IntentFilter mPresenceFilter=new IntentFilter();
	private static final IntentFilter mNewMessageFilter=new IntentFilter();
	
	private final BroadcastReceiver mConnectionEventReceiver=new BroadcastReceiver(){
		
		public void onReceive(Context ctx, Intent intent){
			
			if(intent.getAction().equals(XMPPService.MSG_CONNECTION_FAILED)){
				mStatusSpinner.setSelectionFixed(StatusSpinner.STATUS_POS_OFFLINE);
				Toast.makeText(MainActivity.this, "Connection failed: "+intent.getStringExtra("exception_msg"), Toast.LENGTH_SHORT).show();
			}
			
			else if(intent.getAction().equals(XMPPService.MSG_LOGIN_FAILED)){
				mStatusSpinner.setSelectionFixed(StatusSpinner.STATUS_POS_OFFLINE);
				Toast.makeText(MainActivity.this, "Authentication failed: "+intent.getStringExtra("exception_msg"), Toast.LENGTH_SHORT).show();
			}
			
			else if(intent.getAction().equals(XMPPService.MSG_LOGIN_SUCCEEDED)){
				Toast.makeText(MainActivity.this, "Login OK!", Toast.LENGTH_SHORT).show();
				
				
				int spinnerPos=Utils.presenceToPosition(mService.getPresence());
				mStatusSpinner.setSelectionFixed(spinnerPos);
				
				sharedPrefs.edit().putString("loggedAs", intent.getStringExtra("account")).commit();

		        populateRoster();
			}
			else if(intent.getAction().equals(XMPPService.MSG_CONNECTION_CLOSED) || intent.getAction().equals(XMPPService.MSG_CONNECTION_CLOSED_ERROR)){
				Toast.makeText(MainActivity.this, "Connection closed!", Toast.LENGTH_SHORT).show();
				mStatusSpinner.setSelectionFixed(StatusSpinner.STATUS_POS_OFFLINE);

				((RosterAdapter)mContactsView.getExpandableListAdapter()).update();
			}
			
			MainActivity.this.setProgressBarIndeterminateVisibility(false);
		}
	};
	
	private final BroadcastReceiver mPresenceUpdateReceiver=new BroadcastReceiver(){
		public void onReceive(Context ctx, Intent intent){
			((RosterAdapter)mContactsView.getExpandableListAdapter()).update();
		}
	};
	
	private final BroadcastReceiver mNewMessageReceiver=new BroadcastReceiver(){
		public void onReceive(Context ctx, Intent intent){

			((ConversationListAdapter)mConversationsView.getAdapter()).notifyDataSetChanged();
		}
	};
	
	private void populateRoster(){
		
		Roster roster=mService.getRoster();
		RosterAdapter rosterAdapter=new RosterAdapter(this, roster);

		mContactsView.setAdapter(rosterAdapter);
		
		expandGroups();
	}
	
	private void setupStatusSpinner(){		
        mStatusSpinner=(StatusSpinner)this.findViewById(R.id.status_spinner);
        mStatusSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				
				Log.i("SELECTED", "SELECTED");
				
				if(mService==null){
					//TODO
					return;
				}
				
				if(position==StatusSpinner.STATUS_POS_OFFLINE){
					mService.disconnectFromServer();
					return;
				}
				
				Presence presence=Utils.positionToPresence(position);
				
				if(position!=StatusSpinner.STATUS_POS_OFFLINE && !mService.isOnline()){
					mStatusSpinner.setSelectionFixed(StatusSpinner.STATUS_POS_OFFLINE);
			        setProgressBarIndeterminateVisibility(true);
			        
			        

					String accountName=sharedPrefs.getString("username", "");
					String password=sharedPrefs.getString("password", "");

					LoginData loginData=new LoginData(accountName, password, presence);
					
					mService.connectToServer(loginData);
					
					return;
				}
				
				mService.setPresence(presence);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
        	
        });
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        sharedPrefs=PreferenceManager.getDefaultSharedPreferences(this);
        
        
        setupStatusSpinner();
        
        mSettingsButton=(Button)this.findViewById(R.id.settingsButton);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.this.startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			}
		});
        
        mContactsView=(ExpandableListView)findViewById(R.id.contactList);
        this.registerForContextMenu(mContactsView);
        mContactsView.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				
				RosterEntry entry=(RosterEntry)mContactsView.getExpandableListAdapter().getChild(groupPosition, childPosition);

				Intent intent=new Intent(MainActivity.this, ChatActivity.class);
				intent.putExtra("contact", entry.getUser());
				
				MainActivity.this.startActivity(intent);
				
				return true;
			}
        	
        });
        
        mConversationsView=(ListView)findViewById(R.id.conversationList);
        
        mConversationsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> v, View view, int position,
					long id) {
				
				Conversation conversation=(Conversation)mConversationsView.getItemAtPosition(position);
				String user=conversation.getContact();
				
				Intent intent=new Intent(MainActivity.this, ChatActivity.class);
				intent.putExtra("contact", user);
				
				MainActivity.this.startActivity(intent);
			}
        	
		});
         
        
        mServiceConnection=new ServiceConnection(){
        	public void onServiceConnected(ComponentName name, IBinder service){
        		mService=((XMPPService.LocalBinder)service).getService();
        		Log.i(TAG, "XMPP service connected");
        		

        		ConversationListAdapter conversationAdapter=new ConversationListAdapter(MainActivity.this, mService.getConversationManager().getConversations());
        		mConversationsView.setAdapter(conversationAdapter);
        		
        		if(mService.isOnline()){
    		        populateRoster();
        		}
        		
        		int spinnerPosition=Utils.presenceToPosition(mService.getPresence());
        		mStatusSpinner.setSelectionFixed(spinnerPosition);
        	}
        	
        	public void onServiceDisconnected(ComponentName name){
        		mService=null;
        		Log.w(TAG, "XMPP service disconnected");
        	}
        };
        
        

		mConnectionEventFilter.addAction(XMPPService.MSG_CONNECTION_FAILED);
		mConnectionEventFilter.addAction(XMPPService.MSG_LOGIN_FAILED);
		mConnectionEventFilter.addAction(XMPPService.MSG_LOGIN_SUCCEEDED);
		mConnectionEventFilter.addAction(XMPPService.MSG_CONNECTION_CLOSED);
		mConnectionEventFilter.addAction(XMPPService.MSG_CONNECTION_CLOSED_ERROR);
		
		mPresenceFilter.addAction(XMPPService.MSG_PRESENCE_UPDATE);
		
		mNewMessageFilter.addAction(XMPPService.MSG_NEW_MESSAGE);
		
        this.startService(new Intent(this.getApplicationContext(), XMPPService.class));
        
        
        TabHost tabs=(TabHost)findViewById(R.id.tabhost);
        
        tabs.setup();
        
        TabHost.TabSpec spec=tabs.newTabSpec("contactList");
        
        spec.setContent(R.id.contactListContainer);
        spec.setIndicator("Contact list");
        tabs.addTab(spec);
        
        spec=tabs.newTabSpec("conversations");
        spec.setContent(R.id.conversationList);
        spec.setIndicator("Conversations");
        tabs.addTab(spec);
    }


    @Override
	protected void onStart() {
		super.onStart();
		
		this.bindService(new Intent(this,  XMPPService.class), mServiceConnection, BIND_AUTO_CREATE);
		
		this.registerReceiver(mConnectionEventReceiver, mConnectionEventFilter);
		this.registerReceiver(mPresenceUpdateReceiver, mPresenceFilter);
		this.registerReceiver(mNewMessageReceiver, mNewMessageFilter);
		
		if(sharedPrefs.getString("username", "").equals("") || 
				sharedPrefs.getString("password", "").equals("")){
			
			mContactsView.setVisibility(View.GONE);
			mSettingsButton.setVisibility(View.VISIBLE);
			mStatusSpinner.setVisibility(View.INVISIBLE);
		}
		else{
			mContactsView.setVisibility(View.VISIBLE);
			mSettingsButton.setVisibility(View.GONE);
			mStatusSpinner.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		if(mService!=null){
			this.unbindService(mServiceConnection);
		}
		
		this.unregisterReceiver(mConnectionEventReceiver);
		this.unregisterReceiver(mPresenceUpdateReceiver);
		this.unregisterReceiver(mNewMessageReceiver);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case R.id.action_archive:
			ExpandableListContextMenuInfo info=(ExpandableListContextMenuInfo)item.getMenuInfo();
			
			int group=ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child=ExpandableListView.getPackedPositionChild(info.packedPosition);
			RosterEntry entry=(RosterEntry)mContactsView.getExpandableListAdapter().getChild(group, child);
			
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
			RosterEntry entry=(RosterEntry)mContactsView.getExpandableListAdapter().getChild(group, child);
			
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
		case R.id.action_exit:
			exit();
			return true;
		}
		
		
		return super.onOptionsItemSelected(item);
	}
	
	private void exit(){
		mStatusSpinner.setSelection(StatusSpinner.STATUS_POS_OFFLINE);

		this.stopService(new Intent(this, XMPPService.class));
		
		this.finish();
	}
	
	private void expandGroups(){
		int c=mContactsView.getExpandableListAdapter().getGroupCount();
		for(int i=0;i<c;i++)
			mContactsView.expandGroup(i);
	}
}
