package com.tcmuser.SpringTcmUser.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "DIFFERENTIATION") 
public interface DiffClient {

    // 定義你想抓取的 API 路徑與參數
	@PostMapping("/api/diff/compute")
    Map<String, Object> getComputeData(
        @RequestParam("opt") String opt,
        @RequestParam("symptoms") String symptoms,
        @RequestParam(value = "sub_opt", required = false) String subOpt
    );
	
}