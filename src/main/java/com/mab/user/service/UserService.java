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
public class UserService implements UserDetailsService {

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

	/**
	 * 로그인 성공 후 정보 가져오기
	 * 
	 */
	public UserDto user_login_info(String username) {
		log.info("user_login_info()....");
		
		UserDto uvo = new UserDto();
		
		UserEntity ue = repository.user_login_info(username);
		
		if (ue != null) {
			uvo = modelMapper.map(ue, UserDto.class);
		} else {
			uvo = null;
		}
		
		return uvo;
	}

	/**
	 * 아이디 찾기-사용자 이메일 불러오기
	 * 
	 */
	public UserDto user_email_select(UserDto uvo) {
		log.info("user_id_email_select()....");
		
		UserEntity ue = repository.user_email_select(uvo.getUser_email());
		
		if(ue!=null) {
			uvo = modelMapper.map(ue, UserDto.class);
		} else {
			uvo = null;
		}
		
		return uvo;
	}

	public UserDto select_user_info(String user_no) {
		
		UserDto uvo = new UserDto();
		
		UserEntity ue = repository.SQL_SELECT_ONE(user_no);

		if(ue!=null) {
			uvo = modelMapper.map(ue, UserDto.class);
		} else {
			uvo = null;
		}
		
		return uvo;
	}

	/**
	 * 비밀번호 찾기 - 아이디 이메일 불러오기
	 * 
	 */
	public UserDto user_id_email_select(UserDto uvo) {
		log.info("user_id_email_select()....");
		
		UserEntity ue = repository.user_id_email_select(uvo.getUser_id(), uvo.getUser_email());
		
		if(ue!=null) {
			uvo = modelMapper.map(ue, UserDto.class);
		} else {
			uvo = null;
		}
		
		return uvo;
	}

	/**
	 * 비밀번호 찾기 - 비번 초기화 저장
	 * 
	 */
	public int user_pw_init(UserDto uvo) {
		log.info("user_pw_init()....");
		return repository.user_pw_init(uvo.getPassword(), uvo.getUser_no());
	}

	/**
	 * 회원가입 - 아이디 중복체크
	 * 
	 */
	public UserDto idCheckOK(UserDto uvo) {
		log.info("idCheckOK()....");

		UserEntity ue = repository.idCheckOK(uvo.getUser_id());
		
		if(ue!=null) {
			uvo = modelMapper.map(ue, UserDto.class);
		} else {
			uvo = null;
		}
		
		return uvo;
	}

	/**
	 * 마이페이지- 회원 정보 불러오기
	 * 
	 */
	public UserDto user_mypage_select(UserDto uvo) {
		log.info("user_mypage_select()....");
		
		UserEntity ue = repository.user_mypage_select(uvo.getUser_no());
		
		if(ue!=null) {
			uvo = modelMapper.map(ue, UserDto.class);
		} else {
			uvo = null;
		}
		
		return uvo;
	}

}// end class
