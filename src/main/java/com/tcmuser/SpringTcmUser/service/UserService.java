package com.tcmuser.SpringTcmUser.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tcmuser.SpringTcmUser.models.User;
import com.tcmuser.SpringTcmUser.models.UserDto;
import com.tcmuser.SpringTcmUser.repository.UserRepo;

@Service
public class UserService {
	private final PasswordEncoder passwordEncoder;
	private final UserRepo userRepository;
	
	public UserService(PasswordEncoder passwordEncoder,UserRepo userRepository) {
		this.passwordEncoder=passwordEncoder;
		this.userRepository=userRepository;
	}
	
	public User save(UserDto userDto) {
		User user = new User(
				userDto.getAccount(), 
				userDto.getFullname(), 
				passwordEncoder.encode(userDto.getPassword()), 
				userDto.getWork_day(), 
				userDto.getRole());
		
		return userRepository.save(user);
	};
	
	/**
	 * 檢查帳號是否已存在
	 * @param account 要檢查的帳號字串
	 * @return 如果帳號已存在返回 true，否則返回 false
	 */
	public boolean isAccountExist(String account) {
		return userRepository.existsByAccount(account);
	};
}
