package com.tcmuser.SpringTcmUser.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tcmuser.SpringTcmUser.models.User;
import com.tcmuser.SpringTcmUser.repository.UserRepository;

//用來為Spring Security提供使用者認證所需的資料。他只做一件事情 從資料庫中查找使用者
//操作UserDetail實作封裝的obj

@Service
public class CustomUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		User user = userRepository.findByAccount(username);
		
		if(user == null) {
			System.out.println("not found");
			throw new UsernameNotFoundException("user not found");
		}
		
		return new CustomUserDetail(user); //讓customUserDetail實作UserDetail介面，封裝使用者的資料
	}
	
}
