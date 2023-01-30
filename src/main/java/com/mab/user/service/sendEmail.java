package com.mab.user.service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.mab.user.model.EmailDto;
import com.mab.user.model.UserDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SendEmail {

	@Autowired
	JavaMailSender javaMailSender;

	// **********************
	// 아이디 찾기
	// **********************
	public String findId(UserDto uvo) {
		log.info("아이디 찾기 email : {} :", uvo);

		String result= "";
		
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();
			msg.setSubject("[밋:어봐] 아이디 찾기");
			msg.setText(
			"안녕하세요. "+ "<br><br>"
			+ uvo.getUser_name() + " 회원님의 아이디는 다음과 같습니다." +

			"<br><br>" + "<strong>아이디 : </strong>" + uvo.getUser_id(), "text/html; charset=utf-8");
			msg.setRecipient(RecipientType.TO, new InternetAddress(uvo.getUser_email()));

			javaMailSender.send(msg);
			
			result = "1";
			
		} catch (MessagingException e) {
			result = "0";
		}
		return result;
	}
	
	
	// **********************
	// 비밀번호 찾기
	// **********************
	public UserDto findPw(UserDto uvo) {
		log.info("비밀번호 찾기 email : {} :", uvo);
		
		String temp_pw = RandomStringUtils.randomAlphanumeric(10);
		
		uvo.setUser_pw(new BCryptPasswordEncoder().encode(temp_pw));
		
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();
			msg.setSubject("[밋:어봐] 임시 비밀번호 발급");
			msg.setText("안녕하세요. "+ "<br><br>"+ 
					uvo.getUser_name() + " 회원님의 임시 비밀번호는 다음과 같습니다" + "<br><br>" + "<font color=\"red\">로그인후 재설정을 권장합니다 </font>"
					+ "<br><br>" + "<strong>임시 비밀번호 : </strong>" + temp_pw,
					"text/html; charset=utf-8");
			msg.setRecipient(RecipientType.TO, new InternetAddress(uvo.getUser_email()));
			
			javaMailSender.send(msg);
		} catch (MessagingException e) {
			uvo = null;
		}
		return uvo;
	}

}
