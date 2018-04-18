package io.boscoin.toknenet.wallet.conf;


public class Constants {


	public static final class Invoke {
		public static final String WALLET = "wallet";
        public static final String RECOVER_WALLET = "wallet-recover";
		public static final String KEY = "key";
		public static final String HISTORY = "history";
		public static final String PUBKEY = "public-key";
		public static final String SEND = "send";
	}

    public static final class DB {
        public static final String ADDRESS_BOOK = "addressbook.db";
        public static final String MY_WALLETS = "my-wallet.db";

		public static final String WALLET_NAME = "name";
		public static final String WALLET_ADDRESS = "accountid";
		public static final String WALLET_KET = "key";
		public static final String WALLET_ORDER = "ordering";
		public static final String WALLET_LASTEST = "lastest";
    }

    public static final class Domain {

		public static final String BOS_HORIZON_TEST = "https://horizon-tokennet-test.dev.boscoin.io";
		public static final String STELLAR_HORIZON_TEST = "https://horizon-testnet.stellar.org";


	}

	public static final class Params{
		public static final String ORDER = "order";
		public static final String DESC = "desc";
		public static final String ACCOUNTS = "accounts";
		public static final String PAYMENTS = "payments";
	}

	public static final class Status {
		public static final int NOT_FOUND = 404;
	}

	public static  final  class Network {
		public static final String PASSPHRASE_BOS = "BOS Token Network ; October 2017";
		public static final String PASSPHRASE_BOS_TEST = "Test BOS Token Network ; tokennet.test; September 2017";
		public static final String PASSPHRASE_STELLAR_TEST = "Test SDF Network ; September 2015";

	}
}
