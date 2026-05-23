package com.tcmuser.SpringTcmUser.controllers;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tcmuser.SpringTcmUser.models.Patient;
import com.tcmuser.SpringTcmUser.models.PatientDto;
import com.tcmuser.SpringTcmUser.models.User;
import com.tcmuser.SpringTcmUser.models.UserDto;
import com.tcmuser.SpringTcmUser.repository.PatientRepo;
import com.tcmuser.SpringTcmUser.repository.UserRepo;

import jakarta.validation.Valid;

@Controller
public class PatientController {

	@Autowired
	UserDetailsService userDetailsService;	
	@Autowired
	private PatientRepo patientRepository;
	@Autowired 
	private UserRepo userRepository;
	
	//================================== 病患預約看診(新增預約看診資料) =================================
	@PostMapping("/createPatient")
	public String createPatient(@ModelAttribute PatientDto patientDto, BindingResult createResult, Model model, RedirectAttributes redirectAttributes) {
		
		List<User> doctors = userRepository.findByRole("doctor"); //用於下列式選單顯示醫生
		model.addAttribute("doctors", doctors);
		System.out.println("開始新增預約資料");
		if(createResult.hasFieldErrors("identity")) {
			return "login";
		}
		if(patientDto.getIdentity() == null || patientDto.getIdentity().isEmpty()) {
	        createResult.rejectValue("identity", null, "此欄位為必填");
	        System.out.println("未填欄位");
	    } 
		else if (!patientDto.getIdentity().matches("^[A-Za-z][0-9]{9}$")) {
	        createResult.rejectValue("identity", null, "身分證格式錯誤");
	        System.out.println("格式錯誤");
	    }
		if (createResult.hasErrors()) {
			System.out.println("預約表單錯誤");
			model.addAttribute("formWithErrors", "createPatient");
	        return "patient/appointment";
	    }
		Patient patient = new Patient();
		patient.setSelectDoctor(patientDto.getSelectDoctor());
		patient.setName(patientDto.getName());
		patient.setIdentity(patientDto.getIdentity());
		patient.setAppointmentDate(patientDto.getAppointmentDate());
		patient.setAppointmentTime(patientDto.getAppointmentTime());
		
		patientRepository.save(patient);
		
		System.out.println("儲存成功");
		redirectAttributes.addFlashAttribute("successMessage", "預約成功!");
		return "redirect:/appointment";
	}

	// ========================= patientEdit.html 取消動作後呼叫此function =========================
	@GetMapping("/getPatient")
	public String showPatientResult(@RequestParam("identity") String identity, Model model) {
	    // ----- 根據 identity 查 DB -----
	    List<Patient> result = patientRepository.findByIdentity(identity);
	    model.addAttribute("patient", result);
	    return "patient/patientQuery";
	}

	// ============================ 病患根據身分證查詢自己的預約看診資訊 =====================================
	@PostMapping("/getPatient")
	public String getPatient(@RequestParam("identity") String identity, @ModelAttribute PatientDto patientDto, Model model, BindingResult queryResult) {
		List<User> doctors = userRepository.findByRole("doctor"); //用於下列式選單顯示醫生
		model.addAttribute("doctors", doctors);
		if(queryResult.hasFieldErrors("identity")) {
			return "patient/appointment";
		}
		if(patientDto.getIdentity() == null || patientDto.getIdentity().isEmpty()) {
	        queryResult.rejectValue("identity", null, "此欄位為必填");
	        System.out.println("未填欄位");
	    } 
		else if (!patientDto.getIdentity().matches("^[A-Za-z][0-9]{9}$")) {
	        queryResult.rejectValue("identity", null, "身分證格式錯誤");
	        System.out.println("格式錯誤");
	    }
		if (queryResult.hasErrors()) {
			System.out.println("查詢表單錯誤");
			model.addAttribute("formWithErrors", "getPatient");
	        return "patient/appointment";
	    }
		List<Patient> patient = patientRepository.findByIdentity(identity);
		if(!patient.isEmpty()) {
			 model.addAttribute("patient", patient);
			System.out.println("identity received : " + identity);
		}
		else {
			System.out.println("沒有預約紀錄");
			model.addAttribute("errorMessage", "查無此病患資料!");
		}
		System.out.println("回傳病患資料");
		return "patient/patientQuery";
	}	
	
	//============================ 編輯預約看診(html) ===========================
	@GetMapping("/editPatient")
	public String editPatient(@RequestParam long id, Model model) {
		System.out.println("進入編輯預約畫面");
		try {
			Patient patient = patientRepository.findById(id).get();
			model.addAttribute("patient", patient);
			
			PatientDto patientDto = new PatientDto();
			patientDto.setSelectDoctor(patient.getSelectDoctor());
			patientDto.setName(patient.getName());
			patientDto.setIdentity(patient.getIdentity());
			patientDto.setAppointmentDate(patient.getAppointmentDate());
			patientDto.setAppointmentTime(patient.getAppointmentTime());
			
			List<User> doctors = userRepository.findByRole("doctor");
			
			model.addAttribute("doctors",doctors);
			model.addAttribute("patientDto", patientDto);
		}catch(Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
			return "redirect:/patientQuery";
		}
		return "patient/patientEdit";
	}
	
	//============================ 編輯預約看診(提交) ===========================
	@PostMapping("/editPatient")
	public String editPatient(@RequestParam long id, @Valid @ModelAttribute PatientDto patientDto, Model model, BindingResult result) {
		System.out.println("編輯預約資料開始");
		try {
			Patient patient = patientRepository.findById(id).get();
			//DTO塞回patient
			patient.setSelectDoctor(patientDto.getSelectDoctor());
			patient.setName(patientDto.getName());
			patient.setIdentity(patientDto.getIdentity());
			patient.setAppointmentDate(patientDto.getAppointmentDate());
			patient.setAppointmentTime(patientDto.getAppointmentTime());
			patientRepository.save(patient);		
			//更新資料放入model
			model.addAttribute("patient", patient);
			return "redirect:/getPatient?identity=" + patient.getIdentity();
		}catch(Exception ex) {
			System.out.println("Execption: " + ex.getMessage());
		}
		System.out.println("編輯預約資料修改成功");
		return "patient/patientQuery";
	}
	
	//============================ 刪除預約看診 ===========================
	/*@GetMapping("/deletePatient")
	public String deletePatient(@RequestParam long id, Model model) {
		System.out.println("刪除預約資料開始");
		try {
			Patient patient = patientRepository.findById(id).get();
			patientRepository.delete(patient);
			System.out.println("刪除成功");
		}catch(Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
		model.addAttribute("errorMessage", "查無此病患資料!");
		System.out.println("回傳更新後資料");
		return "patient/patientQuery";
	}
	*/
	@GetMapping("/deletePatient")
	public String deletePatient(@RequestParam long id, RedirectAttributes redirectAttributes) {
	    System.out.println("刪除預約資料開始");
	    try {
	        Patient patient = patientRepository.findById(id).orElse(null);
	        if (patient != null) {
	            String identity = patient.getIdentity(); 
	            patientRepository.delete(patient);
	            System.out.println("刪除成功");
	            return "redirect:/getPatient?identity=" + identity;
	        } else {
	            redirectAttributes.addFlashAttribute("errorMessage", "查無此病患資料!");
	        }
	    } catch (Exception ex) {
	        System.out.println("Exception: " + ex.getMessage());
	        redirectAttributes.addFlashAttribute("errorMessage", "刪除發生錯誤: " + ex.getMessage());
	    }
	    return "redirect:/patientQuery"; 
	}

	
	
	
}
