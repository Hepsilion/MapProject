package com.example.mapproject;

import com.example.client.Client;
import com.example.util.Constants;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.StrictMode;

public class MyApplication extends Application {

	private Client client;// 客户端
	private boolean isClientStart;// 客户端连接是否启动

	private NotificationManager mNotificationManager;
	private int newMsgNum = 0;// 后台运行的消息

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("MyApplication onCreate......");

		Intent service = new Intent(this, GetMsgService.class);
		startService(service);
		System.out.println("MyApplication Start GetMessageService....");

		System.out.println(Constants.SERVER_IP + "  " + Constants.SERVER_PORT);

		client = new Client(Constants.SERVER_IP, Constants.SERVER_PORT);
		System.out.println("new Client....");
	}

	public Client getClient() {
		return client;
	}

	public boolean isClientStart() {
		return isClientStart;
	}

	public void setClientStart(boolean isClientStart) {
		this.isClientStart = isClientStart;
	}

	public NotificationManager getmNotificationManager() {
		return mNotificationManager;
	}

	public void setmNotificationManager(NotificationManager mNotificationManager) {
		this.mNotificationManager = mNotificationManager;
	}

	public int getNewMsgNum() {
		return newMsgNum;
	}

	public void setNewMsgNum(int newMsgNum) {
		this.newMsgNum = newMsgNum;
	}

}
