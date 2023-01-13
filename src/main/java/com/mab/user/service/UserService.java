package com.mab.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mab.user.model.UserEntity;
import com.mab.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService implements UserDetailsService {
	
	@Autowired
	UserRepository repository;
	
	//spring security - UserDetailsService
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("username:{}",username);
		UserEntity user = repository.findByUser_id(username);
		log.info("user:{}",user);
		
		if(user==null) throw new UsernameNotFoundException("Not founc account.");
		
		return createUser(user);
	}
	
	 private org.springframework.security.core.userdetails.User createUser(UserEntity backoffice) {
	     List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
	      return new org.springframework.security.core.userdetails.User(backoffice.getUsername(),
	    		  backoffice.getPassword(),
	              grantedAuthorities);
	   }
	
	//로그인
	public UserEntity user_login_info(String username) {
		log.info("user_login_info()....");
		return repository.user_login_info(username);
	}

	//아이디 찾기-사용자 이메일 불러오기
	public UserEntity user_email_select(UserEntity uvo) {
		log.info("user_id_email_select()....");
		log.info("uvo: {}", uvo);
		return repository.user_email_select(uvo.getUser_email());
	}
	
	
	public UserEntity select_user_info(String user_no) {
		UserEntity vo = repository.SQL_SELECT_ONE(user_no);
		
		return vo;
	}

	//비밀번호 찾기 - 아이디 이메일 불러오기
	public UserEntity user_id_email_select(UserEntity uvo) {
		log.info("user_id_email_select()....");
		log.info("uvo: {}", uvo);
		return repository.user_id_email_select(uvo.getUser_id(), uvo.getUser_email());
	}

	//비밀번호 찾기 - 비번 초기화 저장
	public int user_pw_init(UserEntity uvo) {
		log.info("user_pw_init()....");
		log.info("uvo: {}", uvo);
		return repository.user_pw_init(uvo.getPassword(), uvo.getUser_no());
	}
	
	//회원가입 - 아이디 중복체크
	public UserEntity idCheckOK(UserEntity uvo) {
		log.info("idCheckOK()....");
		log.info("uvo: {}", uvo);

		return repository.idCheckOK(uvo.getUser_id());
	}
	
	//마이페이지- 회원 정보 불러오기
	public UserEntity user_mypage_select(UserEntity uvo) {
		log.info("user_mypage_select()....");
		log.info("uvo: {}", uvo);
		return repository.user_mypage_select(uvo.getUser_no());
//		return repository.user_mypage_select("U1001");
	}
	
	
}//end class
