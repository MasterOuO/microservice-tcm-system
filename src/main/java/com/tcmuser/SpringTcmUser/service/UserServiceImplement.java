package com.tcmuser.SpringTcmUser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tcmuser.SpringTcmUser.models.User;
import com.tcmuser.SpringTcmUser.models.UserDto;
import com.tcmuser.SpringTcmUser.repository.UserRepository;

@Service
public class UserServiceImplement implements UserService{
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public User save(UserDto userDto) {
		//將表單接收到userDto class欄位轉換成User實體(實際的資料表)，userDto只負責傳遞重要資訊，不會傳資料表全部欄位
		User user = new User(
				userDto.getAccount(), 
				userDto.getFullname(), 
				passwordEncoder.encode(userDto.getPassword()), 
				userDto.getWork_day(), 
				userDto.getRole());
		
		return userRepository.save(user);
	}
	
	
	@Override
	public boolean isAccountExist(String account) {
		// 使用 Spring Data JPA 的命名查詢方法檢查帳號是否已存在
		// 假設你的 UserRepository 已經定義了 existsByAccount(String account)
		return userRepository.existsByAccount(account); 
	}
}
