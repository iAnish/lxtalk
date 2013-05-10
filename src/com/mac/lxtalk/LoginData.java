package com.mac.lxtalk;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

public class LoginData{
	private final String accountName;
	private final String username;
	private final String password;
	private final String server;
	private final Presence presence;
	
	public LoginData(String accountName, String password, Presence presence){
		
		this.accountName=accountName;
		
		this.username=StringUtils.parseName(accountName);
		this.password=password;
		this.server=StringUtils.parseServer(accountName);
		this.presence=presence;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getServer(){
		return server;
	}
	
	public String getAccountName(){
		return accountName;
	}
	
	public Presence getPresence(){
		return presence;
	}
}