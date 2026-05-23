package com.tcmuser.SpringTcmUser.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmuser.SpringTcmUser.models.User;
import com.tcmuser.SpringTcmUser.repository.UserRepo;

// ============================ AJAX 非同步更新、API取得資料 =======================================
@RestController
@RequestMapping("/api")
public class ApiController {

	@Autowired //根據類型找到對象完成注入
	private UserRepo userRepository;
	

	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	//============================= 回填醫生的排班時間 ===================================
	@GetMapping("/schedule")
	public Map<String, Object> getDoctorSchedule(@RequestParam Long doctorId){
		Map<String, Object> result = new HashMap<>();
		User doctor = userRepository.findById(doctorId).orElse(null);
		
		if(doctor == null) {
			result.put("success", false);
			result.put("schedule", "{}");
			result.put("message", "找不到對應醫師 ID");
		}
		else {  //選擇醫師後出現頁面
			result.put("success", true);
			result.put("message", "測試api");
			result.put("schedule", doctor.getWork_day() == null ? "{}" : doctor.getWork_day());
		}
		return result;
	}
	
	//============================== 預約看診表單列出對應醫生有上班的日期 =============================
	@GetMapping("/{doctorId}/dates")
	public Map<String, Object> getAvaliableDate(@PathVariable Long doctorId){
		Map<String, Object> result = new HashMap<>();
		User doctor = userRepository.findById(doctorId).orElse(null);
		//----- 查無此醫生 -----
		if(doctor == null) {
			result.put("success", false);
			result.put("message", "找不到該醫生");
			result.put("dates", Collections.emptyList());
			return result;
		}
		//----- 未排工作時間 -----
		String json = doctor.getWork_day();
		if(json == null || json.trim().isEmpty()) {
			result.put("success", true);
			result.put("dates", Collections.emptyList());
			return result;
		}
		
		Map<String, List<List<String>>> scheduleMap;
		//------ Parse JSON -----
		try {
			scheduleMap = mapper.readValue(json, new TypeReference<Map<String, List<List<String>>>>(){});
		}catch(Exception e){
			result.put("success", false);
			result.put("message", "排班JSON解析失敗");
			result.put("dates", Collections.emptyList());
			return result;
		}
		
		LocalDate start = LocalDate.now();
		LocalDate end = start.plusDays(30); //假設最多預約一個月內
		
		List<String> availableDateList = new ArrayList<>();
		while(!start.isAfter(end)) {
			int dow = start.getDayOfWeek().getValue(); //1..7(Mon=1, Sun=7)
			String key = String.valueOf(dow);
			
			//判斷ScheduleMap中有無key，以及對應的shifts是否全部為[]
			if(scheduleMap.containsKey(key)) {
				List<List<String>> shifts = scheduleMap.get(key);
				//shifts = [ [morningStart, end], [afternoonStart, end], [eveningStart, end] ]
				//若三個時間段皆為空 => [[], [], []] => 表示雖然是這個星期，但當天沒上班
				boolean hasAnyShift = shifts.stream().anyMatch(arr -> (arr.size() == 2));
				if(hasAnyShift) {
					availableDateList.add(start.toString());
				}
			}
			start = start.plusDays(1);
		}
		result.put("success", true);
		result.put("dates", availableDateList);
		return result;
	}
	
	//===================================== 預約看診表單列出醫生排班時間 =======================================
	@GetMapping("/{doctorId}/times")
	public Map<String, Object> getAvailableTime(@PathVariable Long doctorId, @RequestParam String date){
		Map<String, Object> result = new HashMap<>();
		User doctor = userRepository.findById(doctorId).orElse(null);
		//----- 查無此醫生 -----
		if(doctor == null) {
			result.put("success", false);
			result.put("timeSlots", Collections.emptyList());
			return result;
		}
		
		//----- 未排工作時間 -----
		String json = doctor.getWork_day();
		if(json ==  null || json.trim().isEmpty()) {
			result.put("success", true);
			result.put("timeSlots", Collections.emptyList());
			return result;
		}
		
		//------ Parse Json -----
		Map<String, List<List<String>>> scheduleMap;
		try {
			scheduleMap = mapper.readValue(json, new TypeReference<Map<String, List<List<String>>>>(){});
		}catch(Exception e) {
			result.put("success", false);
			result.put("timeSlots", Collections.emptyList());
			return result;
		}
		
		LocalDate theDay = LocalDate.parse(date);
		int dow = theDay.getDayOfWeek().getValue(); //e.g. "2025-02-22"
		String key = String.valueOf(dow); //Monday=1..Sunday=7
		List<String> timeSlots = new ArrayList<>();
		if(scheduleMap.containsKey(key)) {
			List<List<String>> shifts = scheduleMap.get(key);
			//shifts[x] => ["08:00", "12:00"] or []
			for(List<String> arr : shifts) {
				if(arr.size() == 2) {
					String startStr = arr.get(0);
					String endStr = arr.get(1);
					timeSlots.add(startStr + "~" + endStr);
				}
			}
		}
		result.put("success", true);
		result.put("timeSlots", timeSlots);
		return result;
		}
	
}
