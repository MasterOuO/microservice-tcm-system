package com.tcmdiff.SpringTcmDiff.models;

import java.util.*;



//舉例[30151, 30166, 30435, 30441, 30442] 標準症狀集合
public class SymptomSet extends ArrayList<Integer> {

	
	private static final long serialVersionUID = 1L;
	
	public SymptomSet() {
        super();
    }
	
    public SymptomSet(List<Integer> elements) {
        super(elements);
    }
    /**
     * Make the intersection of the symptom set and the patient symptom set
     * @param Input_Symptoms_set patient symptom set
     * @return Return the intersection ID of the patient's symptom set and the Syndrome symptom set
     */
    public SymptomSet intersect(SymptomSet input_Symptoms_set){
    	SymptomSet i = new SymptomSet(input_Symptoms_set);
    	i.retainAll(this);  //會只剩下跟this相關的元素
    	return i;
    }
}

