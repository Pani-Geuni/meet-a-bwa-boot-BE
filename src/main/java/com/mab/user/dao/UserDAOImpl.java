package com.mab.user.dao;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mab.user.model.UserDto;
import com.mab.user.model.UserEntity;
import com.mab.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class UserDAOImpl implements UserDAO{

	@Autowired
	UserRepository repository;
	
	@Autowired
	ModelMapper modelMapper;

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
	 * 아이디 찾기
	 * 
	 */
	public UserDto user_id_select(String user_email) {
		log.info("user_id_select()....");
		
		UserDto uvo = new UserDto();
		
		UserEntity ue = repository.user_id_select(user_email);
		
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
	 * 비밀번호 찾기 
	 * 
	 */
	public UserDto user_pw_select(String user_id, String user_email) {
		
		UserDto uvo = new UserDto();
		
		UserEntity ue = repository.user_pw_select(user_id, user_email);
		
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

}
