package com.example.client;

import com.example.tran.TranObject;
import android.content.ContentValues;

/*
 * 消息监听器接口
 */

public interface MessageListener {
	public void Message(TranObject location);
}
