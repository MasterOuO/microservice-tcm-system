package com.tcmdiff.SpringTcmDiff.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.tcmdiff.SpringTcmDiff.repository.ParameterRepo;
import com.tcmdiff.SpringTcmDiff.repository.SyndromeSetRepo;
import com.tcmdiff.SpringTcmDiff.service.DiffService;
import com.tcmdiff.SpringTcmDiff.util.SyndromeGraphPlotter.Cause;
import com.tcmdiff.SpringTcmDiff.util.SyndromeGraphPlotter.Relation;

@Configuration
public class DiffServiceConfig {
	
	@Autowired 
	private SyndromeSetRepo syndromeRepo;
    @Autowired 
    private ParameterRepo parameterRepo;
    
	//初始化diffservice的配置 如何建立與設定物件
	@Bean
    public DiffService diffService() {

		//========================== 初始化json的東西 ==================
		try {
			System.out.println("------- 開始載入辨證 JSON 檔... --------");
			HashMap<String, String> dataJsonStrMap = new HashMap<>();
			HashMap<String, String> parameterPathMap = new HashMap<>();
			dataJsonStrMap.put("DE_m_d", loadJsonFromClasspath("BXL_DataFiles/DE/male/DE_male_mapping.json"));
			dataJsonStrMap.put("DE_f_d", loadJsonFromClasspath("BXL_DataFiles/DE/female/DE_female_mapping.json"));
			dataJsonStrMap.put("Menstrual_d", loadJsonFromClasspath("BXL_DataFiles/OG/menstrual/OG_menstrual_mapping.json"));
			dataJsonStrMap.put("Pregnancy_d", loadJsonFromClasspath("BXL_DataFiles/OG/pregnancy/OG_pregnancy_mapping.json"));
			dataJsonStrMap.put("Postpartum_d", loadJsonFromClasspath("BXL_DataFiles/OG/postpartum/OG_postpartum_mapping.json"));
			dataJsonStrMap.put("Insomnia_d", loadJsonFromClasspath("BXL_DataFiles/PS/insomnia/PS_insomnia_mapping.json"));
			dataJsonStrMap.put("Anxiety_d", loadJsonFromClasspath("BXL_DataFiles/PS/anxiety/PS_anxiety_mapping.json"));
			dataJsonStrMap.put("ICD11_d", loadJsonFromClasspath("BXL_DataFiles/ICD11/ICD11_organ_mapping.json"));
			parameterPathMap.put("DE_m_p", loadJsonFromClasspath("BXL_DataFiles/DE/male/DE_male_parameter.json"));
			parameterPathMap.put("DE_f_p", loadJsonFromClasspath("BXL_DataFiles/DE/female/DE_female_parameter.json"));
			parameterPathMap.put("Menstrual_p", loadJsonFromClasspath("BXL_DataFiles/OG/menstrual/OG_menstrual_parameter.json"));
			parameterPathMap.put("Pregnancy_p", loadJsonFromClasspath("BXL_DataFiles/OG/pregnancy/OG_pregnancy_parameter.json"));
			parameterPathMap.put("Postpartum_p", loadJsonFromClasspath("BXL_DataFiles/OG/postpartum/OG_postpartum_parameter.json"));
			parameterPathMap.put("Insomnia_p", loadJsonFromClasspath("BXL_DataFiles/PS/insomnia/PS_insomnia_parameter.json"));
			parameterPathMap.put("Anxiety_p", loadJsonFromClasspath("BXL_DataFiles/PS/anxiety/PS_anxiety_parameter.json"));
			parameterPathMap.put("ICD11_p", loadJsonFromClasspath("BXL_DataFiles/ICD11/ICD11_organ_parameter.json"));

			dataJsonStrMap.put("Abdomen_d", loadJsonFromClasspath("BXL_DataFiles/OG/abdomen/OG_abdomen_mapping.json"));
			parameterPathMap.put("Abdomen_p", loadJsonFromClasspath("BXL_DataFiles/OG/abdomen/OG_abdomen_parameter.json"));
			dataJsonStrMap.put("Breast_d", loadJsonFromClasspath("BXL_DataFiles/OG/breast/OG_breast_mapping.json"));
			parameterPathMap.put("Breast_p", loadJsonFromClasspath("BXL_DataFiles/OG/breast/OG_breast_parameter.json"));
			dataJsonStrMap.put("Infertility_d", loadJsonFromClasspath("BXL_DataFiles/OG/infertility/OG_infertility_mapping.json"));
			parameterPathMap.put("Infertility_p", loadJsonFromClasspath("BXL_DataFiles/OG/infertility/OG_infertility_parameter.json"));
			dataJsonStrMap.put("Leukorrhea_d", loadJsonFromClasspath("BXL_DataFiles/OG/leukorrhea/OG_leukorrhea_mapping.json"));
			parameterPathMap.put("Leukorrhea_p", loadJsonFromClasspath("BXL_DataFiles/OG/leukorrhea/OG_leukorrhea_parameter.json"));
			dataJsonStrMap.put("Vulvar_d", loadJsonFromClasspath("BXL_DataFiles/OG/vulvar/OG_vulvar_mapping.json"));
			parameterPathMap.put("Vulvar_p", loadJsonFromClasspath("BXL_DataFiles/OG/vulvar/OG_vulvar_parameter.json"));
			
			
			String sstContent = loadJsonFromClasspath("BXL_DataFiles/BXL_SST.json");

			List<Cause> loadCauses = loadCauses();
			List<Relation> loadRelations = loadRelations();
			DiffService diffService =  new DiffService(dataJsonStrMap, parameterPathMap, sstContent , syndromeRepo, parameterRepo, loadCauses, loadRelations);
			System.out.println("------ 辨證 JSON 載入完成! --------");
			return diffService;
		}catch(Exception e){
        	System.out.println("------ 辨證 JSON 載入失敗，請確認缺少的表格或路徑!! --------");			
			e.printStackTrace();
			throw new RuntimeException("DiffService 初始化失敗", e);
		}
    }
	
	private String loadJsonFromClasspath(String classpathRelativePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathRelativePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
	}

	// 演變資料初始化
	private List<Cause> loadCauses() throws Exception {
		
		List<Cause> causes = new ArrayList<>();
	    JSONParser parser = new JSONParser();
	   
	    try (InputStream is = new ClassPathResource("static/Syndrome_Evolution_Causes.json").getInputStream()) 
	    {
	        if (is == null) {
	            throw new IllegalStateException("找不到 JSON 檔");
	        }
	
	        JSONArray arr = (JSONArray) parser.parse(new InputStreamReader(is, "UTF-8"));
	        for (Object o : arr) {
	            JSONObject obj = (JSONObject) o;
	            String id = (String) obj.get("id"); 
	            String to = (String) obj.get("to");
	            String source = (String) obj.get("source");
	            String label = (String) obj.get("label");
	            
	            List<String> causeList = new ArrayList<>();
	            JSONArray causeArr = (JSONArray) obj.get("cause");
	            if (causeArr != null) {
	                for (Object c : causeArr) {
	                    causeList.add((String) c);
	                }
	            }
	            
	            causes.add(new Cause(id, causeList, to, source, label));
	        }
	    }
	    return causes;
	}
	
    // 從json讀檔轉物件
    private List<Relation> loadRelations() throws Exception {
    	List<Relation> relations = new ArrayList<>();
        JSONParser parser = new JSONParser();

       // try (InputStream is = SyndromeGraphPlotter.class.getResourceAsStream("/static/Syndrome_Evolution_Relation.json")) 
        try (InputStream is = new ClassPathResource("static/Syndrome_Evolution_Relation.json").getInputStream()) 
        {
            if (is == null) {
                throw new IllegalStateException("找不到 JSON 檔");
            }

            JSONArray arr = (JSONArray) parser.parse(new InputStreamReader(is, "UTF-8"));
            for (Object o : arr) {
                JSONObject obj = (JSONObject) o;
                String from = (String) obj.get("from");
                String to = (String) obj.get("to");
                String source = (String) obj.get("source");
                String label = (String) obj.get("label");
                relations.add(new Relation(from, to, source, label));
            }
        }
        return relations;
	}
	
}
