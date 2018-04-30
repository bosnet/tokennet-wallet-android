package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.stellar.sdk.KeyPair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Scanner;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;



public class CreateWalletActivity extends AppCompatActivity {

    private static final String SEED_RECOVER = "seedkey-recover";
    private static final String BOS_RECOVER = "boskey-recover";

    private EditText mIName, mIPw, mCPw;
    private Context mContext;
    private KeyPair pair;
    private String isRecover, mKey;
    private TextView titlePw1, titlePw2;
    private boolean isBosRecover, isSeedRecover;
    private DbOpenHelper mDbOpenHelper;
    private long walletId;
    private static final boolean TEST_GET = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);

        mIName = (EditText)findViewById(R.id.input_wname);
        mIPw = (EditText)findViewById(R.id.edit_pw);
        mCPw = (EditText)findViewById(R.id.confirm_pw);
        titlePw1 = (TextView)findViewById(R.id.title_input_pw);
        titlePw2 = (TextView)findViewById(R.id.title_confirm_pw);

        mContext = this;

        Intent it = getIntent();
        isRecover = it.getStringExtra(Constants.Invoke.RECOVER_WALLET);
        mKey = it.getStringExtra(Constants.Invoke.KEY);


        if(isRecover != null && isRecover.equals(BOS_RECOVER)){
            titlePw1.setText(R.string.input_already_pw);
            titlePw2.setText(R.string.confirm_already_pw);
            isBosRecover = true;
            isSeedRecover =  false;
            
        } else if(isRecover != null && isRecover.equals(SEED_RECOVER)){
            titlePw1.setText(R.string.input_new_pw);
            titlePw2.setText(R.string.confirm_new_pw);
            isBosRecover = false;
            isSeedRecover = true;
        } else{
            titlePw1.setText(R.string.input_new_pw);
            titlePw2.setText(R.string.confirm_new_pw);
            isBosRecover = false;
            isSeedRecover = false;
        }

    }

    public void createWallet(View view) {
        // TODO: 2018. 4. 4. string to byte
        String wName = mIName.getText().toString();
        String wPw1 = mIPw.getText().toString();
        String wPw2 = mCPw.getText().toString();

        if(wName.equals("")){
            Toast.makeText(getApplicationContext(), R.string.error_no_name, Toast.LENGTH_LONG).show();
            return;
        }

        if(wPw1.equals("") || wPw2.equals("")){
            Toast.makeText(getApplicationContext(), R.string.error_no_pw, Toast.LENGTH_LONG).show();
            return;
        }

        if(!wPw1.equals(wPw2)){
            Toast.makeText(getApplicationContext(), R.string.error_no_pw, Toast.LENGTH_LONG).show();
            return;
        }

        if(isBosRecover){
            String tmp = mKey.substring(3);
            String tmp2 = tmp.substring(0,tmp.length()-2);
            try {
                String dec =  AESCrypt.decrypt(wPw1,tmp2);
                mDbOpenHelper = new DbOpenHelper(this);
                mDbOpenHelper.open(Constants.DB.MY_WALLETS);
                int count = mDbOpenHelper.getWalletCount();
                walletId = mDbOpenHelper.insertColumnWallet(wName,KeyPair.fromSecretSeed(dec).getAccountId(),mKey, ++count,"0");
                mDbOpenHelper.close();
                makeQRcode();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.error_match_pw, Toast.LENGTH_LONG).show();
                return;
            }
        }else if(isSeedRecover){
            try {
                final String aes =  AESCrypt.encrypt(wPw1, mKey);
                mDbOpenHelper = new DbOpenHelper(this);
                mDbOpenHelper.open(Constants.DB.MY_WALLETS);
                int count = mDbOpenHelper.getWalletCount();
                walletId = mDbOpenHelper.insertColumnWallet(wName,pair.getAccountId(),aes,++count,"0");
                mDbOpenHelper.close();
                makeQRcode();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.error_create_wallet, Toast.LENGTH_LONG).show();
                return;
            }

        } else{
            //create walllet
            pair = KeyPair.random();

            try {

                final String aes =  AESCrypt.encrypt(wPw1, new String(pair.getSecretSeed()));
                mDbOpenHelper = new DbOpenHelper(this);
                mDbOpenHelper.open(Constants.DB.MY_WALLETS);

                int count = mDbOpenHelper.getWalletCount();
                walletId = mDbOpenHelper.insertColumnWallet(wName,pair.getAccountId(),aes, ++count, "0");
                mDbOpenHelper.close();
                makeQRcode();
                if(TEST_GET){
                    new Thread(){
                        public void run(){
                            String friendbotUrl = String.format(
                                  Constants.Domain.BOS_HORIZON_TEST+"/friendbot?addr=%s",
                                    pair.getAccountId());

                            try {
                                InputStream response = new URL(friendbotUrl).openStream();
                                String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
                                System.out.println("SUCCESS! You have a new account :)\n" + body);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();


                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.error_create_wallet, Toast.LENGTH_LONG).show();
                return;
            } 
        }


    }

    private void makeQRcode(){

        Intent it = new Intent(CreateWalletActivity.this, QRActivity.class);
        it.putExtra(Constants.Invoke.WALLET, walletId);
        startActivity(it);
    }
}
