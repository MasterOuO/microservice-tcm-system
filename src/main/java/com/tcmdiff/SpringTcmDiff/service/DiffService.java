package com.tcmdiff.SpringTcmDiff.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


import com.tcmdiff.SpringTcmDiff.models.Relation;
import com.tcmdiff.SpringTcmDiff.models.Cause;

import org.json.simple.parser.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tcmdiff.SpringTcmDiff.models.DiffResult;
import com.tcmdiff.SpringTcmDiff.models.SymptomSet;
import com.tcmdiff.SpringTcmDiff.repository.DiffResultRepo;
import com.tcmdiff.SpringTcmDiff.repository.ParameterRepo;
import com.tcmdiff.SpringTcmDiff.repository.StdSymptomsTableRepo;
import com.tcmdiff.SpringTcmDiff.repository.SyndromeSetRepo;


//有新增疾病或症候要去
//(1)前端系統更新patient_event_handler.js的顯示選項
//(2)前端系統更新Symptom_Data.js的症狀列表跟選項  這個檔案負責打字能即時顯示
//(3)前端系統usercontroller 裡面doctorPage方法 string opt[]新增選項給html抓

//(3)後端更新UserController的initDiffModel()的辨證資料檔案路徑與頁面模板的注入資料  -> 改到DiffServiceConfig
//(4)後端更新DiffService的辨證器實例化 (就是這裡)


public class DiffService {
	 	
		@Autowired
	    private DiffResultRepo diffRepository;
	
		@Autowired
		private SyndromeGraphPlotter syndromeGraphService;

	 	private StdSymptomsTableRepo symptomTable = null;
	    private Differentiator dem_diff = null;
	    private Differentiator def_diff = null;
	    private Differentiator menstrual_diff = null;
	    private Differentiator pregnancy_diff = null;
	    private Differentiator postpartum_diff = null;
	    private Differentiator insomnia_diff = null;
	    private Differentiator anxiety_diff = null;
	    private Differentiator icd11_diff = null;
	    
	    private Differentiator abdomen_diff = null; 
	    private Differentiator breast_diff = null;  
	    private Differentiator infertility_diff = null;  
	    private Differentiator leukorrhea_diff = null;  
	    private Differentiator vulvar_diff = null;
	    
	    private Suggester dem_suggester = null;
	    private Suggester def_suggester = null;
	    private Suggester menstrual_suggester = null;
	    private Suggester pregnancy_suggester = null;
	    private Suggester postpartum_suggester = null;
	    private Suggester insomnial_suggester = null;
	    private Suggester anxiety_suggester = null;
	    private Suggester icd11_suggester = null;
	    
	    private Suggester abdomen_suggester = null;
	    private Suggester breast_suggester = null;
	    private Suggester infertility_suggester = null;
	    private Suggester leukorrhea_suggester = null;
	    private Suggester vulvar_suggester = null;
	    
	    private final List<Cause> loadCauses;
	    private final List<Relation> loadRelations;	    
  
	    public DiffService(HashMap<String,String> d_pathMap, HashMap<String,String> p_pathMap, String sst_path,SyndromeSetRepo syndromeRepo, ParameterRepo parameterRepo, List<Cause> loadCauses, List<Relation> loadRelations) throws IOException, ParseException{
	    	//try-catch部分刪掉 給config判斷 e拋給config即可
   	
	        
	    	//*********************************內科資料*********************************
            this.dem_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("DE_m_d")),parameterRepo.loadParameter(p_pathMap.get("DE_m_p")));
            this.def_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("DE_f_d")),parameterRepo.loadParameter(p_pathMap.get("DE_f_p")));
            
            this.dem_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("DE_m_d")));
            this.def_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("DE_f_d")));
	        
	        //*********************************婦產科資料*********************************
            this.menstrual_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Menstrual_d")),parameterRepo.loadParameter(p_pathMap.get("Menstrual_p")));
            this.pregnancy_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Pregnancy_d")),parameterRepo.loadParameter(p_pathMap.get("Pregnancy_p")));
            this.postpartum_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Postpartum_d")),parameterRepo.loadParameter(p_pathMap.get("Postpartum_p")));
            
            this.menstrual_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Menstrual_d")));
            this.pregnancy_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Pregnancy_d")));
            this.postpartum_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Postpartum_d")));
            
            this.abdomen_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Abdomen_d")),parameterRepo.loadParameter(p_pathMap.get("Abdomen_p")));
            this.abdomen_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Abdomen_d")));  
            this.breast_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Breast_d")),parameterRepo.loadParameter(p_pathMap.get("Breast_p")));
            this.breast_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Breast_d")));  
            this.infertility_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Infertility_d")),parameterRepo.loadParameter(p_pathMap.get("Infertility_p")));
            this.infertility_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Infertility_d")));  
            this.leukorrhea_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Leukorrhea_d")),parameterRepo.loadParameter(p_pathMap.get("Leukorrhea_p")));
            this.leukorrhea_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Leukorrhea_d")));  
            this.vulvar_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Vulvar_d")),parameterRepo.loadParameter(p_pathMap.get("Vulvar_p")));
            this.vulvar_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Vulvar_d")));  

	        //*********************************精神科資料*********************************
            this.insomnia_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Insomnia_d")),parameterRepo.loadParameter(p_pathMap.get("Insomnia_p")));
            this.anxiety_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("Anxiety_d")),parameterRepo.loadParameter(p_pathMap.get("Anxiety_p")));
            
            this.insomnial_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Insomnia_d")));
            this.anxiety_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("Anxiety_d")));

	        //*********************************ICD11*********************************
            this.icd11_diff=new Differentiator(syndromeRepo.loadSyndromeSet(d_pathMap.get("ICD11_d")),parameterRepo.loadParameter(p_pathMap.get("ICD11_p")));
            this.icd11_suggester=new Suggester(syndromeRepo.loadSyndromeSet(d_pathMap.get("ICD11_d")));
            
	        //*********************************標準症狀表裡面有全部症狀的總表*********************************
            symptomTable = new StdSymptomsTableRepo(sst_path);
            
            
            //預載入的cause
            this.loadCauses = loadCauses;
            this.loadRelations = loadRelations;
	    }
	    
	   
	    
	    
	    //從user點選的症狀進行操作 userController用 
	    public HashMap<String, Object> computeDiff(String opt,String subOpt, String userInputSymptoms) throws IOException, InterruptedException {
	    	//使用者點選的症狀轉成arraylist<string>
	    	ArrayList<String> input_Symptoms = new ArrayList<>(Arrays.asList(userInputSymptoms.split("\\s*[\\s*,.、，\"。:;_(){}\\[\\]]\\s*")));// "\\"s*:零個或多個空白字符
	        //拿這些症狀的id集合 
	    	//SymptomSet input_Symptoms_ID = converter.get_IDs(input_Symptoms);
	    	SymptomSet input_Symptoms_ID = this.convertIDs(input_Symptoms);
	        HashMap<String, Object> jsonOutput = new HashMap<>();
	        ArrayList<ArrayList<String>> suggested_symptoms_Group_Name = new ArrayList<>();
	        ArrayList<Set<Integer>> suggested_symptoms_List = null;
	        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

	        
	        String logicKey = opt;
	        if (subOpt != null && !subOpt.isEmpty()) {
	            logicKey = opt + subOpt; // 例如 "OG" + "02" -> "OG02"
	        }
	        
	        switch(logicKey){
	        	case "DE01":
	                result = dem_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = dem_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"舌苔顏色白", "舌苔顏色黃", "舌苔質地黏膩", "舌體顏色淡白色", "舌體顏色紅色"},
	                            {"噯氣", "咳嗽", "聲啞", "呼吸微弱", "呼吸痰鳴"},
	                            {"口乾", "腹痛",  "畏寒喜溫", "全身無力", "大便質稀"},
	                            {"脈寬度細", "脈力度弱", "脈利度滑", "脈緊度緊","脈速度快"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break;
	            case "DE02":
	                result = def_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = def_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"舌苔顏色白", "舌苔顏色黃", "舌苔質地黏膩", "舌體顏色淡白色", "舌體顏色紅色"},
	                            {"咳嗽", "聲啞", "呼吸微弱", "呼吸急促", "意識譫語"},
	                            {"腹脹", "四肢冷", "畏寒喜溫", "食欲低落", "大便質稀"},
	                            {"脈寬度細", "脈力度弱", "脈利度滑", "脈緊度緊","脈速度快"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break;
	            case "OG01":
	                result = menstrual_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = menstrual_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"舌苔顏色白","舌苔顏色黃", "舌苔厚度薄", "舌體顏色淡白色","舌體顏色紅色"},
	                            {"呼吸微弱", "意識懶言","情緒多嘆"},
	                            {"小腹痛", "頭暈","月經量少", "月經量多", "月經質稀"},
	                            {"脈寬度細","脈力度弱", "脈緊度緊", "脈速度快", "脈長度長"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break;
	            case "OG02":
	                result = pregnancy_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = pregnancy_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"舌苔顏色白", "舌苔顏色黃","舌苔厚度薄","舌體顏色淡白色","舌體顏色深紅色"},
	                            {"口臭", "呼吸微弱", "呼吸急促", "呼吸聲大", "意識懶言"},
	                            {"口乾", "頭暈","胸悶", "妊娠腹痛", "妊娠陰道出血"},
	                            {"脈寬度細","脈力度弱", "脈利度滑","脈緊度緊",   "脈速度快"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break;
	            case "OG03":
	                result = postpartum_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = postpartum_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"舌苔顏色白", "舌苔顏色黃","舌苔厚度薄", "舌體顏色淡白色", "舌體顏色紅色"},
	                            {"呼吸微弱", "呼吸痰鳴", "意識懶言", "意識譫語"},
	                            {"頭暈","小腹痛", "小便不暢", "小便急迫", "大便質硬"},
	                            {"脈寬度細", "脈力度弱", "脈緊度緊","脈速度快", "脈長度長"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break;
	            case "PS01":
	                result = insomnia_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = insomnial_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"面色紅", "舌苔顏色黃", "舌苔厚度薄", "舌苔質地黏膩","舌體顏色紅色"},
	                            {"乾嘔","噯氣","噯氣味臭", "意識譫語","呼吸聲大"},
	                            {"難眠", "多夢", "頭暈", "目眩", "大便質硬"},
	                            {"脈寬度細", "脈利度滑", "脈緊度緊","脈速度快", "脈長度長"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break;
	            case "PS02":
	                result = anxiety_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = anxiety_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"面色紅", "舌苔顏色黃", "舌苔厚度薄", "舌苔質地黏膩","舌體顏色紅色"},
	                            {"噯氣", "噯氣味臭", "意識譫語", "呼吸聲大","嘔吐物味酸"},
	                            {"難眠", "多夢", "頭暈", "大便次少", "大便質硬"},
	                            {"脈寬度細", "脈利度滑", "脈緊度緊","脈速度快", "脈長度長"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break;
	            case "ICD11":
	                result = icd11_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = icd11_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"情緒神疲", "舌苔顏色白", "舌苔顏色黃", "舌體顏色淡白色", "舌苔質地黏膩","舌體顏色紅色"},
	                            {"咳喘", "呼吸微弱"},
	                            {"難眠", "心悸", "頭暈", "食欲低落", "大便質稀", "四肢冷", "腹痛", "發熱"},
	                            {"脈寬度細", "脈力度弱", "脈緊度緊","脈速度快"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break;

	            case "OG04":
	                result = leukorrhea_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = leukorrhea_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"情緒憤怒", "情緒煩躁", "情緒憂鬱", "情緒神疲", "面色白","舌苔質地黏膩","舌苔厚度薄","舌體顏色紅色"},
	                            {},
	                            {"手足心熱","小腹痛","腰酸","胸脹","帶下量多","帶下質濃","大便質稀",},
	                            {"脈寬度細", "脈力度弱", "脈緊度緊","脈速度快"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break; 
	            case "OG05":
	                result = breast_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = breast_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"情緒憤怒", "情緒煩躁", "情緒憂鬱", "情緒神疲", "面色白","舌苔質地黏膩","舌苔厚度薄","舌體顏色紅色"},
	                            {"情緒多嘆", "呼吸微弱"},
	                            {"手足心熱","畏寒喜溫","腰酸","胸悶","胸脹","乳汁質稀","乳房膿瘡","大便質稀",},
	                            {"脈寬度細", "脈力度弱", "脈緊度緊","脈速度快"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break; 
	            case "OG06":
	                result = vulvar_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = vulvar_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"情緒神疲", "情緒煩躁", "舌苔顏色黃", "舌體顏色淡白色", "舌苔質地黏膩","舌體顏色紅色","舌苔厚度薄"},
	                            {"意識懶言"},
	                            {"手足心熱","腹脹","腰酸","胸悶","帶下量多","帶下質濃","大便質稀",},
	                            {"脈寬度細", "脈力度弱", "脈緊度緊","脈速度快"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break;  
	            case "OG07":
	                result = infertility_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = infertility_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"情緒煩躁", "情緒憤怒", "情緒憂鬱", "情緒神疲", "面色白","舌苔質地黏膩","舌體顏色紅色","舌苔厚度薄"},
	                            {},
	                            {"手足心熱","畏寒喜溫","腹脹","腰酸","胸悶","胸脹","帶下量多","帶下質濃","大便質稀",},
	                            {"脈寬度細", "脈力度弱", "脈緊度緊","脈速度快"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break; 
	            case "OG08":
	                result = abdomen_diff.differentiateSyndrome(input_Symptoms_ID, 0.001);
	                suggested_symptoms_List = abdomen_suggester.getSuggestSymptoms(input_Symptoms_ID, getTopSyndrome(result));
	                if (result.isEmpty()) {
	                    String[][] sdata= {{"情緒神疲", "舌苔顏色白", "舌體顏色淡白色", "舌體顏色紫色","舌苔質地黏膩","舌苔厚度薄"},
	                            {},
	                            {"畏寒喜溫","腹脹","小腹痛","胸悶","胸脹","帶下量多","帶下質濃","大便質稀",},
	                            {"脈寬度細", "脈力度弱", "脈緊度緊","脈緊度散"}};
	                    setData(suggested_symptoms_Group_Name,sdata);
	                }
	                break; 
	            default:
	                System.out.println("沒有"+logicKey+"選項的辨證器");
	                jsonOutput.put("error", "不支援的辨證選項：" + logicKey);
	                return jsonOutput;
	        }
	        if(!result.isEmpty()) {
	            setData(suggested_symptoms_Group_Name,suggested_symptoms_List);
	        }

	        // 0.3以下的結果不傳回去，顯示比較少
			result.entrySet().removeIf(entry -> entry.getValue() <= 0.3);

	        jsonOutput.put("output", result);
	        jsonOutput.put("output_suggested_G1", suggested_symptoms_Group_Name.get(0));
	        jsonOutput.put("output_suggested_G2", suggested_symptoms_Group_Name.get(1));
	        jsonOutput.put("output_suggested_G3", suggested_symptoms_Group_Name.get(2));
	        jsonOutput.put("output_suggested_G4", suggested_symptoms_Group_Name.get(3));
	        
	        return jsonOutput;
	    }
	    
	    public Path generateGraph(List<Map<String, String>> result) throws Exception {
	    	Map<String, Double> scores = new HashMap<>();
	        List<Cause> customCauses = new ArrayList<>(); 
	        int causeCounter = 1;
	        
	        // 從 relation 取得有效的証候列表
	        Set<String> validSyndromes = new HashSet<>();
	        if (this.loadCauses != null) {
	        	for (Cause cause : this.loadCauses) {
	        		validSyndromes.add(cause.to);
	        	}
	        }
	        
	        // 把controller收到的ajax 資料 map組合成的list抓出來
	        for (int idx = 0; idx < result.size(); idx++) {
	        	Map<String, String> data = result.get(idx);
	            String syndrome = data.get("syndrome");
	            String scoreStr = data.get("score");
	            String causeStr = data.get("cause");         

	            // 無紀錄證候，記錄Log
	            if (!validSyndromes.isEmpty() && !validSyndromes.contains(syndrome)) {
	            	System.out.println("警告：第 " + (idx+1) + " 筆症候 '" + syndrome + "' 不在有效列表");
	            }
	            
	            // 轉換分數
	            if (scoreStr != null && !scoreStr.isEmpty()) {
	                try {
	                	scores.put(syndrome, Double.parseDouble(scoreStr));
	                } catch (NumberFormatException e) {
	                	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
	                		"分數格式錯誤: " + e.getMessage());
	                }
	            }

	            // 使用者有選成因，額外建立一個 Cause 物件
	            if (causeStr != null && !causeStr.trim().isEmpty() && !causeStr.contains("-- 不顯示 --")) {
	                List<String> causeList = new ArrayList<>();
	            
	                String formattedCause = causeStr.replace("\\n", "\\l").replace("\n", "\\l");
	                causeList.add(formattedCause); 
	                String uniqueId = "c" + (causeCounter++);
	                
	                Cause c = new Cause(uniqueId, causeList, syndrome, "自訂來源", "");
	                
	                customCauses.add(c);
	            }
	        }

	        // 限制產圖邏輯
	        long validScoreCount = scores.values().stream()
	                .filter(score -> score > 0.4 && score < 1.000)
	                .count();
	        if (validScoreCount < 2 ) {
	            return null;
	        }

	        List<Relation> relations = this.loadRelations;
	        
	        try {
	        	String dot = syndromeGraphService.toDot(scores, relations, customCauses);        
	        	String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	        	String dotFileName = "case_diff_temp_" + timestamp + ".dot";
	        	Path dotFile = syndromeGraphService.writeDot(dot, dotFileName);
	        	Path pngFile = syndromeGraphService.dotToPng(dotFile);
	        	
	        	System.out.println("已生成圖檔：" + pngFile);
	        	return pngFile;
	        } catch (IOException | InterruptedException e) {
	        	throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
	        		"圖表生成失敗: " + e.getMessage());
	        }
	    }
	    
	    @Transactional // 確保資料庫操作的完整性
	    public void saveDiffRecord(String name, String identity, String symptoms, String result, String other, String tmpGraphPath) throws IOException {
	        DiffResult diff = new DiffResult();
	        
	        diff.setName(name);
	        diff.setIdentity(identity);
	        diff.setSymptom(symptoms);
	        diff.setResult(result);
	        diff.setOther(other);
	        
	        // 處理時間格式
	        diff.setDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

	        //處理圖片從暫存到永久路徑 
	        String finalGraphPath = null;
	        if (tmpGraphPath != null && !tmpGraphPath.trim().isEmpty()) {
	            // 後面 "?t=..." 快取參數砍掉
	            String cleanPath = tmpGraphPath.split("\\?")[0]; 
	            
	            // 抓檔名 "case_diff_temp_20260615_173010.png"
	            String fileName = Paths.get(cleanPath).getFileName().toString();

	            // 定義tmp與permanent的絕對路徑
	            Path sourcePath = SyndromeGraphPlotter.TMP_PNG_DIR.resolve(fileName);
	            Path targetPath = SyndromeGraphPlotter.PNG_DIR.resolve(fileName);
	            /*
	            String currentDir = System.getProperty("user.dir");
	            Path targetDir = Paths.get(currentDir, "tcm_data", "graph", "permanent");
	            Path targetPath = targetDir.resolve(fileName);
				*/
	            // 確保永久資料夾存在
	            if (!Files.exists(SyndromeGraphPlotter.PNG_DIR)) {
	                Files.createDirectories(SyndromeGraphPlotter.PNG_DIR);
	            }

	            // 執行搬移 (若目標檔存在則覆寫)
	            if (Files.exists(sourcePath)) {
	                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
	                // 寫入 DB 的相對路徑，供前端日後讀取
	                finalGraphPath = "/diffgraph/png/" + fileName;
	            }
	        }
	        
	        if (finalGraphPath != null) {
	            diff.setGraphPath(finalGraphPath);
	        }
	        diffRepository.save(diff);
	    }
	    
	    public DiffResult findLatestDiffById(String identity) {
	        List<DiffResult> diffs = diffRepository.findLatestDiffByDateTime(identity);
	        System.out.println("Service層 - 查詢身分證: " + identity);
	        //如果不為空，回傳第一筆；否則回傳 null
	        if (diffs != null && !diffs.isEmpty()) {
	            return diffs.get(0);
	        }
	        return null;
	    }  

		//分數=1.0 跳過,>=0.8直接加入,<0.8只加入前三個
	    private ArrayList<String> getTopSyndrome(LinkedHashMap<String, Double> result) {
	        int count = 0;
	        ArrayList<String> Top_Syndrome = new ArrayList<>();
	        for (Map.Entry<String, Double> entry : result.entrySet()) {
	            if(entry.getValue()==1.0) {
	                continue;
	            }
	            if (entry.getValue() >= 0.8) {
	                Top_Syndrome.add(entry.getKey());
	            } else if (count < 3) {
	                Top_Syndrome.add(entry.getKey());
	                count++;
	            }
	        }
	        return Top_Syndrome;
	    }
	    
	    //四診猜測症狀用 輸出ssGN是一個二維的arraylist 還沒選症狀前使用 根據opt有不同string[][]
	    private void setData(ArrayList<ArrayList<String>> suggested_symptoms_Group_Name,String[][] sdata) {
	        for(String[] d:sdata) {
	        	suggested_symptoms_Group_Name.add(new ArrayList<>(Arrays.asList(d)));
	        }
	    }
	    
	    //四診猜測症狀用 輸出ssGN是一個二維的arraylist 有選症狀使用 
	    private void setData(ArrayList<ArrayList<String>> suggested_symptoms_Group_Name,ArrayList<Set<Integer>> sdata) {
	        for(int i=0;i<sdata.size();i++) {
	        	//suggested_symptoms_Group_Name.add(converter.get_NAMEs(sdata.get(i)));
	        	suggested_symptoms_Group_Name.add(this.convertNames(sdata.get(i)));
	        }
	    }

	    // 1. 症狀名稱集合 轉 ID集合
	    private SymptomSet convertIDs(ArrayList<String> inputSymptoms) {
	        SymptomSet outputSymptomsID = new SymptomSet(); 
	        Integer id;
	        for(String symptomName : inputSymptoms) {
	            id = symptomTable.getID(symptomName); // 直接呼叫內部的 get_ID
	            if (id != null && !outputSymptomsID.contains(id)) {
	                outputSymptomsID.add(id);
	            }
	        }
	        return outputSymptomsID;   
	    }


	 // 2. 症狀ID集合 轉 名稱集合
	    private ArrayList<String> convertNames(Collection<Integer> inputSymptom) {
	        ArrayList<String> outputSymptomsName = new ArrayList<>();
	        String name;
	        List<Integer> list = new ArrayList<Integer>(inputSymptom);
	        Collections.sort(list); // 排序
	        for(Integer symptom_Id : list) {
	            name = symptomTable.getName(symptom_Id); // 直接呼叫內部的 get_Name
	            if (name != null && !outputSymptomsName.contains(name)) {
	                outputSymptomsName.add(name);
	            }
	        }
	        return outputSymptomsName;   
	    }
	    
	    public List<Cause> getPreloadCauses() {
	        return this.loadCauses;
	    }
}