package com.mab.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mab.user.model.TokenDto;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	RedisTemplate redisTemplate;
	
	
	public static final String AUTHORIZATION_HEADER = "Authorization";
	private TokenProvider tokenProvider;

	public JwtFilter(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// 헤더에서 JWT 를 받아옴.
		String accessToken = resolveAccessToken(request);
		String refreshToken = resolveRefreshToken(request);

		// 유효한 토큰인지 확인.
		if (accessToken != null) {
			// 어세스 토큰이 유효한 경우
			if (tokenProvider.validateToken(accessToken)) {
				String isLogout = (String) redisTemplate.opsForValue().get(accessToken);
				// 어세스 토큰이 Redis에 존재하는지(로그아웃 상태)
				if (ObjectUtils.isEmpty(isLogout)) {
					this.setAuthentication(accessToken);
				}else
					log.info("로그아웃 됨.");
			}
			// 어세스 토큰이 만료된 상황 | 리프레시 토큰 또한 존재하는 경우
			else if (!tokenProvider.validateToken(accessToken) && refreshToken != null) {
				// 재발급 후, 컨텍스트에 다시 넣기
				/// 리프레시 토큰 검증
				boolean validateRefreshToken = tokenProvider.validateToken(refreshToken);
				/// 리프레시 토큰 저장소 존재유무 확인
				boolean isRefreshToken = tokenProvider.existsRefreshToken(refreshToken);
				if (validateRefreshToken && isRefreshToken) {
					/// 리프레시 토큰으로 사용자 정보 가져오기
					Authentication authentication = tokenProvider.getAuthentication(refreshToken);
					SecurityContextHolder.getContext().setAuthentication(authentication);
					/// 토큰 발급
					String newAccessToken = tokenProvider.createToken(authentication);
					/// 헤더에 어세스 토큰 추가
					tokenProvider.setHeaderAccessToken(response, newAccessToken);
					/// 컨텍스트에 넣기
					this.setAuthentication(newAccessToken);
				} else
					log.info("만료된 Refresh Token 이거나 Redis에 Refresh Token 없음.");
			} else
				log.info("Header에 Refresh Token 없음.");
		} else
			log.info("유효한 Access Token 없음.");
		filterChain.doFilter(request, response);
	}

// SecurityContext 에 Authentication 객체를 저장.
	public void setAuthentication(String token) {
		// 토큰으로부터 유저 정보를 받아옴.
		Authentication authentication = tokenProvider.getAuthentication(token);
		// SecurityContext 에 Authentication 객체를 저장.
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	// Request의 Header에서 AccessToken 값을 가져옴. "authorization" : "token'
	public String resolveAccessToken(HttpServletRequest request) {
		if (request.getHeader(AUTHORIZATION_HEADER) != null)
			return request.getHeader(AUTHORIZATION_HEADER).substring(7);
		return null;
	}

	// Request의 Header에서 RefreshToken 값을 가져옴. "authorization" : "token'
	public String resolveRefreshToken(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (int i = 0; i < request.getCookies().length; i++) {
				if (request.getCookies()[i].getName().equals("refresh_token"))
					return request.getCookies()[i].getValue();
//				return request.getCookies()[i].getName(); 위에 안되면 이 코드로 해보기
			}
//        return request.getHeader("refresh_token"); // 그 위에도 안되면 이 코드로 하기
		}
		return null;
	}
}