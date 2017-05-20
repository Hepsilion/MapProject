package com.example.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import com.example.tran.TranObject;

import android.content.ContentValues;

public class ClientInputThread extends Thread {
	private Socket socket;
	private boolean isStart = true;
	private ObjectInputStream ois;
	private TranObject message;
	private MessageListener messageListener;// 消息监听接口对象

	public ClientInputThread(Socket socket) {
		this.socket = socket;
		try {

			System.out.println("ClientInputThread 实例化输入流.....");

			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	/**
	 * 提供给外部的消息监听方法
	 *
	 * @param messageListener
	 *            消息监听接口对象
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	@Override
	public void run() {
		try {

			System.out.println("ClientInputThread run.....");

			while (isStart) {
				message = (TranObject) ois.readObject();

				System.out.println("ClientOutputThread run readObject.....");
				System.out.println(message);
				// 每收到一条消息，就调用接口的方法，并传入该消息对象，外部在实现接口的方法时，就可以及时处理传入的消息对象了
				messageListener.Message(message);
			}
			ois.close();
			if (socket != null)
				socket.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
