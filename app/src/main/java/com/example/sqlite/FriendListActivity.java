package com.example.sqlite;

import com.example.client.Client;
import com.example.client.ClientOutputThread;
import com.example.mapproject.MainActivity;
import com.example.mapproject.MyApplication;
import com.example.mapproject.R;
import com.example.mapproject.TrackService;
import com.example.tran.TranObject;
import com.example.tran.TranObjectType;
import com.example.util.Constants;
import com.example.util.DialogFactory;
import com.example.util.SharePreferenceUtil;
import com.example.util.TextMessage;
import com.example.util.User;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

//显示所有好友，当点击某个好友时，请求位置共享，将来加入一个删除好友的功能，但暂时不实现
public class FriendListActivity extends ListActivity {
	private static int temp;
	private DBHelper helpter;

	private MyApplication application;
	Client client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("好友列表");

		System.out.println("FriendListActivity onCreate....");

		application = (MyApplication) getApplication();
		client = application.getClient();

		helpter = new DBHelper(this);
		Cursor c = helpter.query();

		String[] from = { "_id", "name" };
		int[] to = { R.id.userNum, R.id.name, };

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.friendlist, c, from, to);
		ListView listView = getListView();
		listView.setAdapter(adapter);

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);// 得到一个对话框构造器，用它来产生对话框

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
									long arg3) {
				temp = (int) arg3;
				builder.setMessage("要请求与好友位置共享吗？")
						.setPositiveButton("是",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int which) {
										// 请求好友位置共享
										request();
									}
								})
						.setNegativeButton("否",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int which) {
									}
								})
				// .setNeutralButton("删除好友！", new
				// DialogInterface.OnClickListener() {
				// public void onClick(DialogInterface dialog,int which) {
				// helpter.del((int)temp);
				// Cursor c = helpter.query();
				//
				// String[] from = {"_id","name"};
				// int[] to = {R.id.userNum,R.id.name,};
				//
				// SimpleCursorAdapter adapter = new
				// SimpleCursorAdapter(FriendListActivity.this,R.layout.friendlist,
				// c, from, to);
				// ListView listView = getListView();
				// listView.setAdapter(adapter);
				// }
				// })
				;
				AlertDialog ad = builder.create();
				ad.show();
			}
		});
		helpter.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(Menu.NONE, 0, 0, "添加好友");
		menu.add(Menu.NONE, 1, 1, "停止位置共享");
		menu.add(Menu.NONE, 2, 2, "退出");

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch (item.getItemId()) {
			case 0:// 添加好友
				intent.setClass(FriendListActivity.this,
						com.example.sqlite.AddFriendActivity.class);
				startActivity(intent);
				break;
			case 1:// 停止位置共享
				stopService();
				break;
			case 2:// 退出
				finish();
				break;

		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void startService(String fromUser, String toUser) {

		System.out.println("FriendListActivity startService ....");

		Intent intent = new Intent();
		intent.putExtra("fromUser", fromUser);
		intent.putExtra("toUser", toUser);

		intent.setClass(FriendListActivity.this, TrackService.class);
		startService(intent);
	}

	private void stopService() {
		System.out.println("FriendListActivity stopService ....");

		MainActivity.location_share=false;
		Intent intent = new Intent();
		intent.setClass(FriendListActivity.this, TrackService.class);
		stopService(intent);
	}

	/**
	 * 点击确定按钮后，弹出对话框
	 */
	private Dialog mDialog = null;

	private void showRequestDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在发送请求...");
		mDialog.show();
	}

	private void request() {
		showRequestDialog();
		// 通过Socket验证信息
		if (application.isClientStart()) {
			ClientOutputThread out = client.getClientOutputThread();
			// 定义一个传输对象，对象类型为注册
			TranObject<User> o = new TranObject<User>(
					TranObjectType.LOCATION_SHARE);

			// 找到自己的名字,设置fromUser为自己的名字
			// 第一个参数文件名称，第二个参数是操作模式
			SharedPreferences sharedPreferences = getSharedPreferences(
					Constants.Personal_Information, MODE_WORLD_READABLE);
			String fromUser = sharedPreferences.getString("userName", "");
			o.setFromUser(fromUser);

			// 要获得好友姓名
			SQLiteDatabase db = helpter.getReadableDatabase();
			String toUser;
			Cursor cursor = db.query("Friend", new String[] { "_id", "name" },
					"_id=?", new String[] { Integer.toString(temp) }, null,
					null, null);
			while(cursor.moveToNext())
			{
				toUser = cursor.getString(cursor.getColumnIndex("name"));
				System.out.println("name-------->"+toUser);
				o.setToUser(toUser);
				break;
			}

			out.setMessage(o);
		} else {
			if (mDialog.isShowing())
				mDialog.dismiss();
			DialogFactory.ToastDialog(FriendListActivity.this, "位置共享",
					"对不起，服务器暂未开放！");
		}
	}

	// 获得消息
	public void getMessage(TranObject msg) {
		// TODO Auto-generated method stub
		final String fromUser = msg.getFromUser();
		final String toUser = msg.getToUser();

		System.out.println("FriendListActivity getMessage ....");

		if (msg != null) {
			switch (msg.getType()) {
				//收到消息
				case MESSAGE:
					TextMessage message=(TextMessage)msg.getObject();
					new AlertDialog.Builder(FriendListActivity.this)
							.setTitle("温馨提示")
							.setMessage(message.getMessage())
							.setPositiveButton("确定", new DialogInterface.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub

								}

							}).create().show();
					if (mDialog.isShowing())
						mDialog.dismiss();
					break;
				// 好友同意位置共享请求
				case ANSWER_YES_LOCATION_SHARE:
					DialogFactory.creatRequestDialog(FriendListActivity.this,
							"对方已经同意您的请求！");
					// 好友同意请求后开启位置共享服务
					startService(msg.getToUser(), msg.getFromUser());

					Intent intent=new Intent();
					intent.setClass(FriendListActivity.this, MainActivity.class);
					startActivity(intent);

					finish();
					if (mDialog.isShowing())
						mDialog.dismiss();
					break;
				case ANSWER_NO_LOCATION_SHARE:
					DialogFactory.creatRequestDialog(FriendListActivity.this,
							"对方未同意您的位置共享请求！");
					if (mDialog.isShowing())
						mDialog.dismiss();
					break;
				// 好友请求
				case FRIEND_REQUEST:
					new AlertDialog.Builder(FriendListActivity.this)
							.setTitle("温馨提示")
							.setMessage(msg.getFromUser() + "请求成为您的好友！")
							.setPositiveButton("同意",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
															int which) {
											// 同意请求后将好友姓名插入到自己的数据库中
											ContentValues values = new ContentValues();
											values.put("name", fromUser);

											DBHelper helper = new DBHelper(
													FriendListActivity.this);
											helper.insert(values);

											// 通过Socket验证信息
											if (application.isClientStart()) {
												ClientOutputThread out = client
														.getClientOutputThread();
												// 定义一个传输对象，对象类型为注册
												TranObject<User> o = new TranObject<User>(
														TranObjectType.ANSWER_YES_FRIEND_REQUEST);
												o.setFromUser(toUser);
												o.setToUser(fromUser);
												out.setMessage(o);
											}
										}
									})
							.setNegativeButton("不同意",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
															int which) {
											// TODO Auto-generated method stub
											// 通过Socket验证信息
											if (application.isClientStart()) {
												ClientOutputThread out = client
														.getClientOutputThread();
												// 定义一个传输对象，对象类型为注册
												TranObject<User> o = new TranObject<User>(
														TranObjectType.ANSWER_NO_FRIEND_REQUEST);
												o.setFromUser(toUser);
												o.setToUser(fromUser);
												out.setMessage(o);
											}
										}

									}).create().show();

					break;
				case LOCATION_SHARE:
					new AlertDialog.Builder(FriendListActivity.this)
							.setTitle("温馨提示")
							.setMessage(msg.getFromUser() + "请求与您位置共享！")
							.setPositiveButton("同意",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
															int which) {
											// 通过Socket验证信息
											if (application.isClientStart()) {
												ClientOutputThread out = client
														.getClientOutputThread();
												// 定义一个传输对象，对象类型为注册
												TranObject<User> o = new TranObject<User>(
														TranObjectType.ANSWER_YES_LOCATION_SHARE);
												o.setFromUser(toUser);
												o.setToUser(fromUser);
												out.setMessage(o);
											}
											//启动位置共享服务
											startService(toUser, fromUser);

											//结束当前Activity
											Intent intent=new Intent();
											intent.setClass(FriendListActivity.this, MainActivity.class);
											startActivity(intent);
											finish();
										}
									})
							.setNegativeButton("不同意",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
															int which) {
											// TODO Auto-generated method stub
											// 通过Socket验证信息
											if (application.isClientStart()) {
												ClientOutputThread out = client
														.getClientOutputThread();
												// 定义一个传输对象，对象类型为注册
												TranObject<User> o = new TranObject<User>(
														TranObjectType.ANSWER_NO_LOCATION_SHARE);
												o.setFromUser(toUser);
												o.setToUser(fromUser);
												out.setMessage(o);
											}
										}

									}).create().show();
					break;
				default:
					break;
			}
		}
	}

	/**
	 * 广播接收者，接收GetMsgService发送过来的消息
	 */
	private BroadcastReceiver MsgReceiver2 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			TranObject msg = (TranObject) intent
					.getSerializableExtra(Constants.MSGKEY);
			if (msg != null) {
				// 如果不是空，说明是消息广播
				getMessage(msg);// 把收到的消息传递给子类
			} else {// 如果是空消息，说明是关闭应用的广播
				close();
			}
		}
	};

	public void close() {
		Intent i = new Intent();
		i.setAction(Constants.ACTION);
		sendBroadcast(i);
		finish();
	}

	@Override
	public void onStart() {
		super.onStart();

		System.out.println("FriendListActivity onStart ....");

		// 在start方法中注册广播接收者
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION);
		registerReceiver(MsgReceiver2, intentFilter);// 注册接受消息广播

	}

	@Override
	protected void onStop() {
		super.onStop();

		System.out.println("FriendListActivity onStop ....");

		// 在stop方法中注销广播接收者
		unregisterReceiver(MsgReceiver2);// 注销接受消息广播
	}

	private class backListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}

	}
}
