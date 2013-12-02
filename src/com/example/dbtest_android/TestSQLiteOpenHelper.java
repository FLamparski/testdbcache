package com.example.dbtest_android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.dbtest_android.tables.CacheTable;

public class TestSQLiteOpenHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "TestDb";
	public static final int DB_VERSION = 2;

	public TestSQLiteOpenHelper(Context ctx) {
		super(ctx, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		CacheTable.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("database", "The database will be upgraded: " + oldVersion
				+ " -> " + newVersion + ".");
		CacheTable.onUpgrade(db);
		onCreate(db);
	}
}
