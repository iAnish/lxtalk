package com.mac.lxtalk;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.jivesoftware.smack.util.StringUtils;

class Conversation{
	private final String contact;
	private final List<MessageExtended> messages=new ArrayList<MessageExtended>();
	
	private final Date date;
	private final int uid;

	private boolean isRead=true;
	
	public Conversation(String user){
		this.contact=StringUtils.parseBareAddress(user);
		
		date=new Date();
		uid=new Random().nextInt();
	}
	
	public int getId(){
		return uid;
	}
	
	public Date getDate(){
		return date;
	}
	
	public String getContact(){
		return contact;
	}
	
	public boolean isRead(){
		return isRead;
	}
	
	public void setRead(boolean read){
		isRead=read;
	}
	
	public List<MessageExtended> getMessages(){
		return messages;
	}
	

	@Override
	public String toString() {
		return contact;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Conversation)){
			return false;
		}
		
		
		
		return contact.equals(((Conversation)o).contact);
	}
	
}