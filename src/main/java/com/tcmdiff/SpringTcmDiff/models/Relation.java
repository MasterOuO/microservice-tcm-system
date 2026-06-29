package com.tcmdiff.SpringTcmDiff.models;

public class Relation {
	public final String from;
    public final String to;
	public final String source;
	public final String label;


    public Relation( String from, String to,  String source, String label) {
        this.from = from;
        this.to = to;
        this.source = source;
		this.label = label;
    }
    
    public String getFrom() {
		return this.from;
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
