package com.tcmdiff.SpringTcmDiff.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.*;


import com.tcmdiff.SpringTcmDiff.models.Parameter;
import com.tcmdiff.SpringTcmDiff.models.SymptomSet;
import com.tcmdiff.SpringTcmDiff.models.Syndrome;
import com.tcmdiff.SpringTcmDiff.models.SyndromeSet;

//辨證器實例化時選擇用哪一主訴
public class Differentiator {
		//裝辯證器所有證候物件的list
	
    	
		private SyndromeSet syndromeSet;
		private Parameter parameter;
		
		public Differentiator(SyndromeSet syndromeSet, Parameter parameter) throws IOException, ParseException {
			this.syndromeSet = syndromeSet;
			this.parameter = parameter;
		}
		
		public LinkedHashMap<String, Double> differentiateSyndrome(SymptomSet inputSymptomsSet, double threshold) {
			Double syndrome_score=0.0;
			// ********************************************************************************
			// 要輸出的症候陣列空間
			HashMap<String, Double> unsorted_map = new HashMap<>();// 拿來排序的
			LinkedHashMap<String, Double> output_map = new LinkedHashMap<>();// 排序後回傳的
			// ********************************************************************************取出暫存
			// 所有的症候要拿來跟輸入症狀做比對
			SyndromeSet localSyndromeSet = this.syndromeSet;
			// ********************************************************************************辨證
			
			for (Syndrome s : localSyndromeSet) {
			    
				//syndrome_score = syndrome_i.differentiate_Syndrome(Input_Symptoms_set, this.Parameter);
			    syndrome_score = diffSyndrome(inputSymptomsSet, s);
				// 當過於閥值才列入輸出
				if (syndrome_score >= threshold) {
					unsorted_map.put(s.getName(), syndrome_score);
				}
			}
			// ********************************************************************************排序
			// 保留key值的方式來轉成List，目的是要進行排序
			List<Map.Entry<String, Double>> entryList = new ArrayList<>(unsorted_map.entrySet());

			// 使用Comparator根據list裡entry的value進行排序
			
			//java8
			//Collections.sort(entryList, Comparator.comparingDouble(Map.Entry::getValue));
			
			//java7
			Collections.sort(entryList, new Comparator<Map.Entry<String, Double>>() {
			    @Override
			    public int compare(Map.Entry<String, Double> entry1, Map.Entry<String, Double> entry2) {   
			        return Double.compare(entry1.getValue(), entry2.getValue());
			    }
			});
			
			// 以降序放置輸出回傳
			for (int i = entryList.size() - 1; i >= 0; i--) {
				Map.Entry<String, Double> entry = entryList.get(i);
				output_map.put(entry.getKey(), entry.getValue());
			}
			// ********************************************************************************
			// 回傳已排序的結果
			return output_map;
		}

		
		// *****************************拿症狀集+證候物件 計算該證候歸屬度*******************************************		
		private Double diffSyndrome(SymptomSet input_Symptoms_set, Syndrome syndrome_i) {
	        
			//取得該物件的parameter屬性裡面得東西
	        double G_W[] = this.parameter.getGroupWeights(); //五群權重
	        double P_W = this.parameter.getPrimWeight();
	        double S_W = this.parameter.getSecWeight(); 
	        
	        Double result_score=0.0;
	        for (int i = 0; i < 5; i++) {
	            SymptomSet primSet = syndrome_i.getPrimSymptSets()[i];
	            SymptomSet secSet = syndrome_i.getSecSymptSets()[i];


	            int primIntersectCount = (int) primSet.stream().filter(input_Symptoms_set::contains).count();
	            if (secSet.isEmpty()) { 
	                // 沒有次要群 算主要群就好
	                result_score += this.diffGroup(primIntersectCount, primSet.size()) * G_W[i];
	            } else {
	                int secIntersectCount = (int) secSet.stream().filter(input_Symptoms_set::contains).count();
	                
	                result_score += (this.diffGroup(primIntersectCount, primSet.size()) * P_W 
	                                + this.diffGroup(secIntersectCount, secSet.size()) * S_W) * G_W[i];
	            }
	        }
	        return result_score;
	    }
/*
	        for(int i=0;i<5;i++) {
	            if(syndrome_i.getSecSymptSets()[i].size()==0) {  //沒有次要群 算主要5群就好
	                result_score+=this.diffGroup(
	                		syndrome_i.getPrimSymptSets()[i].intersect(input_Symptoms_set).size(), syndrome_i.getPrimSymptSets()[i].size()) * G_W[i];
	            }else {
	                result_score+= (this.diffGroup(syndrome_i.getPrimSymptSets()[i].intersect(input_Symptoms_set).size(), syndrome_i.getPrimSymptSets()[i].size()) * P_W 
	                        + this.diffGroup(syndrome_i.getSecSymptSets()[i].intersect(input_Symptoms_set).size(), syndrome_i.getSecSymptSets()[i].size()) * S_W) * G_W[i];
	            }
	        }
	        
	        return result_score;
	    }
*/		
		
		// m是選擇症狀跟症候某群裡面症狀相同的數字
		// n是症候某群裡面幾個症狀
		// *****************************分權分群演算法  計算證候某群的歸屬度*******************************************
		private Double diffGroup(int m, int n) {
	        
	        if (m == 0) {// 當該群為空，則n會等於0，如4群少5群一群，3群少5群兩群
	            return 0.0;
	        } else if (m != n) {
	            double temp_A = 1.0 / n;
	            double temp_P = Math.pow(2, m);
	            double temp_B = (temp_P - 1.0) / temp_P;
	            double temp_C = (n - 1.0) / n;
	            return temp_A + temp_B * temp_C;
	        } else {
	            return 1.0;
	        }

	    }
}
