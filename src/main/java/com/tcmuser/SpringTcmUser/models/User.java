package com.tcmuser.SpringTcmUser.models;

import jakarta.persistence.*;

@Entity  //unique除了id以外哪些也不能重複
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "account"))
public class User {
		
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)  //id沒有值 DB自動生成
	private long id;	
	
	private String account;
	private String fullname;
	private String password;	
	
	@Lob
	private String work_day;
	private String role;
	
	public User() {
		super();
	}
	
	public User(String account, String fullname, String password, String work_day, String role) {
		this.account = account;
		this.fullname = fullname;
		//this.email = email;
		this.password = password;
		this.work_day = work_day;
		this.role = role;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public void setFullname(String fullname) {
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
