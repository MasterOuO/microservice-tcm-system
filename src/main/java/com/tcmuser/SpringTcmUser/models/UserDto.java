package com.tcmuser.SpringTcmUser.models;

import jakarta.validation.constraints.*;

public class UserDto {
	
	@NotEmpty(message = "此欄位為必填")
	private String account;
	
	@NotEmpty(message = "此欄位為必填")
	private String fullname;
	
	@NotEmpty(message = "此欄位為必填")
	private String password;
	
	private String work_day;
	private String role;
	
	public UserDto(String account, String fullname, String password, String work_day, String role) {
		this.account = account;
		this.fullname = fullname;
		this.password = password;
		this.work_day = work_day;
		this.role = role;
	}
	
	public UserDto() {
		// TODO Auto-generated constructor stub
	}
	public String getAccount() {
		return account;
	}
	
	public void setAccount(String account) {
		this.account = account;
	}
	
	public String getFullname() {
		return fullname;
	}
	
	public void setFullname(String fullname){
		this.fullname = fullname;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getWork_day() {
		return work_day;
	}
	
	public void setWork_day(String work_day) {
		this.work_day = work_day;
	}
	
	public String getRole() {
		return role;	
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	
}
