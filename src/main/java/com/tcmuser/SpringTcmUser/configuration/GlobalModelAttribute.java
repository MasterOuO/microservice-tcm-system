package com.tcmuser.SpringTcmUser.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttribute {

    @ModelAttribute("base")
    public String base(HttpServletRequest request) {
    	// 【單一入口模式】
        // 所有頁面一律假設從 Gateway 進來
    	
        return "/tcm/patients";
    }
}