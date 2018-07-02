package io.boscoin.toknenet.wallet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import io.boscoin.toknenet.wallet.conf.Constants;

public class DbOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private  static SQLiteDatabase mDB;
	private DatabaseHelper mDBHelper;
	private Context mCtx;

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name,
                              CursorFactory factory, int version) {
			super(context, name, factory, version);
		}


		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DataBases.CreateAddressDB._CREATE);
			db.execSQL(DataBases.CreateWalletDB._CREATE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+DataBases.CreateAddressDB._TABLENAME);
			db.execSQL("DROP TABLE IF EXISTS "+DataBases.CreateWalletDB._TABLENAME);
			onCreate(db);
		}

	}

	public DbOpenHelper(Context context){
		this.mCtx = context;
	}

	public DbOpenHelper open(String dbName) throws SQLException {

		mDBHelper = new DatabaseHelper(mCtx, dbName, null, DATABASE_VERSION);
		mDB = mDBHelper.getWritableDatabase();
		return this;

	}

	public void close(){
		mDB.close();
		mDB = null;
	}

	public SQLiteDatabase getmDB(){
	    return mDB;
    }

	public long insertColumnAddress(String name, String add){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateAddressDB.COL_NAME, name);
		values.put(DataBases.CreateAddressDB.COL_ADDRESS, add);
		return mDB.insert(DataBases.CreateAddressDB._TABLENAME, null, values);
	}

	public long insertColumnWallet(String name, String add, String key){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_NAME, name);
		values.put(DataBases.CreateWalletDB.COL_ADDRESS, add);
		values.put(DataBases.CreateWalletDB.COL_KEY, key);
		return mDB.insert(DataBases.CreateWalletDB._TABLENAME, null, values);
	}



	public long insertColumnWallet(String name, String add, String key, int order, String bal, String time){

		long id;

		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_NAME, name);
		values.put(DataBases.CreateWalletDB.COL_ADDRESS, add);
		values.put(DataBases.CreateWalletDB.COL_KEY, key);
		values.put(DataBases.CreateWalletDB.COL_ORDER, order);
		values.put(DataBases.CreateWalletDB.COL_LASTEST, bal);
		values.put(DataBases.CreateWalletDB.COL_TIME, time);

		id = mDB.insert(DataBases.CreateWalletDB._TABLENAME, null, values);

		mDB.close();

		return id;
	}

	public boolean updateColumnAddress(long id , String name, String add){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateAddressDB.COL_NAME, name);
		values.put(DataBases.CreateAddressDB.COL_ADDRESS, add);
		return mDB.update(DataBases.CreateAddressDB._TABLENAME, values, "_id="+id, null) > 0;
	}

	public boolean updateColumnWallet(long id , String name, String add, String key){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_NAME, name);
		values.put(DataBases.CreateWalletDB.COL_ADDRESS, add);
		values.put(DataBases.CreateWalletDB.COL_KEY, key);
		return mDB.update(DataBases.CreateWalletDB._TABLENAME, values, "_id="+id, null) > 0;
	}

	public boolean updateColumnWallet(long id , String name, String add, String key, int order, String bal, String time){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_NAME, name);
		values.put(DataBases.CreateWalletDB.COL_ADDRESS, add);
		values.put(DataBases.CreateWalletDB.COL_KEY, key);
		values.put(DataBases.CreateWalletDB.COL_ORDER, order);
		values.put(DataBases.CreateWalletDB.COL_LASTEST, bal);
		values.put(DataBases.CreateWalletDB.COL_TIME, time);
		return mDB.update(DataBases.CreateWalletDB._TABLENAME, values, "_id="+id, null) > 0;
	}

	public boolean updateColumnWalletBalance(long id , String bal){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_LASTEST, bal);
		return mDB.update(DataBases.CreateWalletDB._TABLENAME, values, "_id="+id, null) > 0;
	}

	public boolean updateColumnWalletName(long id , String name){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_NAME, name);
		return mDB.update(DataBases.CreateWalletDB._TABLENAME, values, "_id="+id, null) > 0;
	}

	public boolean updateColumnWalletKey(long id , String key){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_KEY, key);
		return mDB.update(DataBases.CreateWalletDB._TABLENAME, values, "_id="+id, null) > 0;
	}

	public boolean updateColumnWalletTransTime(long id , String time){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_TIME, time);
		return mDB.update(DataBases.CreateWalletDB._TABLENAME, values, "_id="+id, null) > 0;
	}


	public boolean deleteColumnAddress(long id){
		return mDB.delete(DataBases.CreateAddressDB._TABLENAME, "_id="+id, null) > 0;
	}


	public boolean deleteColumnWallet(long id){
		return mDB.delete(DataBases.CreateWalletDB._TABLENAME, "_id="+id, null) > 0;
	}



	public Cursor getAllColumnsAddress(){
		return mDB.query(DataBases.CreateAddressDB._TABLENAME, null, null, null, null, null, null);
	}

	public Cursor getAllColumnsWallet(){
		return mDB.query(DataBases.CreateWalletDB._TABLENAME, null, null, null, null, null, null);
	}

	public Cursor getColumnAddress(long id){
		Cursor c = mDB.query(DataBases.CreateAddressDB._TABLENAME, null,
				"_id="+id, null, null, null, null);
		if(c != null && c.getCount() != 0)
			c.moveToFirst();
		return c;
	}

	public Cursor getColumnWallet(long id){
		Cursor c = mDB.query(DataBases.CreateWalletDB._TABLENAME, null,
				"_id="+id, null, null, null, null);
		if(c != null && c.getCount() != 0)
			c.moveToFirst();
		return c;
	}


	public Cursor getColumnWalletByOrder(String ordring){
		String orderBy = Constants.DB.WALLET_ORDER + " " +ordring;

		return mDB.query(DataBases.CreateWalletDB._TABLENAME, null,
				Constants.DB.WALLET_ORDER, null, null, null, orderBy);
	}


	public Cursor getColumnWalletName(){
		String[] columns = {Constants.DB.WALLET_NAME};

		Cursor c = mDB.query(DataBases.CreateWalletDB._TABLENAME, columns,
				null, null, null, null, null);

		if(c != null && c.getCount() != 0)
			c.moveToFirst();
		return c;
	}

	public int getWalletCount(){
		Cursor c = mDB.query(DataBases.CreateWalletDB._TABLENAME, null, null, null, null, null, null);
		int count = c.getCount();
		c.close();
		mDB.close();
		return count;
	}

	public int getAddressCount(){
		Cursor c = mDB.query(DataBases.CreateAddressDB._TABLENAME, null, null, null, null, null, null);
		int count = c.getCount();
		c.close();
		mDB.close();
		return count;
	}

	public Cursor getColumnAddressName(){
		String[] columns = {Constants.DB.BOOK_NAME};

		Cursor c = mDB.query(DataBases.CreateAddressDB._TABLENAME, columns,
				null, null, null, null, null);

		if(c != null && c.getCount() != 0)
			c.moveToFirst();
		return c;
	}
}






