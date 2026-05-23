package com.tcmuser.SpringTcmUser.initializer;


import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tcmuser.SpringTcmUser.models.User;
import com.tcmuser.SpringTcmUser.repository.UserRepo;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner{
	private final UserRepo userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public DataInitializer(UserRepo userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override  //varargs可以0~多個string
	public void run(String... args) {
		//========================== 最高層級行政人員初始化 ==============================
		List<User> user = userRepository.findByRole("topadmin");
		if (user.isEmpty()) {
			User topadmin = new User();
			topadmin.setAccount("topadmin");
			topadmin.setFullname("最高管理員");
			topadmin.setPassword(passwordEncoder.encode("topadmin"));
			topadmin.setRole("topadmin");
			userRepository.save(topadmin);	
			
			System.out.println("已經初始化topadmin");
		} else {
			System.out.println("沒有初始化topadmin");
		}
	}
}