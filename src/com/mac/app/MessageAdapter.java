package com.mac.app;

import java.text.DateFormat;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mac.app.MessageExtended.Direction;

public class MessageAdapter extends ArrayAdapter<MessageExtended> {
	
	public MessageAdapter(Context context, List<MessageExtended> objects) {
		super(context, R.layout.message_row, R.id.body, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View layoutView=convertView;
		if(layoutView==null){
			LayoutInflater inflater=(LayoutInflater)this.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			layoutView=inflater.inflate(R.layout.message_row, null, false);
		}

		MessageExtended msg=this.getItem(position);
		
		TextView msgText=(TextView)layoutView.findViewById(R.id.body);
		msgText.setText(msg.getBody());
		
		TextView from=(TextView)layoutView.findViewById(R.id.from);
		from.setText(StringUtils.parseBareAddress(msg.getFrom()));

		TextView date=(TextView)layoutView.findViewById(R.id.date);

		DateFormat dateFormat=DateFormat.getTimeInstance();		
		String dateStr=dateFormat.format(msg.getDate());
		date.setText(dateStr);
		
		if(msg.getDirection()==Direction.INCOMING){
			layoutView.setBackgroundColor(0xffeeeeff);
		}
		else{
			layoutView.setBackgroundColor(0xffefefef);
		}
		
		return layoutView;
	}

	
}
