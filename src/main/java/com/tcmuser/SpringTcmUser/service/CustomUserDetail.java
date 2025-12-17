package com.tcmuser.SpringTcmUser.service;

import java.util.Collection;
import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tcmuser.SpringTcmUser.models.User;

//service這層 放controller需要呼叫的服務
//存放user資訊，用來封裝 Spring Security 需要的使用者認證訊息

public class CustomUserDetail implements UserDetails{
	
	private User user;
	
	public CustomUserDetail(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		//GrantedAuthority : Authentication 中的Authorities 是一個GrantedAuthority 數組
		//設置被賦予的最高權限，權限是用字串定義，所有的權限字串都用ROLE_ 作為前綴。
		return List.of(() -> user.getRole());
	}

	public String getAccount() {
		return user.getAccount();
	}
	
	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getPassword();
	}

	public String getFullname() {
		return user.getFullname();
	}
	
	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return user.getAccount();
	}
	
	public String getWork_day() {
		return user.getWork_day();
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override 
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override 
	public boolean isEnabled() {
		return true;
	}
	
}
