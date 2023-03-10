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

	// BCryptPasswordEncoder??? Spring Security?????? ???????????? ???????????? ????????? ?????? (BCrypt?????? ?????? ????????? ???????????? ??????????????? ????????? ??????.)
	// ?????? ???????????? ????????? ?????? ???????????? ???????????? ??????????????? ????????? ????????? ????????? ????????? ????????????.
	@Bean
	public BCryptPasswordEncoder passwordEncoder1() {
		return new BCryptPasswordEncoder();// - ???????????? ?????? ???(verstion, strength, SecureRandom instance)??? ????????? ????????? ?????????
		// ????????? ??? ????????????.
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
		        .sameOrigin() // ????????? .ALLOW-FROM uri ??????
		
		            // ????????? ???????????? ?????? ????????? STATELESS??? ??????
		            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		
		            .and().authorizeRequests().antMatchers("/","/user/login","/**", "/test/", "/api/v2/**", "/v3/api-docs", "/static/**", "/swagger*/**",
							"/api/v1/auth/**", "/h2-console/**", "/favicon.ico", "/swagger-ui.html", "/swagger/**",
							"/swagger-resources/**", "webjars/**", "/v2/api-docs", "/user/insertOK", "/js/**", "/css/**",
							"/images/**", "/error").permitAll()
		
		            .anyRequest().authenticated()
		
		            .and().apply(new JwtSecurityConfig(tokenProvider))
		            
		            .and().logout()
		            .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")) // ????????????// URL
		            .logoutSuccessUrl("/user/logoutOK") // ????????? ?????? URL
		            .deleteCookies("user_no", "user_image") // JSESSIONID ?????? ??????
		            .permitAll();
		
		      return http.build();
	}
}
