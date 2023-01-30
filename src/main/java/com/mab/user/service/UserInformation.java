package com.mab.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;
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
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mab.config.RedisConfig;
import com.mab.jwt.JwtFilter;
import com.mab.jwt.RedisDao;
import com.mab.jwt.TokenProvider;
import com.mab.user.dao.UserDAO;
import com.mab.user.model.LoginDto;
import com.mab.user.model.TokenDto;
import com.mab.user.model.UserDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserInformation implements UserService {
	
	@Autowired
	UserDAO dao;

	// 자동 개행 및 줄 바꿈 (new Gson은 일자로 나옴)
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private final TokenProvider tokenProvider;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final RedisConfig redisTemplate;
	private final RedisDao redisDao;

	public UserInformation(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, RedisConfig redisTemplate, RedisDao redisDao) {
		this.tokenProvider = tokenProvider;
		this.authenticationManagerBuilder = authenticationManagerBuilder;
		this.redisTemplate = redisTemplate;
		this.redisDao = redisDao;
	}
	
	
	@Override
	public Map<String, Object> login(@Valid LoginDto loginDto, HttpServletResponse response) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
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
			tokenDto.setRefesh_token(re_jwt);
			tokenDto.setResult("1");
			log.info("login success....♡");

			// Cookie Setting
			UserDto uvo = dao.user_login_info(loginDto.getUsername());
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
			ResponseCookie cookie4 = ResponseCookie.from("user_image", "https://meet-a-bwa.s3.ap-northeast-2.amazonaws.com/user/" + uvo.getUser_image()).path("/").sameSite("none").domain("localhost").build();
			response.addHeader("Set-Cookie", cookie4.toString());
			ResponseCookie cookie5 = ResponseCookie.from("refresh_token", re_jwt).path("/").sameSite("none").httpOnly(true).secure(true).domain("localhost").build();
			response.addHeader("Set-Cookie", cookie5.toString());

			// Redis Setting 아래 주석은 세션을 세션 대신에 redis에 저장하는 부분. 우리 프로젝트는 세션을 사용하지 않기 때문에 다르게 작성함.
//			UUID uid = Optional.ofNullable(UUID.class.cast(session.getAttribute("refresh_token"))).orElse(UUID.randomUUID());
//			session.setAttribute("refresh_token", uid);
			 redisTemplate.redisTemplate().opsForValue()
             .set("RT:" + authentication.getName(), re_jwt, tokenProvider.setExpiration(), TimeUnit.MILLISECONDS);
			map.put("jwt", jwt);
		} catch (Exception e) {
			tokenDto.setResult("0");
			log.info("login fail....♡");
		}
		map.put("tokenDto", tokenDto);
		return map;
	}

}// end class