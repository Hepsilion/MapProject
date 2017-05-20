package com.example.tran;

import java.io.Serializable;

/**
 * 传输的对象
 *
 * @author wutingming
 *
 * @param <T>
 */

public class TranObject<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private TranObjectType type;// 发送的消息类型
	private String fromUser;// 来自哪个用户
	private String toUser;// 发往哪个用户
	private T object;// 传输的对象

	public TranObject(TranObjectType type) {
		this.type = type;
	}

	public TranObjectType getType() {
		return type;
	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public String getToUser() {
		return toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}

	@Override
	public String toString() {
		return "TranObject [type=" + type + ", fromUser=" + fromUser
				+ ", toUser=" + toUser + ", object=" + object + "]";
	}
}
