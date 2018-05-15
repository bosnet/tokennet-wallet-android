package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Memo;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

import cz.msebera.android.httpclient.Header;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;

public class SendActivity extends AppCompatActivity {

    private static final String TAG = "SendActivity";
    private EditText mEpubKey, mEammount;
    private String mDestion, mSendValue, mBosKey, mSeed;
    private long mIdx;
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private Context mContext;
    private KeyPair keyPair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        mContext = this;

        mEpubKey = (EditText)findViewById(R.id.edit_pubkey);
        mEammount = (EditText)findViewById(R.id.edit_amount);



        Intent it = getIntent();
        mIdx = it.getLongExtra(Constants.Invoke.SEND, 0);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mCursor = mDbOpenHelper.getColumnWallet(mIdx);

        mBosKey = mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET));

        mDbOpenHelper.close();
    }

    public void sendBos(View view) {

        mDestion = mEpubKey.getText().toString();
        Log.e(TAG, "mDestion = "+mDestion);
        mSendValue = mEammount.getText().toString();

        //alert dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle("Input your Password");
        final EditText pw = new EditText(mContext);
        alert.setView(pw);
        alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String seedkey = pw.getText().toString();
                String tmp = mBosKey.substring(3);
                String tmp2 = tmp.substring(0,tmp.length()-2);
                try {
                    String dec =  AESCrypt.decrypt(seedkey,tmp2);
                    keyPair = KeyPair.fromSecretSeed(dec);
                    mSeed = new String(keyPair.getSecretSeed());
                    Log.e(TAG, "seed key = "+mSeed);
                    sendTransation2();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

            }
        });


        alert.setNegativeButton("no",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();

    }


    private void sendTransation2(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(mDestion);

        client.get(String.valueOf(url),new TextHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Log.e(TAG,"sendBOS");
                sendBOS();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG,"createAccount");
                createAccount();

            }
        });
    }


    private void sendBOS() {

        new Thread(){
            public void run(){
                // TODO: 2018. 4. 13. bosnet
                Network.use(new Network(Constants.Network.PASSPHRASE_BOS_TEST));
                Server server = new Server(Constants.Domain.BOS_HORIZON_TEST);

                KeyPair source = KeyPair.fromSecretSeed(mSeed);
                KeyPair destination = KeyPair.fromAccountId(mDestion);

                // First, check to make sure that the destination account exists.
                // You could skip this, but if the account does not exist, you will be charged
                // the transaction fee when the transaction fails.
                // It will throw HttpResponseException if account does not exist or there was another error.

                // If there was no error, load up-to-date information on your account.
                AccountResponse sourceAccount = null;
                try {
                    sourceAccount = server.accounts().account(source);
                    Log.e(TAG,"sourceAccount = "+sourceAccount);

                    // Start building the transaction.
                    Transaction transaction = new Transaction.Builder(sourceAccount)
                            .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), mSendValue).build())
                            // A memo allows you to add your own metadata to a transaction. It's
                            // optional and does not affect how Stellar treats the transaction.
                            .addMemo(Memo.text("Test Transaction"))
                            .build();
                    // Sign the transaction to prove you are actually the person sending it.
                    transaction.sign(source);

                    // And finally, send it off to Stellar!
                    try {
                        SubmitTransactionResponse response = server.submitTransaction(transaction);

                        Utils.printResponse(response);
                        
                        Intent it = new Intent(SendActivity.this, WalletHistoryActivity.class);
                        it.putExtra(Constants.Invoke.HISTORY,mIdx);
                        startActivity(it);

                    } catch (Exception e) {
                        System.out.println("Something went wrong!");
                        System.out.println(e.getMessage());
                        // If the result is unknown (no response body, timeout etc.) we simply resubmit
                        // already built transaction:
                        // SubmitTransactionResponse response = server.submitTransaction(transaction);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();

    }

    private void createAccount() {

        new Thread(){
            public void run(){
                // TODO: 2018. 4. 13. bosnet
                Network.use(new Network(Constants.Network.PASSPHRASE_BOS_TEST));
                Server server = new Server(Constants.Domain.BOS_HORIZON_TEST);

                KeyPair source = KeyPair.fromSecretSeed(mSeed);
                KeyPair destination = KeyPair.fromAccountId(mDestion);

                // If there was no error, load up-to-date information on your account.
                AccountResponse sourceAccount = null;
                try {
                    sourceAccount = server.accounts().account(source);
                    Log.e(TAG,"sourceAccount = "+sourceAccount);

                    // Start building the transaction.
                    Transaction transaction = new Transaction.Builder(sourceAccount)
                            .addOperation(new CreateAccountOperation.Builder(destination, mSendValue/*"0.002"*/).build())
                           // .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), mSendValue).build())
                            // A memo allows you to add your own metadata to a transaction. It's
                            // optional and does not affect how Stellar treats the transaction.
                            .build();
                    // Sign the transaction to prove you are actually the person sending it.
                    transaction.sign(source);

                    // And finally, send it off to Stellar!
                    try {
                        SubmitTransactionResponse response = server.submitTransaction(transaction);
                        System.out.println("Success!");
                        Utils.printResponse(response);

                        Intent it = new Intent(SendActivity.this, WalletHistoryActivity.class);
                        it.putExtra(Constants.Invoke.HISTORY,mIdx);
                        startActivity(it);

                    } catch (Exception e) {
                        System.out.println("Something went wrong!");
                        System.out.println(e.getMessage());
                        // If the result is unknown (no response body, timeout etc.) we simply resubmit
                        // already built transaction:
                        // SubmitTransactionResponse response = server.submitTransaction(transaction);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();
    }
}
