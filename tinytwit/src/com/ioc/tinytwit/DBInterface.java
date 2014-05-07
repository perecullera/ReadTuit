package com.ioc.tinytwit;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBInterface {

	public static final String CLAU_ID = "_id";
	public static final String CLAU_USER = "user";
	public static final String CLAU_USER_COMPLET = "user_complet";
	public static final String CLAU_TWEET = "tweet";
	public static final String CLAU_DATA = "data";
	public static final String CLAU_RETWEETS = "retweets";
	public static final String CLAU_FAVORITS = "fav";
	public static final String CLAU_URL = "url";

	public static final String TAG = "DBInterface";

	public static final String BD_NOM = "BDClients";
	public static final String BD_TAULA = "feed";
	public static final int VERSIO = 1;

	public static final String BD_CREATE = "create table " + BD_TAULA + "( "
			+ CLAU_ID + " integer primary key autoincrement, " + CLAU_USER
			+ " text not null, " + CLAU_USER_COMPLET + " text not null, " + CLAU_TWEET
			+ " text not null," + CLAU_DATA + " datetime not null,"
			+ CLAU_RETWEETS + " integer not null," + CLAU_FAVORITS
			+ " integer not null, "+ CLAU_URL
			+ " text" + ");";

	private final Context context;
	private AjudaBD ajuda;
	private SQLiteDatabase bd;

	public DBInterface(Context con) {
		this.context = con;
		ajuda = new AjudaBD(context);
	}

	// Obre la BD
	public DBInterface obre() throws SQLException {
		bd = ajuda.getWritableDatabase();
		return this;
	}

	// Tanca la BD
	public void tanca() {
		ajuda.close();
	}

	// Insereix un tweet
	public long insereixTweet(String user, String userComplet, String tweet, Date data, int rt,
			int fav, String url) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(CLAU_USER, user);
		initialValues.put(CLAU_USER_COMPLET, userComplet);
		initialValues.put(CLAU_TWEET, tweet);
		initialValues.put(CLAU_DATA, data.toString());
		initialValues.put(CLAU_RETWEETS, rt);
		initialValues.put(CLAU_FAVORITS, fav);
		initialValues.put(CLAU_URL, url);

		return bd.insert(BD_TAULA, null, initialValues);

	}

	// Esborra un tweet
	public boolean esborraTweet(long IDFila) {
		return bd.delete(BD_TAULA, CLAU_ID + " = " + IDFila, null) > 0;
	}

	// Retorna un tweet
	public Cursor obtenirTweet(long IDFila) throws SQLException {
		Cursor mCursor = bd.query(true, BD_TAULA,
				new String[] { CLAU_ID, CLAU_USER, CLAU_USER_COMPLET, CLAU_TWEET, CLAU_DATA,
						CLAU_RETWEETS, CLAU_FAVORITS, CLAU_URL }, CLAU_ID + " = "
						+ IDFila, null, null, null, null, null);

		if (mCursor != null)
			mCursor.moveToFirst();

		return mCursor;

	}

	// Retorna tots els tweets
	public Cursor obtenirTotsElsTweets() {
		return bd.query(BD_TAULA, new String[] { CLAU_ID, CLAU_USER, CLAU_USER_COMPLET,
				CLAU_TWEET, CLAU_DATA, CLAU_RETWEETS, CLAU_FAVORITS, CLAU_URL }, null,
				null, null, null, null);
	}

	// Esborra la BD
	public void esborraTaula() {

		bd.execSQL("DROP TABLE IF EXISTS " + BD_TAULA);
		try {
			bd.execSQL(BD_CREATE);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static class AjudaBD extends SQLiteOpenHelper {
		AjudaBD(Context con) {
			super(con, BD_NOM, null, VERSIO);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(BD_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int VersioAntiga,
				int VersioNova) {
			Log.w(TAG, "Actualitzant Base de dades de la versió" + VersioAntiga
					+ " a " + VersioNova + ". Destruirà totes les dades");
			db.execSQL("DROP TABLE IF EXISTS " + BD_TAULA);

			onCreate(db);
		}
	}

	public boolean isEmpty() {
		this.obre();
		Boolean empty;
		Cursor cur =  bd.query(BD_TAULA, new String[] { CLAU_ID, CLAU_USER, CLAU_USER_COMPLET,
				CLAU_TWEET, CLAU_DATA, CLAU_RETWEETS, CLAU_FAVORITS, CLAU_URL }, null,
				null, null, null, null);
		if(cur.moveToFirst()) empty= false;
		else empty= true;
		this.tanca();
		return empty;
	}
}
