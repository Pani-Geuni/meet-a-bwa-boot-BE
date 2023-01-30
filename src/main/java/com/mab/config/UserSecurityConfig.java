package com.mab.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CorsFilter;

import com.mab.jwt.JwtAccessDeniedHandler;
import com.mab.jwt.JwtAuthenticationEntryPoint;
import com.mab.jwt.JwtSecurityConfig;
import com.mab.jwt.TokenProvider;
import com.mab.user.service.UserSecurity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableWebSecurity(debug = true)
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class UserSecurityConfig {

	  private final TokenProvider tokenProvider;
	    private final CorsFilter corsFilter;
	    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	    public UserSecurityConfig(
	            TokenProvider tokenProvider,
	            CorsFilter corsFilter,
	            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
	            JwtAccessDeniedHandler jwtAccessDeniedHandler
	    ) {
	        this.tokenProvider = tokenProvider;
	        this.corsFilter = corsFilter;
	        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
	        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
	    }
	    
	@Bean
	public UserDetailsService userUserDetailsService() {
		return new UserSecurity();
	}

	// BCryptPasswordEncoder는 Spring Security에서 제공하는 비밀번호 암호화 객체 (BCrypt라는 해시 함수를 이용하여 패스워드를 암호화 한다.)
	// 회원 비밀번호 등록시 해당 메서드를 이용하여 암호화해야 로그인 처리시 동일한 해시로 비교한다.
	@Bean
	public BCryptPasswordEncoder passwordEncoder1() {
		return new BCryptPasswordEncoder();// - 생성자의 인자 값(verstion, strength, SecureRandom instance)을 통해서 해시의 강도를
		// 조절할 수 있습니다.
	}

	public void configure(WebSecurity web) throws Exception {

		web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider1() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userUserDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder1());

		return authProvider;
	}

	@Bean
	public SecurityFilterChain filterChain1(HttpSecurity http) throws Exception {
		http.authenticationProvider(authenticationProvider1());

		
		http.csrf().disable()
		
		            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
		
		            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
		            .accessDeniedHandler(jwtAccessDeniedHandler)
		
		        // https://gigas-blog.tistory.com/124 
		        .and()
		        .headers()
		        .frameOptions()
		        .sameOrigin() // 안되면 .ALLOW-FROM uri 시도
		
		            // 세션을 사용하지 않기 때문에 STATELESS로 설정
		            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		
		            .and().authorizeRequests().antMatchers("/","/user/login", "/test/", "/api/v2/**", "/v3/api-docs", "/static/**", "/swagger*/**",
							"/api/v1/auth/**", "/h2-console/**", "/favicon.ico", "/swagger-ui.html", "/swagger/**",
							"/swagger-resources/**", "webjars/**", "/v2/api-docs", "/user/insertOK", "/js/**", "/css/**",
							"/images/**", "/error").permitAll()
		
		            .anyRequest().authenticated()
		
		            .and().apply(new JwtSecurityConfig(tokenProvider))
		            
		            .and().logout()
		            .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")) // 로그아웃// URL
		            .logoutSuccessUrl("/user/logoutOK") // 성공시 리턴 URL
		            .deleteCookies("user_no", "user_image") // JSESSIONID 쿠키 삭제
		            .permitAll();
		
		      return http.build();
	}
}
