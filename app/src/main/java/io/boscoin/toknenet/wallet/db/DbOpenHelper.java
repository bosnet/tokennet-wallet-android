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


	private  static SQLiteDatabase mDB;
	private DatabaseHelper mDBHelper;
	private Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

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

		mDBHelper = new DatabaseHelper(mCtx, dbName, null, Constants.DB.DATABASE_VERSION);
		mDB = mDBHelper.getWritableDatabase();
		return this;

	}

	public void close(){
		mDB.close();

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



	public long insertColumnWallet( String name, String add, String key, int order, String bal, String time){

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

	public static boolean updateColumnWalletBalance(Context contx, long id , String bal){

		boolean isOk;
		SQLiteDatabase wDb;
		DatabaseHelper wHelper;
		wHelper = new DatabaseHelper(contx, Constants.DB.MY_WALLETS, null, Constants.DB.DATABASE_VERSION);
		wDb = wHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_LASTEST, bal);

		isOk = wDb.update(DataBases.CreateWalletDB._TABLENAME, values, "_id="+id, null) > 0 ;
		wDb.close();
		return isOk;
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

	public boolean updateColumnWalletOrder(long id , String order){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateWalletDB.COL_ORDER, order);
		return mDB.update(DataBases.CreateWalletDB._TABLENAME, values, "_id="+id, null) > 0;
	}

	public boolean deleteColumnAddress(long id){
		return mDB.delete(DataBases.CreateAddressDB._TABLENAME, "_id="+id, null) > 0;
	}


	public boolean deleteColumnWallet(long id){
		return mDB.delete(DataBases.CreateWalletDB._TABLENAME, "_id="+id, null) > 0;
	}



	public Cursor getAllColumnsAddress(){

		Cursor c = mDB.query(DataBases.CreateAddressDB._TABLENAME, null,
				null, null, null, null, null);

		if(c != null && c.getCount() != 0)
			c.moveToFirst();
		return c;
	}

	public boolean isSameAddress(String adress){


        Cursor c = mDB.query(DataBases.CreateAddressDB._TABLENAME, new String[] {DataBases.CreateAddressDB.COL_ADDRESS},
                DataBases.CreateAddressDB.COL_ADDRESS+ " == '" + adress + "'",
                null, null, null, null);

		if(c.getCount() > 0 ){
			c.close();

			return true;
		}else{
			c.close();

			return false;
		}
	}

	public static String getAddressName( Context contx, String adress){
		String aName = null;
		SQLiteDatabase adDb;
		DatabaseHelper adHelper;
		adHelper = new DatabaseHelper(contx, Constants.DB.ADDRESS_BOOK, null, Constants.DB.DATABASE_VERSION);
		adDb = adHelper.getWritableDatabase();

		Cursor c = adDb.query(DataBases.CreateAddressDB._TABLENAME, new String[] {DataBases.CreateAddressDB.COL_NAME, DataBases.CreateAddressDB.COL_ADDRESS},
				DataBases.CreateAddressDB.COL_ADDRESS+ " == '" + adress + "'",
				null, null, null, null);

		if(c.getCount() > 0){
			c.moveToFirst();
			aName= c.getString(c.getColumnIndex(DataBases.CreateAddressDB.COL_NAME));

			adDb.close();
			c.close();
			return aName;
		}else{
			adDb.close();
			c.close();
			return aName;
		}


	}

	public boolean isSamePubKey(String key){


        Cursor c = mDB.query(DataBases.CreateWalletDB._TABLENAME, new String[] {DataBases.CreateWalletDB.COL_ADDRESS},
                DataBases.CreateWalletDB.COL_ADDRESS+ " == '" + key + "'",
                null, null, null, null);

		if(c.getCount() > 0 ){

			c.close();
			return true;
		}else{
			c.close();

			return false;
		}
	}

	public boolean isSameBosKey(String key){


        Cursor c = mDB.query(DataBases.CreateWalletDB._TABLENAME, new String[] {DataBases.CreateWalletDB.COL_KEY},
                DataBases.CreateWalletDB.COL_KEY+ " == '" + key + "'",
                null, null, null, null);

		if(c.getCount() > 0 ){
			c.close();

			return true;
		}else{
			c.close();

			return false;
		}
	}

	public Cursor getAllColumnsWallet(){

		Cursor c = mDB.query(DataBases.CreateWalletDB._TABLENAME, null,
				null, null, null, null, null);

		if(c != null && c.getCount() != 0)
			c.moveToFirst();
		return c;
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


	public Cursor getAllColumnWalletName(){
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






