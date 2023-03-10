package com.mab.user.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.mab.user.model.LoginDto;
import com.mab.user.model.UserDto;

public interface UserService {

	public Map<String, Object> login(@Valid LoginDto loginDto, HttpServletResponse response);

	public String logoutOK(HttpServletRequest request);

	public String findId(String user_email);

	public String findPw(String user_id, String user_email);

}
