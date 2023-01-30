package com.mab.user.dao;

import javax.validation.constraints.NotNull;

import com.mab.user.model.UserDto;

public interface UserDAO {

	public UserDto user_login_info(@NotNull String username);

}
