package com.example.sqlite;

import java.util.ArrayList;
import java.util.List;

import com.example.sqlite.DBHelper;
import com.example.util.User;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDB {
	private DBHelper helper;

	public UserDB(Context context) {
		helper = new DBHelper(context);
	}

	public User selectInfo(String name) {
		User u = new User();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = db.rawQuery("select * from Friend where name=?",
				new String[] { name + "" });
		if (c.moveToFirst()) {
			u.setName(c.getString(c.getColumnIndex("name")));
		}
		return u;
	}

	public void addUser(List<User> list) {
		SQLiteDatabase db = helper.getWritableDatabase();
		for (User u : list) {
			db.execSQL("insert into Friend (name) values(?)",
					new Object[] { u.getName() });
		}
		db.close();
	}

	public void updateUser(List<User> list) {
		if (list.size() > 0) {
			delete();
			addUser(list);
		}
	}

	public List<User> getUser() {
		SQLiteDatabase db = helper.getWritableDatabase();
		List<User> list = new ArrayList<User>();
		Cursor c = db.rawQuery("select * from Friend", null);
		while (c.moveToNext()) {
			User u = new User();
			u.setName(c.getString(c.getColumnIndex("name")));
			list.add(u);
		}
		c.close();
		db.close();
		return list;
	}

	public void delete() {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from Friend");
		db.close();
	}
}
