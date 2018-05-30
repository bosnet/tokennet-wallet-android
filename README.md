# Tokennet Wallet For Android
BOSCoins Tokennet wallet for Android by BlockchainOS, Inc

This wallet is designed to enhance security to solve the problem of users losing or taken over their *Secret Seed*. The wallet will introduce the *Recovery Key* instead of *Seed Seed*. The *Recovery Key* is stored in the wallet and can only be decrypted with *Password* which you set at the creation time. As you expected, the *Password* will not be stored in your device.

# Status
The current version has been implemented with 'Android Basic UI'. Using current version you can use the following basic features.

- Sending transaction
- Creating wallet
- Importing wallet(with *Recovery Key*)
- Ordiring wallet list(temporary)
- Receive

# Compatibility

* Minimum Android SDK: API level of 21
* Compile Android SDK: API 26 or later

# Development and Build
To build properly, [java-stellar-sdk](https://github.com/bosnet/java-stellar-sdk) is required. It was modified for BOScoins Tokennet. Please Download it, and then build,

```sh
$ git clone https://github.com/bosnet/java-stellar-sdk.git
$ cd java-stellar-sdk
$ ./gradlew build
```

After building 'java-stellar-sdk' for BOSCoin tokennet, import '.jar' file to the project. [Android Studio](https://developer.android.com/studio/index.html) cleanly imports both 'tokennet-wallet-android' source and '.jar'.

To open in Android Studio:

1. Go to "*File*" menu or the "*Welcome Screen*"
2. Click "*Open*"
3. Navigate to 'tokennet-wallet-android' root directory.

**Note**: Make sure your 'Android SDK' has the 'Android Support Repository' installed, and that your `$ANDROID_HOME` environment variable is pointing at the SDK or add a `local.properties` file in the root project with a `sdk.dir=...` line.
