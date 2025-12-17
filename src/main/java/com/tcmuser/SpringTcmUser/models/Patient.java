package com.tcmuser.SpringTcmUser.models;

import jakarta.persistence.*;
//@Entity → 代表這是一個 JPA 實體類別，Spring Boot 啟動時會讓 Hibernate 掃描它，並建立與資料表的映射關係。
//@Table(name = "patients") → 指定對應資料庫的表格名稱為 patients。
//這層定義從DB抓的資料對應 
//JPA 透過 Model 對應 Table

@Entity  //hibernate實例化
@Table(name = "patients")
public class Patient {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.AUTO)  //配置ID生成策略
	private long id;
	
	//db名稱跟這裡不一樣 要註解
	@Column(name="select_doctor")
	private String selectDoctor;
	
	private String name;
	private String identity;
	
	@Column(name="appointment_date")
	private String appointmentDate;
	@Column(name="appointment_time")
	private String appointmentTime;
	
	public Patient() {
		super();
	}
	public Patient(String select_doctor, String name, String identity, String appointmentDate, String appointmentTime) {
		this.selectDoctor = select_doctor;
		this.name = name;
		this.identity = identity;
		this.appointmentDate = appointmentDate;
		this.appointmentTime = appointmentTime;
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getSelectDoctor() {
		return selectDoctor;
	}


	public void setSelectDoctor(String select_doctor) {
		this.selectDoctor = select_doctor;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getIdentity() {
		return identity;
	}


	public void setIdentity(String identity) {
		this.identity = identity;
	}

	
	public String getAppointmentDate() {
		return appointmentDate;
	}
	
	public void setAppointmentDate(String appointmentDate) {
		this.appointmentDate = appointmentDate;
	}
	
	public String getAppointmentTime() {
		return appointmentTime;
	}
	
	public void setAppointmentTime(String appointmentTime) {
		this.appointmentTime = appointmentTime;
	}
	
	
}
