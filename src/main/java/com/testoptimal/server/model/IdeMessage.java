package com.testoptimal.server.model;

public class IdeMessage {
	public String type;
	public String text;
	public String source;
	
	public IdeMessage(String type_p, String text_p, String source_p) {
		this.type = type_p;
		this.text = text_p;
		this.source = source_p;
	}
}
