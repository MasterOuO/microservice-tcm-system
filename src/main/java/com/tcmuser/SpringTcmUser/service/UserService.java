package com.tcmuser.SpringTcmUser.service;

import com.tcmuser.SpringTcmUser.models.User;
import com.tcmuser.SpringTcmUser.models.UserDto;
//定義有哪些方法可以用，Controller 只依賴介面
public interface UserService {
	User save(UserDto userDto);
	
	
	/**
	 * 檢查帳號是否已存在
	 * @param account 要檢查的帳號字串
	 * @return 如果帳號已存在返回 true，否則返回 false
	 */
	boolean isAccountExist(String account);
}
