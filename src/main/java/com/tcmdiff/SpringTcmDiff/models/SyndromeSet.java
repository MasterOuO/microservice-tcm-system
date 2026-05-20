package com.tcmdiff.SpringTcmDiff.models;

import java.util.*;



public class SyndromeSet extends ArrayList<Syndrome> {
	
	private static final long serialVersionUID = 1L;
	

	public SyndromeSet() {
        super();
	}
	
	
	@Override
	public boolean add(Syndrome syndrome) {
		if (this.contains(syndrome)) {
			return false;
		}
		return super.add(syndrome);
	}
}
