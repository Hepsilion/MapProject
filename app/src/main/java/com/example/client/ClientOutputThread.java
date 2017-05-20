package com.example.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.example.tran.TranObject;

import android.content.ContentValues;

public class ClientOutputThread extends Thread {
	private Socket socket;
	private ObjectOutputStream oos;
	private boolean isStart = true;
	private TranObject message;

	public ClientOutputThread(Socket socket) {
		this.socket = socket;
		try {

			System.out.println("ClientOutputThread 实例化输出流.....");

			oos = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	public void setMessage(TranObject message) {
		this.message = message;
		synchronized (this) {
			notify();
		}
	}

	@Override
	public void run() {
		try {

			System.out.println("ClientOutputThread run.....");

			while (isStart) {
				if (message != null) {
					oos.writeObject(message);

					System.out
							.println("ClientOutputThread run writeObject.....");

					oos.flush();
					synchronized (this) {
						wait();// 发送完消息后，线程进入等待状态
					}
				}
			}
			oos.close();// 循环结束后，关闭输出流和socket
			if (socket != null)
				socket.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
