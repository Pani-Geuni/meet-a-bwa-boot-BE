package com.mab.user.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mab.user.model.UserDto;
import com.mab.user.model.UserEntity;
import com.mab.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserSecurity implements UserDetailsService {

	@Autowired
	UserRepository repository;
	
	@Autowired
	ModelMapper modelMapper;

	/**
	 * spring security - UserDetailsService
	 * 
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("username:{}", username);
		UserDto user = new UserDto();
		
		UserEntity ue = repository.findByUser_id(username);
		log.info("user:{}", ue);

		if (ue == null) {
			throw new UsernameNotFoundException("Not founc account.");
		} else {
			user = modelMapper.map(ue, UserDto.class);
		}
		return createUser(user);
	}

	private org.springframework.security.core.userdetails.User createUser(UserDto backoffice) {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		return new org.springframework.security.core.userdetails.User(backoffice.getUsername(),
				backoffice.getPassword(), grantedAuthorities);
	}


}// end class
