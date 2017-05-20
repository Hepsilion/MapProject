package com.example.util;

import java.io.Serializable;

/**
 * 小程序，就以姓名做为主键
 *
 * @author wutingming
 *
 */
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;// 用户名
	private String email;// email
	private String password;// 密码

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
