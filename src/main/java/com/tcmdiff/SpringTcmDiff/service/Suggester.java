package com.tcmdiff.SpringTcmDiff.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ParseException;


import com.tcmdiff.SpringTcmDiff.models.SymptomSet;
import com.tcmdiff.SpringTcmDiff.models.Syndrome;
import com.tcmdiff.SpringTcmDiff.models.SyndromeSet;




public class Suggester {
	 private SyndromeSet syndromeSet;

	    public Suggester(SyndromeSet syndromeSet) throws IOException, ParseException {
	    	this.syndromeSet = syndromeSet;
	    }

	    
	    public ArrayList<Set<Integer>> getSuggestSymptoms(ArrayList<Integer> inputSymptomsSet, ArrayList<String> SyndromesName) {
	        ArrayList<Set<Integer>> result_all = new ArrayList<>();

	        Set<Integer> result_G1 = new HashSet<>();
	        Set<Integer> result_G2 = new HashSet<>();
	        Set<Integer> result_G3 = new HashSet<>();
	        Set<Integer> result_G4 = new HashSet<>();
	        Map<String, Syndrome> syndromeMap = new HashMap<>();
	        
	        for (Syndrome syndrome : this.syndromeSet) {
	            syndromeMap.put(syndrome.getName(), syndrome);
	        }
	        for (String name_i : SyndromesName) {
	            Syndrome syndrome_i = syndromeMap.get(name_i);
	            SymptomSet pset[]=syndrome_i.getPrimSymptSets();
	            SymptomSet sset[]=syndrome_i.getSecSymptSets();
	            for(int i=0;i<5;i++) {
	                addSymptomsIfNotExists(pset[i], inputSymptomsSet, result_G1,10000,20000);
	                addSymptomsIfNotExists(pset[i], inputSymptomsSet, result_G2,20000,30000);
	                addSymptomsIfNotExists(pset[i], inputSymptomsSet, result_G3,30000,40000);
	                addSymptomsIfNotExists(pset[i], inputSymptomsSet, result_G4,40000,50000);
	                
	                addSymptomsIfNotExists(sset[i], inputSymptomsSet, result_G1,10000,20000);
	                addSymptomsIfNotExists(sset[i], inputSymptomsSet, result_G2,20000,30000);
	                addSymptomsIfNotExists(sset[i], inputSymptomsSet, result_G3,30000,40000);
	                addSymptomsIfNotExists(sset[i], inputSymptomsSet, result_G4,40000,50000);
	            }
	        }
	        result_all.add(result_G1);
	        result_all.add(result_G2);
	        result_all.add(result_G3);
	        result_all.add(result_G4);
	        return result_all;
	    }
	    private void addSymptomsIfNotExists(List<Integer> symptoms, List<Integer> inputSymptoms, Set<Integer> result, int low,int high) {
	        for (Integer value : symptoms) {
	            if (!inputSymptoms.contains(value) && !result.contains(value) && (value>=low&&value<high)) {
	                result.add(value);
	            }
	        }
	    }
}
