package com.mab.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
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
	
	@Autowired
	SendEmail email;

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
	
	/**
	 * 로그인
	 * 
	 */
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


	/**
	 * 로그아웃
	 * 
	 */
	@Override
	public String logoutOK(HttpServletRequest request) {
		
		String result = "1";
		 /*1. Access Token 검증*/
	    if (!tokenProvider.validateToken(request.getHeader("Authorization").substring(7))) {
	    	result="0";
	    }

	    /*2. Access Token에서 User id을 가져옴*/
	    Authentication authentication = tokenProvider.getAuthentication(request.getHeader("Authorization").substring(7));

	    /*3. Redis에서 해당 User id로 저장된 refresh token이 있는지 여부를 확인 후, 있을 경우 삭제*/
	    if (redisTemplate.redisTemplate().opsForValue().get("RT:" + authentication.getName()) != null) {
	        //refresh token 삭제
	    	redisDao.deleteValues("RT:" + authentication.getName());
	    }

	    /*4. 해당 access token 유효시간 가지고 와서 BlackList로 저장*/
	    Long expiration = tokenProvider.getExpiration(request.getHeader("Authorization").substring(7));
	    redisTemplate.redisTemplate().opsForValue()
	            .set(request.getHeader("Authorization").substring(7), "logout", expiration, TimeUnit.MILLISECONDS);
	    
	    return result;
	}

	/**
	 * 아이디 찾기
	 * 
	 */
	@Override
	public String findId(String user_email) {

		String result = "0";
		UserDto uvo = dao.user_id_select(user_email);
		
		if (uvo != null) {
			result = email.findId(uvo); // 유저의 메일로 아이디 전송
		}
		
		return result;
	}

}// end class
