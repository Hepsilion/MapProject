package com.example.mapproject;

import com.example.client.Client;
import com.example.client.ClientInputThread;
import com.example.client.ClientOutputThread;
import com.example.client.MessageListener;
import com.example.login.LoginActivity;
import com.example.mapproject.R;
import com.example.sqlite.FriendListActivity;
import com.example.tran.TranObject;
import com.example.tran.TranObjectType;
import com.example.util.Constants;
import com.example.util.DialogFactory;
import com.example.util.SharePreferenceUtil;
import com.example.util.TextMessage;
import com.example.util.User;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;

/**
 * 不断获取消息的服务
 *
 * @author wutingming
 *
 */
public class GetMsgService extends Service {
	private static final int MSG = 0x001; // 辨认消息号

	private NotificationManager mNotificationManager;
	private Notification mNotification;

	private MyApplication application;
	private Client client;

	private boolean isStart = false;// 是否与服务器连接上
	private SharePreferenceUtil util;

	private Context mContext = this;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		System.out.println("GetMsgService onCreate.....");

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects() // 探测SQLite数据库操作
				.penaltyLog() // 打印logcat
				.penaltyDeath().build());

		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		application = (MyApplication) getApplication();
		client = application.getClient();

		application.setmNotificationManager(mNotificationManager);
	}

	@Override
	public void onStart(Intent intent, int startId) {

		System.out.println("GetMsgService onStart.....");

		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		util = new SharePreferenceUtil(getApplicationContext(),
				Constants.SAVE_USER);
		// 启动客户端后设置客户端运行状态
		isStart = client.start();
		application.setClientStart(isStart);

		System.out.println("client start:" + isStart + "......");

		// 如果客户端已经在运行，则获得客户端输入流并监听输入流
		if (isStart) {
			ClientInputThread in = client.getClientInputThread();
			in.setMessageListener(new MessageListener() {

				@Override
				public void Message(TranObject object) {
					// TODO Auto-generated method stub
					// 如果 是在后台运行，就更新通知栏，否则就发送广播给Activity
					if (util.getIsStart()) {
						// 只处理文本消息类型
						if (object.getType() == TranObjectType.MESSAGE) {
							System.out.println("收到新消息");
							// 把消息对象发送到handler去处理
							Message message = handler.obtainMessage();
							message.what = MSG;
							message.getData().putSerializable("msg", object);
							handler.sendMessage(message);
						}
					} else {
						Intent broadCast = new Intent();
						broadCast.setAction(Constants.ACTION);
						broadCast.putExtra(Constants.MSGKEY, object);
						// 把收到的消息已广播的形式发送出去
						sendBroadcast(broadCast);
					}
				}
			});
		}
	}

	// 用来更新通知栏消息的handler
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG:
					int newMsgNum = application.getNewMsgNum();// 从全局变量中获取消息条数
					newMsgNum++;// 每收到一次消息，自增一次
					application.setNewMsgNum(newMsgNum);// 再设置为全局变量消息条数
					TranObject<TextMessage> textObject = (TranObject<TextMessage>) msg
							.getData().getSerializable("msg");
					System.out.println(textObject);
					if (textObject != null) {
						// 消息从哪里来
						String form = textObject.getFromUser();
						// 消息内容
						String content = textObject.getObject().getMessage();

						// 更新通知栏
						int icon = R.drawable.notify_newmessage;
						CharSequence tickerText = form + ":" + content;
						long when = System.currentTimeMillis();
						mNotification = new Notification(icon, tickerText, when);

						mNotification.flags = Notification.FLAG_NO_CLEAR;
						// 设置默认声音
						mNotification.defaults |= Notification.DEFAULT_SOUND;
						// 设定震动(需加VIBRATE权限)
						mNotification.defaults |= Notification.DEFAULT_VIBRATE;
						mNotification.contentView = null;

						Intent intent = new Intent(mContext,
								FriendListActivity.class);
						PendingIntent contentIntent = PendingIntent.getActivity(
								mContext, 0, intent, 0);
						mNotification.setLatestEventInfo(mContext, util.getName()
										+ " (" + newMsgNum + "条新消息)", content,
								contentIntent);
					}
					mNotificationManager.notify(Constants.NOTIFY_ID, mNotification);// 发出通知
					break;
				default:
					break;
			}
		}
	};

	// 在服务被摧毁时，做一些事情
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

		System.out.println("GetMsgService onDestroy.....");

		super.onDestroy();
		mNotificationManager.cancel(Constants.NOTIFY_ID);
		// 如果客户端仍在运行，则关闭客户端，并给服务器发送下线消息
		if (isStart) {
			ClientOutputThread out = client.getClientOutputThread();
			TranObject<User> o = new TranObject<User>(TranObjectType.LOGOUT);
			User u = new User();
			u.setName(util.getName());
			o.setObject(u);
			out.setMessage(o);
			// 发送完之后，关闭client,即关闭消息输入输出流
			out.setStart(false);
			client.getClientInputThread().setStart(false);
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
