package com.example.others;

import com.example.mapproject.R;
import com.example.mapproject.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class helpActivity extends Activity {
	private Button back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		back=(Button)findViewById(R.id.help_back_btn);
		back.setOnClickListener(new backListener());

		System.out.println("helpActivity onCreate....");
	}
	private class backListener implements OnClickListener
	{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}

	}

}
