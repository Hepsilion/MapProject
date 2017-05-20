package com.example.util;

/*
 * 一些常量
 */

public class Constants {
	public static final String SERVER_IP = "192.168.1.100";// 服务器ip
	// //这里要填写服务器ip地址
	public static final int SERVER_PORT = 6013;// 服务器端口 //这里要填写服务器端口号

	public static final String ACTION = "com.example.message";// 消息广播action
	public static final String MSGKEY = "message";// 消息的key
	// 保存用户信息的xml文件名
	public static final String SAVE_USER = "saveUser";
	// 数据库名
	public static final String DB_NAME = "friend.db";
	// 表名
	public static final String TBL_NAME = "Friend";
	// 创建表的语句
	public static final String CREATE_TBL = " create table "
			+ " Friend(_id integer primary key autoincrement,userNum text,name text) ";

	public static final int NOTIFY_ID = 0x911;// 通知ID

	public static final String Personal_Information = "Personal_Information";
}
