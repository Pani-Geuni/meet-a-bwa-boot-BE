
package com.mab.user.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto implements Serializable,UserDetails {

	private String user_no;

	private String user_image;

	private String user_id;

	private String user_pw;

	private String user_name;

	private String user_nickname;

	private String user_email;

	private String user_tel;

	private Date user_birth;
	
	private String user_gender;
	
	private String user_interest;

	private String user_city;

	private String user_county;

	private String user_state;

	private String role;
	

	// 계정이 가지고있는 권한 목록을 리턴
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
		for (String role : role.split(",")) {
			log.info("role:{}", role);
			roles.add(new SimpleGrantedAuthority(role));
		}
		return roles;
	}

	@Override
	public String getPassword() {
		return this.getUser_pw();
	}

	@Override
	public String getUsername() {
		log.info("id::::::::::::::::::{}", this.getUser_id());
		return this.getUser_id();

	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}// end class
