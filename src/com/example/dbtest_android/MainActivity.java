package com.example.dbtest_android;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nde2.errors.NDE2Exception;
import nde2.pull.methodcalls.discovery.FindAreas;
import nde2.pull.types.Area;

import org.xmlpull.v1.XmlPullParserException;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.example.dbtest_android.tables.CacheTable;

public class MainActivity extends ListActivity implements
		LoaderCallbacks<Cursor> {

	CacheSnooperCursorAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mAdapter = new CacheSnooperCursorAdapter(getApplicationContext(), null,
				true);
		setListAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this);
		EditText e = (EditText) findViewById(R.id.editText1);
		e.setOnEditorActionListener(sFindPostcode);
	}

	private OnEditorActionListener sFindPostcode = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			fetchArea(v.getText().toString());
			return true;
		}
	};

	private void fetchArea(String postcode) {
		FindAreas f = new FindAreas();
		f.forPostcode(postcode).ofLevelType(13).inHierarchy(26);
		String call = f.toURLString();
		System.out.printf("Preparing to call %s\n", call);
		Cursor cur = getContentResolver().query(CacheProvider.CACHE_URI,
				CacheTable.PROJECTION_ALL, CacheTable.FIELD_URL + " = ?",
				new String[] { call }, CacheTable.FIELD_RETRIEVED_ON + " DESC");
		if (cur.getCount() == 0) {
			Toast.makeText(this, "Fetching from ONS, hold on...",
					Toast.LENGTH_SHORT).show();
			sFetchAreaFromONS.execute(f);
		} else {
			Toast.makeText(this, "This should be in the DB.",
					Toast.LENGTH_SHORT).show();
		}
	}

	private AsyncTask<FindAreas, Void, Map<FindAreas, Area>> sFetchAreaFromONS = new AsyncTask<FindAreas, Void, Map<FindAreas, Area>>() {

		@Override
		protected Map<FindAreas, Area> doInBackground(FindAreas... params) {
			Map<FindAreas, Area> m = new HashMap<FindAreas, Area>();
			Set<Area> sa = null;
			try {
				sa = params[0].execute();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (NDE2Exception e) {
				e.printStackTrace();
			}
			Area a = sa.iterator().next();
			m.put(params[0], a);
			return m;
		}

		protected void onPostExecute(java.util.Map<FindAreas, Area> result) {
			fetchAreaFromONS_finish(result.keySet().iterator().next(), result
					.values().iterator().next());
			getLoaderManager().restartLoader(1, null, MainActivity.this);
		};

	};

	private void fetchAreaFromONS_finish(FindAreas f, Area a) {
		try {
			byte[] ba = Pickle.dump(a);
			String fu = f.toURLString();
			long dr = System.currentTimeMillis();
			ContentValues insert_statement = new ContentValues();
			insert_statement.put(CacheTable.FIELD_CACHED_OBJECT, ba);
			insert_statement.put(CacheTable.FIELD_RETRIEVED_ON, dr);
			insert_statement.put(CacheTable.FIELD_URL, fu);
			getContentResolver().insert(CacheProvider.CACHE_URI,
					insert_statement);
		} catch (Exception e) {
			Log.w("MainActivity", e);
			Toast.makeText(this, "Cannot fetch areas :(", Toast.LENGTH_SHORT)
					.show();
			return;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri baseUri = CacheProvider.CACHE_URI;
		String where = "(" + CacheTable.FIELD_URL + " LIKE '%FindAreas%') OR ("
				+ CacheTable.FIELD_URL + " LIKE '%SearchSByAByPostcode%')";
		String orderBy = CacheTable.FIELD_RETRIEVED_ON + " ASC";
		return new CursorLoader(this, baseUri, CacheTable.PROJECTION_ALL,
				where, null, orderBy);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}

}
