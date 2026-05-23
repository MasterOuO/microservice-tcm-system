package com.tcmuser.SpringTcmUser.controllers;

import java.security.Principal;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tcmuser.SpringTcmUser.models.Patient;
import com.tcmuser.SpringTcmUser.models.User;
import com.tcmuser.SpringTcmUser.models.UserDto;
import com.tcmuser.SpringTcmUser.repository.PatientRepo;
import com.tcmuser.SpringTcmUser.repository.UserRepo;
import com.tcmuser.SpringTcmUser.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.validation.Valid;
//===================================== 管理員的控制器(人員CRUD以及預約名單查看) =======================================
//註解controller才會被MVC DispatcherServlet 掃描到，才能綁定 URL
@Controller
public class AdminController {

	@Autowired
	UserDetailsService userDetailsService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepo userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private PatientRepo patientRepository;
	
	
	//========================== 從admin_page.html 前往 createRole page ============================
	@GetMapping("admin-page/createRole")
	public String showCreateDoctor(Model model, Principal principal) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
		model.addAttribute("userDetails", userDetails);
		UserDto userDto = new UserDto();
		model.addAttribute("userDto", userDto);		
		return "doctor/createRole";
	}
	
	//=================== 提交create action(topadmin不能新增 預設只有一個 避免json寫入問題) ========================
	@PostMapping("admin-page/createRole")
	public String createDoctor(@Valid @ModelAttribute UserDto userDto, BindingResult result, Model model, Principal principal) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
		model.addAttribute("userDetails", userDetails);
		
		if(result.hasErrors()) {
			return "doctor/createRole";
		}
		//判斷重複帳號名稱
		if (userService.isAccountExist(userDto.getAccount())) {
			// 如果帳號已存在，手動新增一個錯誤到 BindingResult，並綁定到 "account" 欄位
			result.rejectValue("account", "userDto.account.duplicate", "此帳號已被使用，請更換其他帳號。");
			
			// 返回表單頁面，讓使用者看到錯誤訊息
			return "doctor/createRole";
		}
		
		// 儲存資料進DB
		userService.save(userDto);
		//告訴Spring用admin-page去找模板檔案template找
		return "redirect:/admin-page";
	}
	
	//====================== 從admin_page.html 前往 edit page =====================================
	@GetMapping("admin-page/edit")
	public String showEditDoctor(Model model, @RequestParam long id, Principal principal) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
		model.addAttribute("userDetails", userDetails);
		try {
			User user = userRepository.findById(id).get(); //取得使用者資料
			model.addAttribute("user", user);
			//----- 將使用者資料轉換成表單用的實體(user->userDto) -----
			UserDto userDto = new UserDto();
			userDto.setAccount(user.getAccount()); 
			userDto.setFullname(user.getFullname());
			userDto.setPassword(user.getPassword());
			userDto.setRole(user.getRole());
			model.addAttribute("userDto", userDto);	
		}catch(Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
			return "redirect:/admin-page";
		}		
		return "doctor/editDoctor"; 
	}
	
	//================================ 提交edit action(和post create同理) ==================================
	@PostMapping("admin-page/edit")
	public String updateDoctor(Model model, @RequestParam long id,  @Valid @ModelAttribute UserDto userDto, BindingResult result,  Principal principal) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
		model.addAttribute("userDetails", userDetails);
		
		try {			
			User user = userRepository.findById(id).get();
			model.addAttribute("user", user);
			if(result.hasErrors()) {
				return "doctor/editDoctor";
			}
			user.setAccount(userDto.getAccount());
			user.setFullname(userDto.getFullname());
			user.setPassword(passwordEncoder.encode(userDto.getPassword()));
			user.setRole(userDto.getRole());			
			userRepository.save(user);				
		}catch(Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}				
		return "redirect:/admin-page";
	}
	
	//============================== 從admin_page.html 前往 排班頁面 ==============================
	@GetMapping("admin-page/schedule")
	public String showDoctorSchedule(Model model, Principal principal) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
		model.addAttribute("userDetails", userDetails);
		List<User> doctors = userRepository.findByRole("doctor");
		model.addAttribute("doctors", doctors);
		return "doctor/doctorSchedule";
	}
	
	//============================= 提交排班 action(和post create同理) =================================
	@PostMapping("admin-page/schedule")
	public String updateDoctorSchedule(Model model, @RequestParam long doctorId, @RequestParam("scheduleJson") String scheduleRaw,  RedirectAttributes redirectAttributes, Principal principal ) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
		model.addAttribute("userDetails", userDetails);
		User user = userRepository.findById(doctorId).get();
		System.out.println("received: " + user.getFullname());
		model.addAttribute("user",user);
		user.setWork_day(scheduleRaw);
		userRepository.save(user);
		return "redirect:/admin-page/schedule";
	}
	
	//============================= 刪除 重導向回admin-page(return 接 html; redirect 接 url) ==========================
	@GetMapping("admin-page/delete")
	public String deleteDoctor(@RequestParam long id, Principal principal) {
		try {
			User user = userRepository.findById(id).get();
			userRepository.delete(user);			
		}catch(Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}		
		return "redirect:/admin-page";
	}
	
	//============================== 管理員查看所有預約紀錄 ======================================
	@GetMapping("/all_Patient")
	public String getAllPatient(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,Model model, Principal principal) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
		
		Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentDate").descending());   //定義排序參數格式 用pagerequest實作出來 
		Page<Patient> patientPage = patientRepository.findAll(pageable);  //把全部patient封裝進page 
		
		//List<Patient> patient = patientRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));	
		//model.addAttribute("userDetails", userDetails);
		//model.addAttribute("patient", patient);
		
		model.addAttribute("userDetails", userDetails);   //取得正在使用的人 前端右上顯示
		model.addAttribute("patient", patientPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", patientPage.getTotalPages());
		model.addAttribute("size", size);
		return "admin/admin_get_patient";
	}
}
