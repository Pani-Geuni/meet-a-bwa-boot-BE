
package com.mab.user.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
import com.mab.config.RedisConfig;
import com.mab.jwt.JwtFilter;
import com.mab.jwt.TokenProvider;
import com.mab.user.model.LoginDto;
import com.mab.user.model.TokenDto;
import com.mab.user.model.UserEntity;
import com.mab.user.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

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
	private final RedisConfig redisTemplate;

	public UserController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, RedisConfig redisTemplate) {
		this.tokenProvider = tokenProvider;
		this.authenticationManagerBuilder = authenticationManagerBuilder;
		this.redisTemplate = redisTemplate;
	}

	@PostMapping("/authenticate")
	public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {

		TokenDto tokenDto = new TokenDto();

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				loginDto.getUsername(), loginDto.getPassword());

		try {
			Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);

			String jwt = tokenProvider.createToken(authentication);
			String re_jwt = tokenProvider.creatRefreshToken(authentication);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

			tokenDto.setToken(jwt);
			tokenDto.setRefesh_token("Bearer " + re_jwt);
			tokenDto.setResult("1");
			log.info("login success....♡");

			// Cookie Setting
			UserEntity uvo = service.user_login_info(loginDto.getUsername());
//			Cookie cookie1 = new Cookie("user_no", uvo.getUser_no());
//			cookie1.setPath("/");
//			response.addCookie(cookie1);
//
//			Cookie cookie2 = new Cookie("refresh_token", re_jwt);
//			cookie2.setPath("/");
//			cookie2.setHttpOnly(true);
//			cookie2.setSecure(true);
//			response.addCookie(cookie2);
			
			// cros 환경에서 쿠키 저장하려면 samesite 설정을 none으로 해야 함. 
			ResponseCookie cookie3 = ResponseCookie.from("user_no", uvo.getUser_no()).path("/").sameSite("none").domain("localhost").build();
			response.addHeader("Set-Cookie", cookie3.toString());
			ResponseCookie cookie4 = ResponseCookie.from("refresh_token", re_jwt).path("/").sameSite("none").httpOnly(true).secure(true).domain("localhost").build();
			response.addHeader("Set-Cookie", cookie4.toString());

			// Redis Setting 아래 주석은 세션을 세션 대신에 redis에 저장하는 부분. 우리 프로젝트는 세션을 사용하지 않기 때문에 다르게 작성함.
//			UUID uid = Optional.ofNullable(UUID.class.cast(session.getAttribute("refresh_token"))).orElse(UUID.randomUUID());
//			session.setAttribute("refresh_token", uid);
			 redisTemplate.redisTemplate().opsForValue()
             .set("RT:" + authentication.getName(), re_jwt, tokenProvider.setExpiration(), TimeUnit.MILLISECONDS);
			
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
