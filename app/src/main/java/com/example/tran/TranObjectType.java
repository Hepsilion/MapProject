package com.example.tran;

/**
 * 传输对象类型
 *
 * @author wutingming
 *
 */
public enum TranObjectType {
	REGISTER, // 注册
	LOGIN, // 用户登录
	LOGOUT, // 用户登出
	FRIEND_REQUEST, // 好友请求
	ANSWER_YES_FRIEND_REQUEST, // 同意添加好友请求
	ANSWER_NO_FRIEND_REQUEST, // 不同意添加好友请求
	LOCATION_SHARE, // 位置共享
	ANSWER_YES_LOCATION_SHARE, // 同意位置共享
	ANSWER_NO_LOCATION_SHARE, // 不同意位置共享
	FRIENDLOGIN, // 好友上线
	FRIENDLOGOUT, // 好友下线
	LOCATION, // 位置信息
	MESSAGE, // 用户发送消息
	UNCONNECTED, // 无法连接
	FILE, // 传输文件
	REFRESH, // 刷新
}
