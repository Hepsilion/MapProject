package com.example.login;

import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.client.Client;
import com.example.client.ClientOutputThread;
import com.example.mapproject.GetMsgService;
import com.example.mapproject.MyApplication;
import com.example.mapproject.R;
import com.example.sqlite.FriendListActivity;
import com.example.sqlite.UserDB;
import com.example.tran.TranObject;
import com.example.tran.TranObjectType;
import com.example.util.Constants;
import com.example.util.DialogFactory;
import com.example.util.SharePreferenceUtil;
import com.example.util.User;

public class LoginActivity extends MyActivity {
	private Button Login, Register, Back;
	private EditText NameEt, PasswdEt;
	private static final String Personal_Information = "Personal_Information";

	private MyApplication application;
	private Client client;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginpage);

		System.out.println("LoginActivity onCreate....");

		initView();

		application = (MyApplication) getApplication();
		client = application.getClient();

	}

	private void initView() {
		// TODO Auto-generated method stub

		Login = (Button) findViewById(R.id.login_btn);
		Register = (Button) findViewById(R.id.toregister_btn);
		Back = (Button) findViewById(R.id.login_back_btn);
		Login.setOnClickListener(new loginListener());
		Register.setOnClickListener(new registerListener());
		Back.setOnClickListener(new backListener());

		NameEt = (EditText) findViewById(R.id.login_name);
		PasswdEt = (EditText) findViewById(R.id.login_password);

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		System.out.println("LoginActivity onStop....");

		Editor editor = getSharedPreferences(Personal_Information,
				MODE_WORLD_READABLE).edit();
		editor.putString("userName", NameEt.getText().toString());
		editor.putString("passWord", PasswdEt.getText().toString());
		editor.commit();// 将数据保存
		super.onStop();
	}

	private class loginListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			submit();
		}
	}

	private class backListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}

	}

	private class registerListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, RegisterActivity.class);
			startActivity(intent);
		}

	}

	/**
	 * 点击登录按钮后，弹出验证对话框
	 */
	private Dialog mDialog = null;

	private void showRequestDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在验证账号...");
		mDialog.show();
	}

	/**
	 * 提交账号密码信息到服务器
	 */
	private void submit() {
		String name = NameEt.getText().toString();
		String password = PasswdEt.getText().toString();
		if (name.length() == 0 || password.length() == 0) {
			DialogFactory.ToastDialog(this, "登录", "帐号或密码不能为空!");
		} else {
			showRequestDialog();
			// 通过Socket验证信息
			if (application.isClientStart()) {
				ClientOutputThread out = client.getClientOutputThread();
				// 定义一个传输对象，对象类型为注册
				TranObject<User> o = new TranObject<User>(TranObjectType.LOGIN);

				User u = new User();
				u.setName(name);
				u.setPassword(password);

				o.setObject(u);
				out.setMessage(o);

				System.out.println("LoginActivity 提交....");

			} else {
				if (mDialog.isShowing())
					mDialog.dismiss();
				DialogFactory.ToastDialog(LoginActivity.this, "登录", "服务器暂未开放！");
			}
		}
	}

	@Override
	public void getMessage(TranObject msg) {
		// TODO Auto-generated method stub

		System.out.println("LoginActivity getMessage....");

		if (msg != null) {
			switch (msg.getType()) {
				case LOGIN:// LoginActivity只处理登录的消息
					List<User> list = (List<User>) msg.getObject();
					if (list.size() > 0) {
						// 保存用户信息
						SharePreferenceUtil util = new SharePreferenceUtil(
								LoginActivity.this, Constants.SAVE_USER);
						util.setName(NameEt.getText().toString());
						util.setPasswd(PasswdEt.getText().toString());
						util.setEmail(list.get(0).getEmail());
						// 将好友列表加入到本地数据库中
						UserDB db = new UserDB(LoginActivity.this);
						db.addUser(list);

						Intent i = new Intent(LoginActivity.this,
								FriendListActivity.class);
						startActivity(i);
						if (mDialog.isShowing())
							mDialog.dismiss();
						finish();
						Toast.makeText(getApplicationContext(), "登录成功", 0).show();
					} else {
						DialogFactory.ToastDialog(LoginActivity.this, "登录",
								"您的帐号或密码错误！");
						if (mDialog.isShowing())
							mDialog.dismiss();
					}
					break;
				default:
					break;
			}
		}
	}

}
