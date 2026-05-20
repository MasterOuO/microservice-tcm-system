package com.tcmdiff.SpringTcmDiff.repository;


import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Repository;
import com.tcmdiff.SpringTcmDiff.models.Parameter;

@Repository
public class ParameterRepo {
		
	public Parameter loadParameter(String jsonContent) throws IOException, ParseException {
		double[] groupWeights=new double[5];
	    JSONParser parser = new JSONParser();
	    
	    /* 改成直接接收json字串而非路徑，不用FIS，包成jar才能用 
        //FileReader reader = new FileReader(FilePath);
        
        FileInputStream fileInputStream = new FileInputStream(FilePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        */
        // 使用JSONParser解析JSON文件
        Object obj = parser.parse(jsonContent);
        JSONObject jsonObject = (JSONObject) obj;
        
        groupWeights[0] = ((Double) jsonObject.get("G1_weight")).doubleValue();
        groupWeights[1] = ((Double) jsonObject.get("G2_weight")).doubleValue();
        groupWeights[2] = ((Double) jsonObject.get("G3_weight")).doubleValue();
        groupWeights[3] = ((Double) jsonObject.get("G4_weight")).doubleValue();
        groupWeights[4] = ((Double) jsonObject.get("G5_weight")).doubleValue();
        
        double primaryWeight = ((Double) jsonObject.get("primary_weight")).doubleValue();
        double secondaryWeight = ((Double) jsonObject.get("secondary_weight")).doubleValue();      
	   
        return new Parameter(groupWeights, primaryWeight, secondaryWeight);
	}

}
