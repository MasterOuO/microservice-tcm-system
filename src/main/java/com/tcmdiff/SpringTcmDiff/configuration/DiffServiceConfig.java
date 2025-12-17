package com.tcmdiff.SpringTcmDiff.configuration;

import java.io.File;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tcmdiff.SpringTcmDiff.service.DiffService;

@Configuration
public class DiffServiceConfig {
	
	
	//處理路徑 在application
	@Value("${tcm.data.root-path}")
    private String rootPath;
	
	//初始化diffservice的配置
	@Bean
    public DiffService diffService() {

		//========================== 初始化json的東西 ==================
		try {
			System.out.println("------- 開始載入辨證 JSON 檔... --------");
			HashMap<String, String> dataPathMap = new HashMap<>();
			HashMap<String, String> parameterPathMap = new HashMap<>();
			dataPathMap.put("DE_m_d", getAbsolutePath("BXL_DataFiles/DE/male/DE_male_mapping.json"));
			dataPathMap.put("DE_f_d", getAbsolutePath("BXL_DataFiles/DE/female/DE_female_mapping.json"));
			dataPathMap.put("Menstrual_d", getAbsolutePath("BXL_DataFiles/OG/menstrual/OG_menstrual_mapping.json"));
			dataPathMap.put("Pregnancy_d", getAbsolutePath("BXL_DataFiles/OG/pregnancy/OG_pregnancy_mapping.json"));
			dataPathMap.put("Postpartum_d", getAbsolutePath("BXL_DataFiles/OG/postpartum/OG_postpartum_mapping.json"));
			dataPathMap.put("Insomnia_d", getAbsolutePath("BXL_DataFiles/PS/insomnia/PS_insomnia_mapping.json"));
			dataPathMap.put("Anxiety_d", getAbsolutePath("BXL_DataFiles/PS/anxiety/PS_anxiety_mapping.json"));
			dataPathMap.put("ICD11_d", getAbsolutePath("BXL_DataFiles/ICD11/ICD11_organ_mapping.json"));
			parameterPathMap.put("DE_m_p", getAbsolutePath("BXL_DataFiles/DE/male/DE_male_parameter.json"));
			parameterPathMap.put("DE_f_p", getAbsolutePath("BXL_DataFiles/DE/female/DE_female_parameter.json"));
			parameterPathMap.put("Menstrual_p", getAbsolutePath("BXL_DataFiles/OG/menstrual/OG_menstrual_parameter.json"));
			parameterPathMap.put("Pregnancy_p", getAbsolutePath("BXL_DataFiles/OG/pregnancy/OG_pregnancy_parameter.json"));
			parameterPathMap.put("Postpartum_p", getAbsolutePath("BXL_DataFiles/OG/postpartum/OG_postpartum_parameter.json"));
			parameterPathMap.put("Insomnia_p", getAbsolutePath("BXL_DataFiles/PS/insomnia/PS_insomnia_parameter.json"));
			parameterPathMap.put("Anxiety_p", getAbsolutePath("BXL_DataFiles/PS/anxiety/PS_anxiety_parameter.json"));
			parameterPathMap.put("ICD11_p", getAbsolutePath("BXL_DataFiles/ICD11/ICD11_organ_parameter.json"));

			dataPathMap.put("Abdomen_d", getAbsolutePath("BXL_DataFiles/OG/abdomen/OG_abdomen_mapping.json"));
			parameterPathMap.put("Abdomen_p", getAbsolutePath("BXL_DataFiles/OG/abdomen/OG_abdomen_parameter.json"));
			dataPathMap.put("Breast_d", getAbsolutePath("BXL_DataFiles/OG/breast/OG_breast_mapping.json"));
			parameterPathMap.put("Breast_p", getAbsolutePath("BXL_DataFiles/OG/breast/OG_breast_parameter.json"));
			dataPathMap.put("Infertility_d", getAbsolutePath("BXL_DataFiles/OG/infertility/OG_infertility_mapping.json"));
			parameterPathMap.put("Infertility_p", getAbsolutePath("BXL_DataFiles/OG/infertility/OG_infertility_parameter.json"));
			dataPathMap.put("Leukorrhea_d", getAbsolutePath("BXL_DataFiles/OG/leukorrhea/OG_leukorrhea_mapping.json"));
			parameterPathMap.put("Leukorrhea_p", getAbsolutePath("BXL_DataFiles/OG/leukorrhea/OG_leukorrhea_parameter.json"));
			dataPathMap.put("Vulvar_d", getAbsolutePath("BXL_DataFiles/OG/vulvar/OG_vulvar_mapping.json"));
			parameterPathMap.put("Vulvar_p", getAbsolutePath("BXL_DataFiles/OG/vulvar/OG_vulvar_parameter.json"));
			
			
			String sstPath = getAbsolutePath("BXL_DataFiles/BXL_SST.json");
			DiffService diffService =  new DiffService(dataPathMap, parameterPathMap, sstPath);
			System.out.println("------ 辨證 JSON 載入完成! --------");
			return diffService;
		}catch(Exception e){
        	System.out.println("------ 辨證 JSON 載入失敗，請確認缺少的表格或路徑!! --------");			
			e.printStackTrace();
			throw new RuntimeException("DiffService 初始化失敗", e);
		}
    }
	
	
	private String getAbsolutePath(String relativePath) {
        // File.separator 處理斜線差異
        File f = new File(rootPath + File.separator + relativePath);
        return f.getAbsolutePath();
    }
	
	/*
	private String getAbsolutePath(String relativePath) {
		File f = new File("src/main/resources/static/" + relativePath);
		return f.getAbsolutePath();
	}
	*/
}
