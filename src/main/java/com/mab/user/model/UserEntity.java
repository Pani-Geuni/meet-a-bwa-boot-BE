
package com.mab.user.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USERINFO")
public class UserEntity implements Serializable {

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
	

}// end class
