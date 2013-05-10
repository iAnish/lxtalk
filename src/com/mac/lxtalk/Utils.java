package com.mac.lxtalk;

import org.jivesoftware.smack.packet.Presence;

public class Utils {
	
	public static int presenceToPosition(Presence presence){
		int position=StatusSpinner.STATUS_POS_OFFLINE;
		
		Presence.Type type=presence.getType();
		if(type==Presence.Type.available){
			position=StatusSpinner.STATUS_POS_AVAILABLE;
			
			Presence.Mode mode=presence.getMode();
			
			if(mode!=null){
				switch(mode){
				case available:
					position=StatusSpinner.STATUS_POS_AVAILABLE;
					break;
				case chat:
					position=StatusSpinner.STATUS_POS_FREE;
					break;
				case away:
					position=StatusSpinner.STATUS_POS_AWAY;
					break;
				case xa:
					position=StatusSpinner.STATUS_POS_XAWAY;
					break;
				case dnd:
					position=StatusSpinner.STATUS_POS_DND;
					break;
				}
			}
		}
		
		return position;
	}
	
	public static Presence positionToPresence(int position){
		
		if(position==StatusSpinner.STATUS_POS_OFFLINE)
			return new Presence(Presence.Type.unavailable);
		

		Presence presence=new Presence(Presence.Type.available);
		Presence.Mode mode=Presence.Mode.available;
		
		switch(position){
		case StatusSpinner.STATUS_POS_AVAILABLE:
			mode=Presence.Mode.available;
			break;
		case StatusSpinner.STATUS_POS_FREE:
			mode=Presence.Mode.chat;
			break;
		case StatusSpinner.STATUS_POS_DND:
			mode=Presence.Mode.dnd;
			break;
		case StatusSpinner.STATUS_POS_AWAY:
			mode=Presence.Mode.away;
			break;
		case StatusSpinner.STATUS_POS_XAWAY:
			mode=Presence.Mode.xa;
			break;
		}
		
		presence.setMode(mode);
		
		return presence;
	}
	
	public static int presenceDrawable(int position){
		
		int drawable=R.drawable.offline;
		
		switch(position){
		case StatusSpinner.STATUS_POS_AVAILABLE:
			drawable=R.drawable.online;
			break;
		case StatusSpinner.STATUS_POS_FREE:
			drawable=R.drawable.free;
			break;
		case StatusSpinner.STATUS_POS_DND:
			drawable=R.drawable.dnd;
			break;
		case StatusSpinner.STATUS_POS_AWAY:
			drawable=R.drawable.away;
			break;
		case StatusSpinner.STATUS_POS_XAWAY:
			drawable=R.drawable.xaway;
			break;
		case StatusSpinner.STATUS_POS_OFFLINE:
			drawable=R.drawable.offline;
			break;
		}
		
		return drawable;
	}
	
	public static int presenceDrawable(Presence presence){
		return presenceDrawable(presenceToPosition(presence));
	}
	

}
