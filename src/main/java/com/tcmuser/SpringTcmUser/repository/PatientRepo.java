package com.tcmuser.SpringTcmUser.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tcmuser.SpringTcmUser.models.Patient;

@Repository
public interface PatientRepo extends JpaRepository<Patient, Long>{

	//自訂義function用身分證找病患資料 
	//findBy 一些JPA的資料庫語法
	List<Patient> findByIdentity(String identity);
	
	List<Patient> findBySelectDoctor(String select_doctor);
	
}
