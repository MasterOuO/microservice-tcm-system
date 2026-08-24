package com.tcmdiff.SpringTcmDiff.controllers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.tcmdiff.SpringTcmDiff.service.DiffService;
import com.tcmdiff.SpringTcmDiff.service.SyndromeGraphPlotter;
import com.tcmdiff.SpringTcmDiff.models.DiffResult;
import com.tcmdiff.SpringTcmDiff.models.Cause;

@RestController
@RequestMapping("/api/diff")
public class DiffController {

	@Autowired
	private DiffService diffService;	
	
	@Autowired
	private SyndromeGraphPlotter syndromeGraphPlotter;
	
	//=====================================User controller的方法拆過來 =======================================
	//url在doctor_page下面const
	// ============================ 選擇主訴 執行這邊辯證計算結果 ============================
	@PostMapping("/compute")
	@ResponseBody
	public Map<String, Object> computeDiff(
			@RequestParam("opt") String opt,
			@RequestParam(value ="symptoms") String inputSymptom,
			@RequestParam(value = "sub_opt") String subOpt) throws Exception{
		// 錯誤參數直接回傳

		if (opt == null || opt.trim().isEmpty() || subOpt == null || subOpt.trim().isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "科別不能為空");
	    }
		double startTime = System.nanoTime();
		Map<String, Object> result = diffService.computeDiff(opt, subOpt, inputSymptom);
		// 有計算分數且高於門檻的才取得他的cause 下拉可以看
		@SuppressWarnings("unchecked")
	    Map<String, Double> computedSyndromes = (Map<String, Double>) result.get("output");
	    
		// map錯誤回傳
		if (computedSyndromes == null) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "輸入錯誤，無法計算");
	    } 
		
		Set<String> computedSyndromeNames = computedSyndromes.keySet();

		List<Cause> allCauses = diffService.getPreloadCauses();
		Map<String, List<String>> syndromeCausesMap = new HashMap<>();
		for (Cause c : allCauses) {
			if (computedSyndromeNames.contains(c.getTo())) {
	            syndromeCausesMap.put(c.getTo(), c.getCauseList()); 
	        }
		}
		result.put("causesData", syndromeCausesMap);
/*
		Path pngPath = diffService.generateGraph(result);		
		//要不要生圖
		if (pngPath != null) {
			String fullPath = pngPath.toString(); 
			String[] parts = fullPath.split("static"); 
			String urlPath = parts[1].replace("\\", "/");
	        result.put("graph", urlPath);
	    } else {
	        result.put("graph", null); 
	    }
*/
		//執行時間
		double timeMs = (System.nanoTime() - startTime) / 1000000.0;
		System.out.println("opt:" + opt);
		System.out.println("subOpt:" + subOpt);
		System.out.println("Symtoms:" + inputSymptom);
		System.out.println("Execution time : " + timeMs);
		System.out.println("-------------------------------------------------------");
		return result;
	}
	
	//@CrossOrigin(origins = "http://localhost:8080")
	@PostMapping("/generateGraph")
	@ResponseBody
	public Map<String, Object> generateGraph(@RequestBody List<Map<String, String>> requestData) {
		double startTime = System.nanoTime();
		Map<String, Object> result = new HashMap<>();

	    try {
	    	// 驗證請求數據
	    	if (requestData == null || requestData.isEmpty()) {
	    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請求為空");
	    	}
	    	
	    	// 驗證數據完整
	    	for (int i = 0; i < requestData.size(); i++) {
	    		Map<String, String> data = requestData.get(i);
	    		if (data == null) {
	    			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "第 " + (i+1) + " 筆數據為空");
	    		}
	    		
	    		String syndrome = data.get("syndrome");
	    		String scoreStr = data.get("score");
	    		
	    		if (syndrome == null || syndrome.trim().isEmpty() || scoreStr == null || scoreStr.trim().isEmpty()) {
	    			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "證候或分數不能為空");
	    		}
	    		
	    		// 驗證有效的數字
	    		try {
	    			Double.parseDouble(scoreStr);
	    		} catch (NumberFormatException e) {
	    			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "第 " + (i+1) + " 筆：分數格式錯誤 ");
	    		}
	    	}
	    	
	    	// 呼叫改寫後的 Service 方法
	    	Path pngPath = diffService.generateGraph(requestData);

	    	//測試打包用路徑 不在static裡面
	    	if (pngPath != null) {
	    		String fileName = pngPath.getFileName().toString(); 
	    		String urlPath = "/diffgraph/tmpPng/" + fileName;
	    		String noCacheUrl = urlPath + "?t=" + System.currentTimeMillis();
	    		
	    		result.put("graphUrl", noCacheUrl); 
	    		result.put("success", true);
	    	} else {
	    		result.put("graphUrl", null);
	    		result.put("success", false); 
	    		result.put("message", "分數錯誤無法生成圖表");
	    	}
	    } catch (ResponseStatusException e) {
	    	// 重新拋出Spring已定義的例外
	    	throw e;
	    } catch (Exception e) {
	    	// 捕獲其他異常（如圖片生成失敗）
	    	throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "圖表生成失敗: " + e.getMessage());
	    }
	    
	    double timeMs = (System.nanoTime() - startTime) / 1000000.0;
		System.out.println("Graph Generate exec time : " + timeMs);
		System.out.println("-------------------------------------------------------");
	    
	    return result;
	}

		
		
	
	//===================================== APIcontroller的方法拆過來 =======================================
	
	//===================================== 儲存辨證結果與分數 =======================================
	/*
	@PostMapping("/save-diff")
	@CrossOrigin(origins = "http://localhost:8090")
	public Map<String, Object> updateDiff(@RequestParam Long id, @RequestParam String name, @RequestParam String identity,@RequestParam String symptoms, @RequestParam String result, @RequestParam String other){
		Map<String, Object> response = new HashMap<>();
		try {
			DiffResult diff = new DiffResult();
			diff.setName(name);
			diff.setIdentity(identity);
			diff.setSymptom(symptoms);
			diff.setResult(result);
			diff.setOther(other);
			diff.setDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			diffRepository.save(diff);
			response.put("success", true);
			response.put("message", "儲存成功");
		}catch(Exception e) {
			response.put("success", false);
			response.put("message", "儲存失敗" + e.getMessage());
		}
		return response;
	}
	*/
	
	//測試可擴充性 先直接連行政
	//@CrossOrigin(origins = "http://localhost:8080")
	@PostMapping("/save-diff")
	public Map<String, Object> updateDiff(
            @RequestParam Long id, 
            @RequestParam String name, 
            @RequestParam String identity,
            @RequestParam String symptoms, 
            @RequestParam String result,  
            @RequestParam String voiceResult, 
            @RequestParam String other,
            @RequestParam(required = false) String tmpGraphPath) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
        	//測試超時
        	if ("TIMEOUT_TEST".equals(name)) {
                throw new RuntimeException("Simulated Timeout for Testing");
            }
            // 將所有參數交給 Service 處理
            diffService.saveDiffRecord(name, identity, symptoms, result, voiceResult, other, tmpGraphPath);
            
            response.put("success", true);
            response.put("message", "儲存成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "儲存失敗: " + e.getMessage());
        }
        
        return response;
    }
		
		
	//===================================== 取得最近一次看診分數和紀錄(前次分數) =======================================
	/*
	@GetMapping("/load-diff")
	@CrossOrigin(origins = "http://localhost:8090")
	public Map<String, Object> getLatestDiff(@RequestParam String identity){
		Map<String, Object> result = new HashMap<>();
		//調用findLat..方法 去DB抓資料，透過Model組成entity物件 返回成list <DiffResult>
		List<DiffResult> diffs = diffRepository.findLatestDiffByDateTime(identity);
		System.out.println("查詢身分證: " + identity);	
		if(!diffs.isEmpty()) {
			DiffResult latest_diff = diffs.get(0);
			System.out.println("查詢結果: " + latest_diff.getDateTime());
			result.put("success", true);
			result.put("symptom", latest_diff.getSymptom());
			result.put("result", latest_diff.getResult());
		}
		else {
			System.out.println("查詢結果: 無");
			result.put("success", false);
		}
		return result;
	}
	*/
	
	//@CrossOrigin(origins = "*")
	@GetMapping("/load-diff")
    public Map<String, Object> getLatestDiff(@RequestParam String identity) {
        Map<String, Object> response = new HashMap<>();
        
        try {
        	//調用findLat..方法 去DB抓資料，透過Model組成entity物件 返回成list <DiffResult>
            DiffResult latestDiff = diffService.findLatestDiffById(identity);
            
            if (latestDiff != null) {
                response.put("success", true);
                response.put("symptom", latestDiff.getSymptom());
                response.put("result", latestDiff.getResult());
            } else {
            	response.put("success", false);
                response.put("message", "無前次分數"); 
            }
        } catch (Exception e) {
            // 預防DB error
            response.put("success", false);
            response.put("message", "系統錯誤: " + e.getMessage());
        }
        return response;
    }

	//開給別的microservice用的API
	@GetMapping("/external/getdiff")
    public ResponseEntity<?> extGetDiff(@RequestParam String identity) {
        try {
            DiffResult latestDiff = diffService.findLatestDiffById(identity);
            
            if (latestDiff != null) {
            	return ResponseEntity.ok(latestDiff);
            } else {
            	return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "無前次分數"));
            }
        } catch (Exception e) {
            // 預防DB error
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "系統錯誤: " + e.getMessage()));
        }
    }
	
	/*
	// OOM測試API
	private static List<byte[]> memoryLeakList = new ArrayList<>();

	@PostMapping("/test/oom")
	public String triggerOOM() {
	    // 每次呼叫塞100KB 
	    memoryLeakList.add(new byte[  1024 * 100]); 
	    return "Added 100KB";
	}
	
	//測試jmeter用
	@GetMapping("/test/patient")
	public String patient() {
	    return "api return";
	}
	
	@GetMapping("/test/biggraph")
    public ResponseEntity<String> generateHeavyGraph() {
        try {
            
            Path dotFilePath = Paths.get("/app/huge-graph.dot"); 
            Path result = syndromeGraphPlotter.dotToPng(dotFilePath);
            
            return ResponseEntity.ok("成功: " + result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("失敗: " + e.getMessage());
        }
    }
    */
}
