package com.tcmuser.SpringTcmUser.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tcmuser.SpringTcmUser.models.Patient;
import com.tcmuser.SpringTcmUser.repository.PatientRepo;
import com.tcmuser.SpringTcmUser.client.AcuClient;
import com.tcmuser.SpringTcmUser.repository.PatientRepo;

@RestController
public class ApiTestController {
	
	@Autowired
	private PatientRepo patientRepo;
	
	@Autowired
    private AcuClient acuClient;

	@GetMapping("/api-test") // 這裡必須對應 JS 的 fetch("/api-test")
    public Map<String, Object> test(@RequestParam String part) {
        // 這行執行後，Feign 會去幫你找 acupuncture 服務
        return acuClient.fetchPoints(part);
    }
	
	//測試jmeter用
	@GetMapping("/api-test/patient")
	public String patient() {
	    return "api return";
	}
	
	@GetMapping("/api-test/getPatient")
	public String showPatientResult(@RequestParam("identity") String identity, Model model) {
	    // ----- 根據 identity 查 DB -----
	    List<Patient> result = patientRepo.findByIdentity(identity);
	    model.addAttribute("patient", result);
	    return "patient/patientQuery";
	}
	
	// OOM測試API
	private static List<byte[]> memoryLeakList = new ArrayList<>();

	@PostMapping("/api-test/oom")
	public String triggerOOM() {
	    // 每次呼叫塞100KB 
	    memoryLeakList.add(new byte[  1024 * 100]); 
	    return "Added 100KB";
	}
}
