package com.mab.user.dao;

import javax.validation.constraints.NotNull;

import com.mab.user.model.UserDto;

public interface UserDAO {

	public UserDto user_login_info(@NotNull String username);

	public UserDto user_id_select(String user_email);

	public UserDto user_pw_select(String user_id, String user_email);

	public int user_pw_init(UserDto uvo);

}
