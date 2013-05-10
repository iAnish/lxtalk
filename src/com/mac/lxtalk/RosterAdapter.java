package com.mac.lxtalk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RosterAdapter extends BaseExpandableListAdapter{

	private final Roster roster;
	
	private final Context ctx;
	
	private final List<Group> groups=new ArrayList<Group>();
	
	private class Group implements Comparable<Group>{
		private final String name;
		private final List<RosterEntry> entries;
		
		public Group(String name, Collection<RosterEntry> entries){
			this.name=name;
			
			this.entries=new ArrayList<RosterEntry>(entries);
		}
		
		public Group(RosterGroup group){
			this.name=group.getName();
			
			entries=new ArrayList<RosterEntry>(group.getEntries());
		}
		
		public void sortEntries(){
			Collections.sort(entries, new EntriesComparator());
		}
		
		public String getName(){
			return name;
		}
		
		public RosterEntry get(int position){
			return entries.get(position);
		}
		
		public int getEntryCount(){
			return entries.size();
		}
		
		public int getOnlineCount(){
			int i=0;
			
			for(RosterEntry entry : entries){
				Presence p=roster.getPresence(entry.getUser());
				
				if(p!=null && p.getType()==Type.available)
					i++;
			}
			
			return i;
		}

		@Override
		public int compareTo(Group g2) {
			return name.compareTo(g2.getName());
		}
		
		private class EntriesComparator implements Comparator<RosterEntry>{
			@Override
			public int compare(RosterEntry e1, RosterEntry e2) {
				Presence p1=roster.getPresence(e1.getUser());
				Presence p2=roster.getPresence(e2.getUser());
				
				int diff=presenceValue(p2)-presenceValue(p1);
				
				return diff;
			}
			
			private int presenceValue(Presence p){
				int v=0;
				
				if(p==null)
					return v;
				
				if(p.getType()==Presence.Type.available){
					v=4;

					Presence.Mode presenceMode=p.getMode();
					if(presenceMode!=null){
						
						switch(presenceMode){
						case chat:
							v=5;
						break;
						case available:
							v=4;
						break;
						case away:
							v=3;
						break;
						case xa:
							v=2;
						break;
						case dnd:
							v=1;
						break;
						}
					}
				}
				
				return v;
			}
		}
	}
	
	public RosterAdapter(Context ctx, Roster roster) {
		super();
		
		this.roster=roster;
		this.ctx=ctx;
		
		for(RosterGroup group : roster.getGroups()){
			groups.add(new Group(group));
		}
		
		Collections.sort(groups);
		
		Group others=new Group("Others", roster.getUnfiledEntries());
		groups.add(others);
	}
	
	public void update(){
		for(Group group : groups){
			group.sortEntries();
		}
		
		this.notifyDataSetChanged();
	}

	@Override
	public RosterEntry getChild(int groupPosition, int childPosition) {
		return groups.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		RosterEntry entry=groups.get(groupPosition).get(childPosition);

		View layoutView=convertView;
		if(layoutView==null){
			LayoutInflater inflater=(LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layoutView=inflater.inflate(R.layout.contact_row, null, false);
		}
		
		TextView userName=(TextView)layoutView.findViewById(R.id.contact_name);
		
		if(entry.getName()==null || entry.getName().length()==0)
			userName.setText(entry.getUser());
		else
			userName.setText(entry.getName());
		

		Presence presence=roster.getPresence(entry.getUser());
		TextView userDesc=(TextView)layoutView.findViewById(R.id.contact_desc);
		
		if(presence.getStatus()==null || presence.getStatus().length()==0)
			userDesc.setVisibility(View.GONE);
		else
			userDesc.setText(presence.getStatus());
		
		
		ImageView img=(ImageView)layoutView.findViewById(R.id.contact_icon);

		
		img.setImageResource(Utils.presenceDrawable(presence));
		
		return layoutView;
		
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return groups.get(groupPosition).getEntryCount();
	}

	@Override
	public Object getGroup(int position) {
		return groups.get(position);
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public long getGroupId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		
		View layoutView=convertView;
		if(layoutView==null){
			LayoutInflater inflater=(LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layoutView=inflater.inflate(R.layout.group_row, null, false);
		}
		
		Group group=groups.get(groupPosition);
		
		TextView textName=(TextView)layoutView.findViewById(R.id.text1);
		textName.setText(group.getName());
		
		
		int entryCount=group.getEntryCount();
		int onlineCount=group.getOnlineCount();
		
		TextView textCount=(TextView)layoutView.findViewById(R.id.text2);
		textCount.setText("("+onlineCount+"/"+entryCount+")");
		
		return layoutView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

}
