package com.mac.app;

import java.util.Date;

import org.jivesoftware.smack.packet.Message;

public class MessageExtended extends Message {

	public enum Direction{INCOMING, OUTCOMING};
	
	private Date date;
	
	private final Direction direction;
	
	public MessageExtended(Message msg, Direction direction) {
		this.direction=direction;
		
		this.setBody(msg.getBody());
		this.setFrom(msg.getFrom());
		this.setTo(msg.getTo());
		this.setThread(msg.getThread());
		this.setType(msg.getType());
		this.setSubject(msg.getSubject());
		this.setPacketID(msg.getPacketID());
		this.setLanguage(msg.getLanguage());
		
		
		date=new Date();
	}
	
	public MessageExtended(Message msg, Direction direction, Date date) {
		this(msg, direction);
		
		this.date=date;
	}
	
	public Date getDate(){
		return date;
	}
	
	public Direction getDirection(){
		return direction;
	}

}
