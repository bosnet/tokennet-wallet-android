# Tokennet Wallet For Android

[![CLA assistant](https://cla-assistant.io/readme/badge/bosnet/tokennet-wallet-android)](https://cla-assistant.io/bosnet/tokennet-wallet-android)

BOSCoin Tokennet wallet for Android by BlockchainOS, Inc

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

# Secret seed Key
secret seed key is used to produce from KeyPair.random() method. Refer[java-stellar-sdk](https://github.com/bosnet/java-stellar-sdk)
This secret secret key is encoded to make it easier to read and transfer, hence, it is some what vulnerable to security.
To resolve this problem, BOScoin Tokennet Wallet made recovery key to enhance security.


# Recovery Key
Recovery key is structured like so:


 'BOS' + encoded secret key + Platform + Database version 

Where,
* BOS prefix for identification recovery key is created from BOScoin Tokennet Wallet. 
* Encoded secret key is created from secret seed key and password entered by user.
* Identifier for Platform indicates from which platform it was created.(In order to cope with cross platform problem.)
* Identifier for Android Database version indicates using when upgrading the database.

#How it Works
How secrete seed key is actually encrypted by using BOScoin Tokennet Wallet.


Encrypt like below so: (refer crypt/AESCrypt.java)
```java
String password = "input user passward";
String sskey = "secret seed key";	
try {
      AESCrypt.encrypt(password, sskey);
} catch (GeneralSecurityException e) {
     //hadle error

}
```
Decrypt like below so: (refer crypt/AESCrypt.java)
```java
String password = "password";
String bkey = "BOSu86XBHDvnur.....h11PA1";
//prease remove remove prefix(BOS) and subfix(A1). then 
String enckey = "u86XBHDvnur.....h11P";

 try {
       String dec =  AESCrypt.decrypt(password,enckey);
       keyPair = KeyPair.fromSecretSeed(dec);
       mSeed = new String(keyPair.getSecretSeed());
} catch (GeneralSecurityException e) {
     //hadle error
}
```

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
