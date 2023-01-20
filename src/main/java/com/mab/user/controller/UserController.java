
package com.mab.user.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mab.jwt.JwtFilter;
import com.mab.jwt.TokenProvider;
import com.mab.user.model.LoginDto;
import com.mab.user.model.TokenDto;
import com.mab.user.model.UserEntity;
import com.mab.user.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

@Api(tags = "유저 컨트롤러")
@Slf4j
@RestController
@RequestMapping("/")
public class UserController {

	@Autowired
	UserService service;

	@Autowired
	HttpSession session;

//	@Autowired
//	UserSendEmail authSendEmail;

	// 자동 개행 및 줄 바꿈 (new Gson은 일자로 나옴)
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private final TokenProvider tokenProvider;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;

	public UserController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
		this.tokenProvider = tokenProvider;
		this.authenticationManagerBuilder = authenticationManagerBuilder;
	}

	@PostMapping("/authenticate")
	public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {

		TokenDto tokenDto = new TokenDto();

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				loginDto.getUsername(), loginDto.getPassword());
		
		try {
			Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			String jwt = tokenProvider.createToken(authentication);
			
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
			
			tokenDto.setToken(jwt);
			tokenDto.setResult("1");
			log.info("login success....♡");
			
			return new ResponseEntity<>(tokenDto, httpHeaders, HttpStatus.OK);
		} catch (Exception e) {
			tokenDto.setResult("0");
			log.info("login fail....♡");
			return new ResponseEntity<>(tokenDto, HttpStatus.OK);
		}
		
	}

	// **********************
	// 로그인 완료
	// **********************
	@ApiOperation(value = "로그인 성공", notes = "로그인 성공 입니다")
	@PostMapping("/loginSuccess")
	public String user_loginOK(@RequestParam String username, HttpServletResponse response) {
		log.info("user_loginOK ()...");
		log.info("username: {}", username);

		// 로그인 성공시 기존의 유저관련쿠키 제거
		Cookie cc = new Cookie("user_no", null); // choiceCookieName(쿠키 이름)에 대한 값을 null로 지정
		cc.setMaxAge(0); // 유효시간을 0으로 설정
		response.addCookie(cc); // 응답 헤더에 추가해서 없어지도록 함

		UserEntity uvo = service.user_login_info(username);
		log.info("uvo: {}", uvo);
		Map<String, String> map = new HashMap<String, String>();

		session.setAttribute("user_id", uvo.getUser_id());

		Cookie cookie = new Cookie("user_no", uvo.getUser_no()); // 고유번호 쿠키 저장
		cookie.setPath("/");
		response.addCookie(cookie);

		log.info("User Login success.....");
		map.put("result", "1"); // 로그인 성공

		String jsonObject = gson.toJson(map);

		return jsonObject;
	}

	// **********************
	// 로그인 실패
	// **********************
	@ApiOperation(value = "로그인 실패", notes = "로그인 실패 입니다")
	@PostMapping("/loginFail")
	public String user_loginFail(UserEntity uvo, HttpServletResponse response) {
		log.info("user_loginFail ()...");
		log.info("result: {}", uvo);

		Map<String, String> map = new HashMap<String, String>();

		log.info("User Login failed.....");
		map.put("result", "0"); // 로그인 실패

		String jsonObject = gson.toJson(map);

		return jsonObject;
	}

}// end class
