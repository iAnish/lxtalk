package com.mac.lxtalk;

import java.util.ArrayList;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationListAdapter extends ArrayAdapter<Conversation> {
	public ConversationListAdapter(Context ctx, ArrayList<Conversation> conversations){
		super(ctx, R.layout.conversation_list_row, R.id.contact_name, conversations);
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Conversation conversation=this.getItem(position);
		
		View layoutView=convertView;
		if(layoutView==null){
			LayoutInflater inflater=(LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layoutView=inflater.inflate(R.layout.conversation_list_row, null, false);
		}
		
		
		TextView usernameView=(TextView)layoutView.findViewById(R.id.contact_name);
		
		String username=StringUtils.parseBareAddress(conversation.getContact());
		usernameView.setText(username);

		ImageView img=(ImageView)layoutView.findViewById(R.id.msg_icon);
		
		if(!conversation.isRead())
			img.setImageResource(R.drawable.msg_new);
		
		return layoutView;
	}
	
	
}
