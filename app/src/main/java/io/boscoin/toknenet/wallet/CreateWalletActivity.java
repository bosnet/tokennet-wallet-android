package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;


public class CreateWalletActivity extends AppCompatActivity {

    private static final String TAG = "CreateWalletActivity";
    private static final String SEED_RECOVER = "seedkey-recover";
    private static final String BOS_RECOVER = "boskey-recover";
    private static final int MAX_WALLET_NAME = 11;
    private static final int MIN_PASSWORD = 7;

    private EditText mEInputName, mEInputPW, mEConfirmPW;
    private Context mContext;
    private KeyPair pair;
    private String isRecover, mKey;
    private TextView mTvTitle, mTvLengthErr, mTvNameErr;
    private boolean isBosRecover, isSeedRecover;
    private DbOpenHelper mDbOpenHelper, mDbOpenHelperName;
    private long walletId;
    private static final boolean TEST_GET = true;
    private Cursor mCursor;
    private boolean mAleradyWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);

        mContext = this;

        mEInputName = findViewById(R.id.input_wname);
        mTvNameErr = findViewById(R.id.txt_err_name);
        mTvTitle = findViewById(R.id.title);
        mTvTitle.setText(R.string.create_wallet);
        mTvLengthErr = findViewById(R.id.txt_err_name_length);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEInputName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mTvNameErr.setVisibility(View.GONE);
                mTvLengthErr.setVisibility(View.GONE);

                if(mDbOpenHelperName != null){
                    mDbOpenHelperName.close();
                    mDbOpenHelperName = null;
                }

                if(!hasFocus){

                    String wName = mEInputName.getText().toString();
                    mDbOpenHelperName = new DbOpenHelper(mContext);
                    mDbOpenHelperName.open(Constants.DB.MY_WALLETS);
                    mCursor = null;
                    mCursor = mDbOpenHelperName.getColumnWalletName();

                    Log.e(TAG,"count = "+mCursor.getCount());
                    if(mCursor.getCount() > 0){
                        do{

                            if(wName.equals(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)))){

                                mTvNameErr.setVisibility(View.VISIBLE);
                                mTvLengthErr.setVisibility(View.GONE);
                                mDbOpenHelperName.close();
                                mDbOpenHelperName = null;
                                mAleradyWallet = true;
                                return;
                            }
                        }while (mCursor.moveToNext());
                    }



                    if(mDbOpenHelperName != null){
                        mDbOpenHelperName.close();
                        mDbOpenHelperName = null;
                    }
                    mAleradyWallet = false;
                }

            }
        });

        mEInputPW = (EditText)findViewById(R.id.edit_pw);
        mEConfirmPW = (EditText)findViewById(R.id.confirm_pw);


        mContext = this;

        Intent it = getIntent();
        isRecover = it.getStringExtra(Constants.Invoke.RECOVER_WALLET);
        mKey = it.getStringExtra(Constants.Invoke.KEY);


        if(isRecover != null && isRecover.equals(BOS_RECOVER)){
            isBosRecover = true;
            isSeedRecover =  false;
            mTvTitle.setText(R.string.import_wallet);
            
        } else if(isRecover != null && isRecover.equals(SEED_RECOVER)){
            isBosRecover = false;
            isSeedRecover = true;
            mTvTitle.setText(R.string.import_wallet);
        } else{
            isBosRecover = false;
            isSeedRecover = false;
        }

    }

    public void createWallet(View view) {

        String wName = mEInputName.getText().toString();
        String wPw1 = mEInputPW.getText().toString();
        String wPw2 = mEConfirmPW.getText().toString();


        if(mAleradyWallet){
            Toast.makeText(getApplicationContext(), R.string.error_already, Toast.LENGTH_LONG).show();
            mEInputName.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(wName) || !isWNameValid(wName)){
            mTvNameErr.setVisibility(View.GONE);
            mTvLengthErr.setVisibility(View.VISIBLE);
            mEInputName.requestFocus();
            return;
        }



        if(TextUtils.isEmpty(wPw1) || TextUtils.isEmpty(wPw2)){
            Toast.makeText(getApplicationContext(), R.string.error_no_pw, Toast.LENGTH_LONG).show();
            mEInputPW.requestFocus();
            return;
        }

        if(!Utils.isPasswordValid(wPw1) || !Utils.isPasswordValid(wPw2)){
            final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setMessage(R.string.error_pw).setCancelable(false).setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mEInputPW.requestFocus();

                        }
                    });
            AlertDialog dialog = alert.create();
            dialog.show();
            return;
        }


        if(!wPw1.equals(wPw2)){
            Toast.makeText(getApplicationContext(), R.string.error_match_pw, Toast.LENGTH_LONG).show();
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
                String time = Utils.getCreateTime(System.currentTimeMillis());
                walletId = mDbOpenHelper.insertColumnWallet(wName,KeyPair.fromSecretSeed(dec).getAccountId(),mKey, ++count,"0", time);
                mDbOpenHelper.close();
                confirmRecoverAlert();

            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.error_bos_pw, Toast.LENGTH_LONG).show();
                return;
            }
        }else if(isSeedRecover){
            try {
                final String aes =  AESCrypt.encrypt(wPw1, mKey);
                mDbOpenHelper = new DbOpenHelper(this);
                mDbOpenHelper.open(Constants.DB.MY_WALLETS);
                int count = mDbOpenHelper.getWalletCount();
                String time = Utils.getCreateTime(System.currentTimeMillis());
                walletId = mDbOpenHelper.insertColumnWallet(wName, KeyPair.fromSecretSeed(mKey).getAccountId(),aes,++count,"0",time);
                mDbOpenHelper.close();
                confirmPwAlert();

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
                String time = Utils.getCreateTime(System.currentTimeMillis());
                walletId = mDbOpenHelper.insertColumnWallet(wName,pair.getAccountId(),aes, ++count, "0", time);
                mDbOpenHelper.close();
                confirmPwAlert();

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


    private void confirmPwAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext );
        alert.setTitle(R.string.title_setup);
        alert.setMessage(R.string.rember_recover).setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        makeQRcode();
                    }
                });
        AlertDialog dialog = alert.create();

        dialog.show();


    }

    private void confirmRecoverAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext );
        alert.setMessage(R.string.restore).setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goWalletList();
                    }
                });
        AlertDialog dialog = alert.create();

        dialog.show();


    }

    private void goWalletList() {
        Intent it = new Intent(CreateWalletActivity.this, WalletListActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(it);
    }

    private void makeQRcode(){

        Intent it = new Intent(CreateWalletActivity.this, RecoveryQRActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        it.putExtra(Constants.Invoke.WALLET, walletId);
        startActivity(it);
    }

    private boolean isWNameValid(String wname) {
        return wname.length() < MAX_WALLET_NAME;
    }




}
