package com.example.dbtest_android;

import java.io.IOException;
import java.io.StreamCorruptedException;

import nde2.pull.types.Area;
import android.content.Context;
import android.database.Cursor;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.dbtest_android.tables.CacheTable;

public class CacheSnooperCursorAdapter extends CursorAdapter {
	private int colid_CT_URL, colid_CT_RET, colid_CT_BLOB;
	private Context sctx;
	private LayoutInflater inflater;

	private static final String DESC_TEMPLATE = "Retrieved %.1f days ago from %s";

	/**
	 * @param context
	 * @param c
	 * @param autoRequery
	 */
	public CacheSnooperCursorAdapter(Context context, Cursor c,
			boolean autoRequery) {
		super(context, c, autoRequery);
		sctx = context;
		setup();
	}

	/**
	 * @param context
	 * @param c
	 * @param flags
	 */
	public CacheSnooperCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		sctx = context;
		setup();
	}

	private void setup() {
		inflater = LayoutInflater.from(sctx);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		colid_CT_BLOB = getCursor().getColumnIndex(
				CacheTable.FIELD_CACHED_OBJECT);
		colid_CT_RET = getCursor()
				.getColumnIndex(CacheTable.FIELD_RETRIEVED_ON);
		colid_CT_URL = getCursor().getColumnIndex(CacheTable.FIELD_URL);
		TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		TextView text2 = (TextView) view.findViewById(android.R.id.text2);

		long retrieved;
		String url;
		Area area;
		retrieved = cursor.getLong(colid_CT_RET);
		url = cursor.getString(colid_CT_URL);

		try {
			area = (Area) Pickle.load(cursor.getBlob(colid_CT_BLOB));
			text1.setText(area.getName());
		} catch (StreamCorruptedException e) {
			text1.setText(new SpannableString(
					"<span style=\"color:red\">Corrupted data</span>"));
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			text1.setText(new SpannableString(
					"<span style=\"color:red\">Undefined type</span> -- not an Area?"));
			e.printStackTrace();
		} catch (IOException e) {
			text1.setText(new SpannableString(
					"<span style=\"color:red\">Read error</span>"));
			e.printStackTrace();
		}
		float retd = ((float) retrieved) / (1000f * 3600f * 24f);
		text2.setText(String.format(DESC_TEMPLATE, retd, url.substring(44)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup root) {
		if (context != sctx) {
			sctx = context;
			inflater = LayoutInflater.from(sctx);
		}
		View target = inflater.inflate(android.R.layout.simple_list_item_2,
				root, false);
		bindView(target, sctx, cursor);
		return target;
	}

}
