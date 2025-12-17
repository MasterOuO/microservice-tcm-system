package com.tcmuser.SpringTcmUser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tcmuser.SpringTcmUser.models.User;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	//自定義JPA中的function
	User findByAccount(String account);
	
	List<User> findByRole(String role);
	
	boolean existsByAccount(String account);
	
}
