package com.tcmuser.SpringTcmUser.models;

import jakarta.validation.constraints.*;

public class PatientDto {

	@NotEmpty(message = "此欄位為必填")
	private String selectDoctor;
	
	@NotEmpty(message = "此欄位為必填")
	private String name;
	
	@NotEmpty(message = "此欄位為必填")
	@Pattern(regexp = "^[A-Za-z][0-9]{9}$", message="身分證格式錯誤")
	private String identity;
	
	private String appointmentDate;
	private String appointmentTime;
	
	public PatientDto(String selectDoctor, String name, String identity, String appointmentDate, String appointmentTime) {
		this.selectDoctor = selectDoctor;
		this.name = name;
		this.identity = identity;
		this.appointmentDate = appointmentDate;
		this.appointmentTime = appointmentTime;
	}

	public PatientDto() {
		super();
	}

	public String getSelectDoctor() {
		return selectDoctor;
	}

	public void setSelectDoctor(String selectDoctor) {
		this.selectDoctor = selectDoctor;
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
