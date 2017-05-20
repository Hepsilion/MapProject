package com.example.sqlite;

import java.util.List;
import com.example.client.Client;
import com.example.client.ClientOutputThread;
import com.example.login.LoginActivity;
import com.example.login.MyActivity;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//请求添加好友，好友同意后将好友信息插入到数据库中
public class AddFriendActivity extends MyActivity {
	private EditText friendName;
	private Button serach, Back;

	private MyApplication application;
	private Client client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addfriend);
		this.setTitle("添加好友");

		System.out.println("AddFriendActivity onCreate....");

		application = (MyApplication) getApplication();
		client = application.getClient();

		friendName = (EditText) findViewById(R.id.friendName);
		serach = (Button) findViewById(R.id.serach);
		serach.setOnClickListener(new serachListener());
		Back = (Button) findViewById(R.id.search_back);
		Back.setOnClickListener(new backListener());
	}

	private class serachListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			search();
		}

	}

	/**
	 * 点击查找按钮后，弹出对话框
	 */
	private Dialog mDialog = null;

	private void showRequestDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在发动请求...");
		mDialog.show();
	}

	private void search() {
		String name = friendName.getText().toString();
		if (name.length() == 0) {
			DialogFactory.ToastDialog(AddFriendActivity.this, "添加好友",
					"好友姓名不能为空!");
		} else {
			showRequestDialog();
			// 通过Socket验证信息
			if (application.isClientStart()) {
				ClientOutputThread out = client.getClientOutputThread();
				// 定义一个传输对象，对象类型为注册
				TranObject<User> o = new TranObject<User>(
						TranObjectType.FRIEND_REQUEST);
				// 添加对象

				// 找到自己的名字,设置fromUser为自己的名字
				SharedPreferences sharedPreferences = getSharedPreferences(
						Constants.Personal_Information, MODE_WORLD_READABLE);
				String fromUser = sharedPreferences.getString("userName", "");
				o.setFromUser(fromUser);
				o.setToUser(name);

				out.setMessage(o);

				System.out.println("AddFriendActivity 发送请求....");
			} else {
				if (mDialog.isShowing())
					mDialog.dismiss();
				DialogFactory.ToastDialog(AddFriendActivity.this, "添加好友",
						"对不起，服务器暂未开放！");
			}
		}

	}

	@Override
	public void getMessage(TranObject msg) {
		// TODO Auto-generated method stub
		final String fromUser = msg.getFromUser();
		final String toUser = msg.getToUser();
		System.out.println("AddFriendActivity getMessage....");

		if (msg != null) {
			switch (msg.getType()) {
				case MESSAGE:
					TextMessage message=(TextMessage)msg.getObject();
					new AlertDialog.Builder(AddFriendActivity.this)
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
				// 好友同意请求
				case ANSWER_YES_FRIEND_REQUEST:
					DialogFactory.creatRequestDialog(AddFriendActivity.this,
							"好友已经同意您的请求！");
					// 好友同意请求后将好友姓名插入到自己的数据库中
					ContentValues values = new ContentValues();
					values.put("name", fromUser);

					DBHelper helper = new DBHelper(this);
					helper.insert(values);

					// 好友同意请求后进入好友列表
					Intent intent = new Intent(AddFriendActivity.this,
							FriendListActivity.class);
					startActivity(intent);
					if (mDialog.isShowing())
						mDialog.dismiss();
					finish();
					break;
				case ANSWER_NO_FRIEND_REQUEST:
					DialogFactory.creatRequestDialog(AddFriendActivity.this,
							"对方未同意您的添加好友请求！");
					if (mDialog.isShowing())
						mDialog.dismiss();
					break;
				// 好友请求
				case FRIEND_REQUEST:
					new AlertDialog.Builder(AddFriendActivity.this)
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
													AddFriendActivity.this);
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

											// 好友同意请求后进入好友列表
											Intent intent = new Intent(
													AddFriendActivity.this,
													FriendListActivity.class);
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
														TranObjectType.ANSWER_NO_FRIEND_REQUEST);
												o.setFromUser(toUser);
												o.setToUser(fromUser);
												out.setMessage(o);
											}
										}

									}).create().show();

					break;
				case LOCATION_SHARE:
					new AlertDialog.Builder(AddFriendActivity.this)
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
											//同意好友的位置共享请求后，启动位置共享服务
											startService(toUser, fromUser);

											//进入主界面，并关闭当前界面
											Intent intent=new Intent();
											intent.setClass(AddFriendActivity.this, MainActivity.class);
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

	private void startService(String fromUser, String toUser) {

		System.out.println("FriendListActivity startService ....");

		Intent intent = new Intent();
		intent.putExtra("fromUser", fromUser);
		intent.putExtra("toUser", toUser);

		intent.setClass(AddFriendActivity.this, TrackService.class);
		startService(intent);
	}

	private class backListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}

	}
}
