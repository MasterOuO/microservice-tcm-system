package com.tcmdiff.SpringTcmDiff.repository;


import java.io.IOException;
import java.util.*;
import org.json.simple.parser.*;
import org.json.simple.JSONObject;


//用json建立兩張map 
public class StdSymptomsTableRepo {

	//Table 可以用 中文名稱 或 字串id 找到 數字id
	//Table_name 只能用 數字id 找到 中文名稱 比較像原始的json格式
	private HashMap<String, Integer> Table;
	private HashMap<Integer, String> Table_name;
	

	
	/**
	 * Constructors of the Standard Symptom List
	 * @param path File path to standard symptoms json table
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public StdSymptomsTableRepo(String path) throws IOException, ParseException {
		
		this.Table=new HashMap<String, Integer>();
		this.Table_name=new HashMap<Integer, String>();
		this.connData(path);
	}
	
	//拿到id
	public Integer getID(String Symptom_name){
		return this.Table.get(Symptom_name);
	}
	
	//拿到症狀名稱
	public String getName(Integer Symptom_id){
		return this.Table_name.get(Symptom_id);
	}

	
	
	/*
	
	//沒用到
	public HashMap<String, Integer> get_Standard_Symptoms_Table(){
		return this.Table;
	}
	
	
	// 放到diffservice private
	// 1. 症狀名稱集合 轉 ID集合
    public SymptomSet getIDs(ArrayList<String> input_Symptoms) {
        SymptomSet output_Symptoms_ID = new SymptomSet(); 
        Integer id;
        for(String symptom_Name : input_Symptoms) {
            id = this.getID(symptom_Name); // 直接呼叫內部的 get_ID
            if (id != null && !output_Symptoms_ID.contains(id)) {
                output_Symptoms_ID.add(id);
            }
        }
        return output_Symptoms_ID;   
    }
    
    // 2. 症狀ID集合 轉 名稱集合
    public ArrayList<String> getNames(Collection<Integer> input_Symptom) {
        ArrayList<String> output_Symptoms_NAME = new ArrayList<>();
        String name;
        List<Integer> list = new ArrayList<Integer>(input_Symptom);
        Collections.sort(list); // 排序
        for(Integer symptom_Id : list) {
            name = this.getName(symptom_Id); // 直接呼叫內部的 get_Name
            if (name != null && !output_Symptoms_NAME.contains(name)) {
                output_Symptoms_NAME.add(name);
            }
        }
        return output_Symptoms_NAME;   
    }
	
	*/
	
	/**
	 * Link data table to get data
	 * @param FilePath The file path of the standard symptom table
	 * @throws ParseException 
	 * @throws IOException 
	 */
	private void connData(String jsonContent) throws IOException, ParseException {
		
	    JSONParser parser = new JSONParser();
	    
	    /*
	    FileInputStream fileInputStream = new FileInputStream(FilePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        */
	    
		// 使用JSONParser解析JSON文件
		Object obj = parser.parse(jsonContent);
		JSONObject jsonObject = (JSONObject) obj;
		for (Object key : jsonObject.keySet()) {
			int intKey = Integer.parseInt(key.toString());
			String value = (String) jsonObject.get(key);
			this.Table.put(value, intKey);
			this.Table_name.put(intKey, value);
		}
	}
}
