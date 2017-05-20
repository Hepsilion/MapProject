package com.example.login;

import com.example.client.Client;
import com.example.client.ClientOutputThread;
import com.example.mapproject.MyApplication;
import com.example.mapproject.R;
import com.example.tran.TranObject;
import com.example.tran.TranObjectType;
import com.example.util.DialogFactory;
import com.example.util.User;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends MyActivity {
	private Button Register;
	private Button Back;
	private EditText EmailEt, NameEt, PasswdEt, PasswdEt2;

	private MyApplication application;
	private Client client;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		System.out.println("RegisterActivity onCreate....");

		initView();

		application = (MyApplication) getApplication();
		client = application.getClient();
	}

	public void initView() {
		Register = (Button) findViewById(R.id.register_btn);
		Back = (Button) findViewById(R.id.reg_back_btn);
		Register.setOnClickListener(new registerListener());
		Back.setOnClickListener(new backListener());

		EmailEt = (EditText) findViewById(R.id.reg_email);
		NameEt = (EditText) findViewById(R.id.reg_name);
		PasswdEt = (EditText) findViewById(R.id.reg_password);
		PasswdEt2 = (EditText) findViewById(R.id.reg_password2);

	}

	private class registerListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			estimate();
		}

	}

	private class backListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			RegisterActivity.this.finish();
		}

	}

	private Dialog mDialog = null;

	private void showRequestDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在注册中...");
		mDialog.show();
	}

	// 提交注册信息
	private void estimate() {
		String email = EmailEt.getText().toString();
		String name = NameEt.getText().toString();
		String passwd = PasswdEt.getText().toString();
		String passwd2 = PasswdEt2.getText().toString();
		if (email.equals("") || name.equals("") || passwd.equals("")
				|| passwd2.equals("")) {
			DialogFactory.ToastDialog(RegisterActivity.this, "注册",
					"请注意：带*项是不能为空！");
		} else {
			if (passwd.equals(passwd2)) {
				showRequestDialog();
				// 如果已连接上服务器
				if (application.isClientStart()) {
					ClientOutputThread out = client.getClientOutputThread();
					// 定义一个传输对象，且传输对象类型为注册
					TranObject<User> object = new TranObject<User>(
							TranObjectType.REGISTER);

					User u = new User();
					u.setName(name);
					u.setPassword(passwd);
					u.setEmail(email);

					object.setObject(u);
					out.setMessage(object);

					System.out.println("RegisterActivity 提交....");

				} else {
					if (mDialog.isShowing())
						mDialog.dismiss();
					DialogFactory.ToastDialog(this, "注册", "对不起，服务器暂未开放！");
				}
			} else {
				DialogFactory.ToastDialog(RegisterActivity.this, "注册",
						"您两次输入的密码不同！");
			}
		}
	}

	@Override
	public void getMessage(TranObject msg) {
		// TODO Auto-generated method stub

		System.out.println("RegisterActivity getMessage...");

		switch (msg.getType()) {
			case REGISTER:
				User u = (User) msg.getObject();
				if (u != null) {
					if (mDialog != null) {
						mDialog.dismiss();
						mDialog = null;
					}
					DialogFactory
							.ToastDialog(RegisterActivity.this, "注册", "请牢记您的登录用户名："
									+ u.getName() + "\n密码：" + u.getPassword());
				} else {
					if (mDialog != null) {
						mDialog.dismiss();
						mDialog = null;
					}
					DialogFactory.ToastDialog(RegisterActivity.this, "注册", "注册失败！");
				}
				break;
			default:
				break;
		}
	}
}
