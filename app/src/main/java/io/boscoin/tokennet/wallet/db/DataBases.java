package io.boscoin.toknenet.wallet.db;

import android.provider.BaseColumns;

import io.boscoin.toknenet.wallet.conf.Constants;

// DataBase Table
public final class DataBases {

	// To prevent someone from accidentally instantiating the contract class,
	// make the constructor private.
	private DataBases(){};

	public static final class CreateAddressDB implements BaseColumns {
		public static final String COL_NAME = Constants.DB.BOOK_NAME;
		public static final String COL_ADDRESS = Constants.DB.BOOK_ADDRESS;
		public static final String _TABLENAME = "address";
		public static final String _CREATE =
				"CREATE TABLE "+_TABLENAME+"("
						+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
						+COL_NAME+" Text NOT NULL , "
						+COL_ADDRESS+" Text NOT NULL )";
	}


	public static final class CreateWalletDB implements BaseColumns {
		public static final String COL_NAME = Constants.DB.WALLET_NAME;
		public static final String COL_ADDRESS = Constants.DB.WALLET_ADDRESS;
		public static final String COL_KEY = Constants.DB.WALLET_KET;
		public static final String COL_ORDER = Constants.DB.WALLET_ORDER;
		public static final String COL_LASTEST = Constants.DB.WALLET_LASTEST;
		public static final String COL_TIME = Constants.DB.WALLET_LAST_TIME;
		public static final String _TABLENAME = "wallets";
		public static final String _CREATE =
				"CREATE TABLE "+_TABLENAME+"("
						+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
						+COL_NAME+" Text NOT NULL , "
						+COL_ADDRESS+" Text NOT NULL ,"
						+COL_KEY+" Text NOT NULL , "
						+COL_ORDER+" Integer DEFAULT 0 , "
		                +COL_LASTEST+" Text NOT NULL ,"
						+COL_TIME+" Text NOT NULL )";
	}
}
