package com.tcmuser.SpringTcmUser.controllers;

import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.tcmuser.SpringTcmUser.models.Patient;
import com.tcmuser.SpringTcmUser.models.PatientDto;
import com.tcmuser.SpringTcmUser.models.User;
import com.tcmuser.SpringTcmUser.models.UserDto;
import com.tcmuser.SpringTcmUser.repository.PatientRepo;
import com.tcmuser.SpringTcmUser.repository.UserRepo;
import com.tcmuser.SpringTcmUser.service.CustomUserDetail;
import com.tcmuser.SpringTcmUser.service.UserService;
import com.tcmuser.SpringTcmUser.client.DiffClient;


//有新增疾病要去
//(1)前端更新patient_event_handler.js的顯示選項
//(2)前端更新Symptom_Data.js的症狀列表
//(3)後端更新UserController的doctorPage()中的opt顯示選項 以及 initDiffModel()的辨證資料檔案路徑與頁面模板的注入資料
//(4)後端更新DiffService的辨證器實例化

@Controller
public class UserController {
	
	@Autowired
	UserDetailsService userDetailsService;
	
	@Autowired
	private UserService userService;
	
	@Autowired 
	private UserRepo userRepository;
	
	@Autowired
	private PatientRepo patientRepository;
	
	//辯證服務
	@Autowired
    private DiffClient diffClient;

	
	//======================== 在API做測試用 & 初始新增admin =================================
	@PostMapping("/register")
	public ResponseEntity<UserDto> saveUser(@RequestBody UserDto userDto) {
		userService.save(userDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
	}
	
	//======================== 行政、醫生登入表單 ==================================
	@GetMapping("/login")
	public String login(@ModelAttribute("user") UserDto userDto, Model model) {
	    System.out.println("welcome");
	    return "login";
	}
	
	//========================== 病患預約表單 =====================================
	@GetMapping("/appointment")
	public String appointment(PatientDto patientDto, Model model) {
		List<User> doctors = userRepository.findByRole("doctor");
		model.addAttribute("doctors", doctors);
		return "patient/appointment";
	}

	
	//================================= 根據登入(login.html)帳號進到管理員頁面 ======================================
	//Principle : 代表使用者的物件，使用者並不僅是用戶，也可以是裝置或可以在應用程式中執行某種操作的其他系統。
	@GetMapping("admin-page")
	public String adminPage(Model model, Principal principal) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName()); //principal call UserDetails.getUsername()
		//System.out.println("continue");
		List<User> users = userRepository.findAll(Sort.by(Sort.Direction.ASC,"id"));
		model.addAttribute("userDetails", userDetails);
		model.addAttribute("users", users);
		return "admin/admin_page";
	}
	
	
	//================================= 根據登入(login.html)帳號進到醫生頁面 ======================================
	@GetMapping("doctor-page")
	public String doctorPage(Model model, Principal principal) {
		System.out.println("8090近來doctorPage邏輯");
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
		model.addAttribute("userDetails", userDetails);
		
		System.out.println("------ 開始取得科別和病患資料 -------");
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formatterDate = currentDate.format(formatter);
		//userDetails編譯期間找不到getFullname()，customDetails才可以
		CustomUserDetail customDetails = (CustomUserDetail) userDetails;
		List<Patient> all_patients = patientRepository.findBySelectDoctor(customDetails.getFullname());
		
		List<Patient> patients = all_patients.stream()
				.filter(p -> formatterDate.equals(p.getAppointmentDate()))
				.collect(Collectors.toList());
		
		
		String[][] opts = { {"DE", "內科"}, 
                			{"OG", "婦科"}, 
                			{"PS", "精神科"},{"ICD", "ICD內科"}};
		
		model.addAttribute("opts", opts);
		System.out.println("opt:" + opts);
		model.addAttribute("diff_url", "/api/diff/load-diff/compute");
		model.addAttribute("patients",patients);
		
		System.out.println("--- UI 服務接收前端請求，準備轉發給辨證服務 ---");
		return "doctor/doctor_page";
	}


}
