package com.tcmdiff.SpringTcmDiff.models;

import java.util.List;

public class Cause {
	public final String id;
	public final List<String> cause;        
    public final String to;   
    public final String source;  
    public final String label;

    public Cause (String id, List<String> cause, String to, String source, String label) {
    	this.id = id;
        this.cause = cause;
        this.to = to;
        this.source = source;
		this.label = label;
    }

    public String getId() {
		return this.id;
    }
	public List<String> getCauseList() {
		return this.cause;
	}
    public String getTo() {
    		return this.to;
    }
    public String getSource() {
		return this.source;
    }
    public String getLabel() {
		return this.label;
    }
}
