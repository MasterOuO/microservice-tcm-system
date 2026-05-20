package com.tcmuser.SpringTcmUser.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

//name 是他在 Eureka 上的名字
@FeignClient(name = "acupuncture") // 確保這名字在 Eureka 上看得到
public interface AcuClient {

	@GetMapping("/api/acupuncture/get-points")
    Map<String, Object> fetchPoints(@RequestParam("part") String part);
}