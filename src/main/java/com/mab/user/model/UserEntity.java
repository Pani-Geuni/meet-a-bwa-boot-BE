
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
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USERINFO")
public class UserEntity implements Serializable,UserDetails {

	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user")
	@SequenceGenerator(sequenceName = "seq_user", allocationSize = 1, name = "seq_user")
	@Column(name = "user_no", insertable = false, updatable = false)
	private String user_no;

	@Column(name = "user_image")
	private String user_image;

	@Column(name = "user_id")
	private String user_id;

	@Column(name = "user_pw")
	private String user_pw;

	@Column(name = "user_name")
	private String user_name;

	@Column(name = "user_nickname")
	private String user_nickname;

	@Column(name = "user_email")
	private String user_email;

	@Column(name = "user_tel")
	private String user_tel;

//	@DateTimeFormat(pattern ="yyyyMMdd")
	@Column(name = "user_birth")
	private Date user_birth;
	
	@Column(name = "user_gender")
	private String user_gender;
	
	@Column(name = "user_interest")
	private String user_interest;

	@Column(name = "user_city")
	private String user_city;

	@Column(name = "user_county")
	private String user_county;

	@Column(name = "user_state")
	private String user_state;

	@Column(name = "role")
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
		// TODO Auto-generated method stub
		return this.getUser_pw();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		log.info("id::::::::::::::::::{}", this.getUser_id());
		return this.getUser_id();

	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

}// end class
