
package com.mab.user.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mab.jwt.JwtFilter;
import com.mab.user.model.LoginDto;
import com.mab.user.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Api(tags = "유저 컨트롤러")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserService service;

//	@Autowired
//	HttpSession session;

	/**
	 * 로그인
	 * 
	 */
	@ApiOperation(value = "로그인", notes = "로그인 - 성공/실패")
	@PostMapping(path="/login", produces="application/json; charset=UTF8")
	public ResponseEntity<Object> authorize(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
		
		Map<String, Object> map = service.login(loginDto, response);
		
		if(map.get("jwt")!=null) {
			return ResponseEntity.ok().header(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + map.get("jwt")).body(map.get("tokenDto"));
		} else {
			return ResponseEntity.ok().body(map.get("tokenDto"));
		}

	}
	
	/**
	 * 로그아웃
	 * 
	 */
	@ApiOperation(value = "로그아웃", notes = "로그아웃 - 성공/실패")
	@GetMapping("/logoutOK")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		
		String result = service.logoutOK(request);

	    return ResponseEntity.ok().body(result);
	}
	
	/**
	 * 아이디 찾기
	 * 
	 */
	@ApiOperation(value = "아이디", notes = "아이디 찾기")
	@GetMapping("/search/id")
	public ResponseEntity<String> findId(String user_email) {
		
		String result = service.findId(user_email);
		
		return ResponseEntity.ok().body(result);
	}
	
	/**
	 * 비밀번호 찾기
	 * 
	 */
	@ApiOperation(value = "비밀번호", notes = "비밀번호 찾기")
	@PutMapping("/search/pw")
	public ResponseEntity<String> findPw(String user_id, String user_email) {
		
		String result = service.findPw(user_id, user_email);
		
		return ResponseEntity.ok().body(result);
	}


}// end class
