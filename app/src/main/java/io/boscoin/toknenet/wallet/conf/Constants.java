package io.boscoin.toknenet.wallet.conf;


public class Constants {


	public static final class Invoke {
		public static final String WALLET = "wallet";
		public static final String RECOVER_WALLET = "wallet-recover";
		public static final String KEY = "key";
		public static final String HISTORY = "history";
		public static final String PUBKEY = "public-key";
		public static final String SEND = "send";
		public static final String ADDRESS_BOOK = "address-book";
		public static final String EDIT = "edit";
		public static final String PASSWORD = "password";
		public static final String QR_SEED = "qr-seed";
		public static final String QR_BOS = "qr-bos";
		public static final String SETTING = "setting";
		public static final String BROAD_FINISH = "io.boscoin.tokennet.wallet.finish";
		public static final String BROAD_CHANGE_LANG = "io.boscoin.tokennet.wallet.change.language";
	}

	public static final class DB {
		public static final String ADDRESS_BOOK = "addressbook.db";
		public static final String MY_WALLETS = "my-wallet.db";

		public static final String WALLET_NAME = "name";
		public static final String WALLET_ADDRESS = "accountid";
		public static final String WALLET_KET = "key";
		public static final String WALLET_ORDER = "ordering";
		public static final String WALLET_LASTEST = "lastest";
		public static final String WALLET_LAST_TIME = "ltime";

		public static final String BOOK_NAME = "name";
		public static final String BOOK_ADDRESS = "address";
		public static final int DATABASE_VERSION = 1;
	}

	public static final class Params{
		public static final String ORDER = "order";
		public static final String DESC = "desc";
		public static final String ASC = "asc";
		public static final String CURSOR = "cursor";
		public static final String ACCOUNTS = "accounts";
		public static final String PAYMENTS = "payments";
		public static final String LIMIT = "limit";
	}

	public static final class Status {
		public static final int NOT_FOUND = 404;
	}

	public static final class ResultCode {
		public static final int CHANGE_NAME = 1;
		public static final int DELETE_WALLET = 2;
		public static final int ADDRESS = 3;
		public static final int SEND = 4;
		public static final int FINISH = 5;
	}
}
