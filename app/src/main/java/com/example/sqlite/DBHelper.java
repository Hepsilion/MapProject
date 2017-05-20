package com.example.sqlite;

import com.example.util.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * DatabaseHelper作为一个访问SQLite的助手类，提供了两个方面的功能
 *  1、getReadableDatabase和getWriteableDatabase可以获得SQLite对象，
 *  2、提供 onCreate和onUpgrade这两个回调函数，允许我们在创建和更新数据库时，进行我们自己的操作
 */

//这个数据库用来存放好友信息
public class DBHelper extends SQLiteOpenHelper {

	private SQLiteDatabase db;

	// 在SQLiteOpenHelper子类中必须有构造函数
	// 第一个参数
	// 第二个参数为创建数据库的名字
	// 第三个参数
	// 第四个参数为数据库的版本
	public DBHelper(Context c) {
		super(c, Constants.DB_NAME, null, 2);
	}

	@Override
	// 在第一次创建数据库时调用，即第一次得到SQLiteDatabase对象时使用
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		db.execSQL(Constants.CREATE_TBL);
	}

	public void insert(ContentValues values) {
		SQLiteDatabase db = getWritableDatabase();
		db.insert(Constants.TBL_NAME, null, values);
		db.close();
	}

	public Cursor query() {
		SQLiteDatabase db = getWritableDatabase();
		// 返回一个在结果集第一个元素之前位置的游标对象
		Cursor c = db.query(Constants.TBL_NAME, null, null, null, null, null,
				null);
		return c;
	}

	public void del(int id) {
		if (db == null)
			db = getWritableDatabase();
		db.delete(Constants.TBL_NAME, "_id=?",
				new String[] { String.valueOf(id) });
	}

	public void close() {
		if (db != null)
			db.close();
	}

	@Override
	// 更新数据库的版本
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("ALTER TABLE user ADD COLUMN other TEXT");
	}

}
