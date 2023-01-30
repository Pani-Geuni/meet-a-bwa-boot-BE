
package com.mab.user.controller;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mab.config.RedisConfig;
import com.mab.jwt.JwtFilter;
import com.mab.jwt.RedisDao;
import com.mab.jwt.TokenProvider;
import com.mab.user.model.LoginDto;
import com.mab.user.model.TokenDto;
import com.mab.user.model.UserDto;
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

	@Autowired
	HttpSession session;

//	@Autowired
//	UserSendEmail authSendEmail;

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
	
	@ApiOperation(value = "로그아웃", notes = "로그아웃 - 성공/실패")
	@GetMapping("/logoutOK")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		
		String result = service.logoutOK(request);

	    return ResponseEntity.ok().body(result);
	}


}// end class
