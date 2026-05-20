package com.tcmuser.SpringTcmUser.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tcmuser.SpringTcmUser.client.AcuClient;

@RestController
public class ApiTestController {
	
	@Autowired
    private AcuClient acuClient;

	@GetMapping("/api-test") // 這裡必須對應 JS 的 fetch("/api-test")
    public Map<String, Object> test(@RequestParam String part) {
        // 這行執行後，Feign 會去幫你找 acupuncture 服務
        return acuClient.fetchPoints(part);
    }
}
