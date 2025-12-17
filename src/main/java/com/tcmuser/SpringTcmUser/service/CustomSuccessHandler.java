package com.tcmuser.SpringTcmUser.service;
//登入時根據role切換進去的頁面
import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class CustomSuccessHandler implements AuthenticationSuccessHandler{


	// HttpServletRequest裡面封裝server發請求的那些資訊
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException{
		System.out.println("Handler");
		var authourities = authentication.getAuthorities();
		var roles = authourities.stream().map(r -> r.getAuthority()).findFirst();
		
		//Optional<String> 用orElse回傳string
		if(roles.orElse("").equals("admin")) {
			response.sendRedirect("/admin-page");
		}
		else if(roles.orElse("").equals("topadmin")) {
			response.sendRedirect("/admin-page");
		}
		else if(roles.orElse("").equals("doctor")) {
			response.sendRedirect("/doctor-page");
		}
		else {
			response.sendError(404);
		}
	}
}
