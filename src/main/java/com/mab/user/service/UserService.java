package com.mab.user.service;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.mab.user.model.LoginDto;
import com.mab.user.model.UserDto;

public interface UserService {

	public Map<String, Object> login(@Valid LoginDto loginDto, HttpServletResponse response);

}
