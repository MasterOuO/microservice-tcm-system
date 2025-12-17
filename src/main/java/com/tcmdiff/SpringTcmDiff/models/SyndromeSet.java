package com.tcmdiff.SpringTcmDiff.models;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.*;


public class SyndromeSet extends ArrayList<Syndrome> {
	
	private static final long serialVersionUID = 1L;
	
	
	public SyndromeSet(String filePath) throws IOException, ParseException {

        
        //java8
        //FileReader reader = new FileReader(FilePath);
        
	    //解碼用
	    JSONParser parser = new JSONParser();
		FileInputStream fileInputStream = new FileInputStream(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        
        //症候集合的屬性 group是外層id
        JSONObject groups;
        Integer id;
        String name;
        JSONArray[] primaryGroups;
        JSONArray[] secondaryGroups;
        SymptomSet[] primarySymptomSets;
        SymptomSet[] secondarySymptomSets;
        
        // 使用JSONParser解析JSON文件 解析成javaobj
        Object obj = parser.parse(reader);

        // 將解析結果轉為JSONObject
        JSONObject jsonObject = (JSONObject) obj;
        
        for (Object key : jsonObject.keySet()) {    
            groups = (JSONObject) jsonObject.get(key);// 第i個症候id 找到後再看它裡面的javaobj (內層map)
            
            id = Integer.parseInt(key.toString());
            name = (String) groups.get("name");
            primaryGroups = new JSONArray[5];
            secondaryGroups = new JSONArray[5];
            for (int i = 0; i < 5; i++) {
                primaryGroups[i] = (JSONArray) groups.get("Primary_Group_" + (i + 1));
                secondaryGroups[i] = (JSONArray) groups.get("Secondary_Group_" + (i + 1));
            }
            // *************************產生集合的空間
            //病因,病位,舌診,脈診,特徵 轉成symtomset型態  這裡new物件 所以syndrome建構子那邊不用new
            primarySymptomSets = new SymptomSet[5];
            secondarySymptomSets = new SymptomSet[5];

            for (int i = 0; i < 5; i++) {
                primarySymptomSets[i] = new SymptomSet();
                secondarySymptomSets[i] = new SymptomSet();
            }

            // *********************************將各集合的症狀放入集合空間
            JSONArray primaryGroup;   //pgs[i] 的某一個jsonarray
            JSONArray secondaryGroup;
            for (int i = 0; i < 5; i++) {
                primaryGroup = primaryGroups[i];
                secondaryGroup = secondaryGroups[i];
                
                if (primaryGroup == null) {
                    throw new RuntimeException("資料錯誤！症候 ID: " + id + " (" + name + ") 的 primary_Group_" + (i + 1) + " 遺失或拼寫錯誤！");
                }
                for (Object s : primaryGroup) {
                    primarySymptomSets[i].add(((Long) s).intValue());
                }

                if (secondaryGroup == null) {
                    throw new RuntimeException("資料錯誤！症候 ID: " + id + " (" + name + ") 的 secondary_Group_" + (i + 1) + " 遺失或拼寫錯誤！");
                }
                
                for (Object s : secondaryGroup) {
                    secondarySymptomSets[i].add(((Long) s).intValue());
                }
            }
            // ************************************將該症候症狀集合存成物件，生成第i個症候的物件 封裝成症候物件
            Syndrome syndrome_object = new Syndrome(id, name, primarySymptomSets, secondarySymptomSets);
            // ************************將物件放入回傳的陣列
            this.add(syndrome_object);
        }
	}

}
