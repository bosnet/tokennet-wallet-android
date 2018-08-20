package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;


public class CreateWalletActivity extends AppCompatActivity {


    private static final String SEED_RECOVER = "seedkey-recover";
    private static final String BOS_RECOVER = "boskey-recover";
    private static final int MSG_INSERT_COMPLETE = 1;
    private static final int MSG_STOP = 0;
    private static final int MSG_REQUEST_COMPLETE = 0xff;
    private static final int MSG_REQUEST_ERROR = 0xf0;

    private EditText mEInputName, mEInputPW, mEConfirmPW;
    private Context mContext;
    private KeyPair pair;
    private String isRecover, mKey;
    private TextView mTvTitle, mTvLengthErr, mTvNameAlready, mTvNameNone;
    private TextView mTvPwNone, mTvPwMatch;
    private boolean isBosRecover, isSeedRecover;
    private DbOpenHelper mDbOpenHelper, mDbOpenHelperName;
    private long walletId;
    private static final boolean TEST_GET = true;
    private Cursor mCursor;
    private boolean mAleradyWallet;
    private ProgressDialog mProgDialog;
    private DbInsertThread mThread;
    private TextInputLayout mlayoutName, mlayoutPw, mlayoutConfirm;
    private boolean mIsNameValid, mIsPw1Valid, mIsPw2Valid, mIsNext;
    private Button mBtnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mContext = this;

        setLanguage();


        initUI();

    }

    private void setLanguage() {
        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);
    }

    private void initUI() {

        setContentView(R.layout.activity_create_wallet);

        mlayoutName = findViewById(R.id.input_wallet_name);
        mlayoutPw = findViewById(R.id.pw_layout);
        mlayoutConfirm = findViewById(R.id.comfirm_layout);

        mEInputName = findViewById(R.id.input_wname);
        mTvNameAlready = findViewById(R.id.txt_err_name);
        mTvNameNone = findViewById(R.id.txt_err_name_none);
        mTvPwNone = findViewById(R.id.txt_err_pw_none);
        mTvPwMatch = findViewById(R.id.txt_err_pw_confirm);

        mTvTitle = findViewById(R.id.title);
        mTvTitle.setText(R.string.create_wallet);
        mTvLengthErr = findViewById(R.id.txt_err_name_length);

        Intent it = getIntent();
        isRecover = it.getStringExtra(Constants.Invoke.RECOVER_WALLET);
        mKey = it.getStringExtra(Constants.Invoke.KEY);

        mBtnCreate = findViewById(R.id.btn_create);

        if(isRecover != null && isRecover.equals(BOS_RECOVER)){
            isBosRecover = true;
            isSeedRecover =  false;
            mTvTitle.setText(R.string.import_wallet);

            mlayoutName.setHint(getResources().getString(R.string.input_new_wallet_name));
            mlayoutPw.setHint(getResources().getString(R.string.input_already_pw));
            mlayoutConfirm.setHint(getResources().getString(R.string.confirm_already_pw));

        } else if(isRecover != null && isRecover.equals(SEED_RECOVER)){
            isBosRecover = false;
            isSeedRecover = true;
            mTvTitle.setText(R.string.import_wallet);

            mlayoutName.setHint(getResources().getString(R.string.input_new_wallet_name));
            mlayoutPw.setHint(getResources().getString(R.string.input_new_pw));
            mlayoutConfirm.setHint(getResources().getString(R.string.confirm_new_pw));
        } else{
            isBosRecover = false;
            isSeedRecover = false;

            mlayoutName.setHint(getResources().getString(R.string.input_wallet_name));
            mlayoutPw.setHint(getResources().getString(R.string.enter_pw));
            mlayoutConfirm.setHint(getResources().getString(R.string.confirm_pw));
        }


        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mEInputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String wName = s.toString().trim();
                if(TextUtils.isEmpty(wName)){
                    setErrNameNone();
                    mIsNameValid = false;
                    changeButton();
                } else if(!Utils.isNameValid(wName)){
                    setErrNameValid();
                    mIsNameValid = false;
                    changeButton();
                } else{
                    mDbOpenHelperName = new DbOpenHelper(mContext);
                    mDbOpenHelperName.open(Constants.DB.MY_WALLETS);
                    mCursor = null;
                    mCursor = mDbOpenHelperName.getAllColumnWalletName();

                    if(mCursor.getCount() > 0){
                        do{

                            if(wName.equals(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)))){

                                setErrNameAlready();
                                mDbOpenHelperName.close();
                                mCursor.close();
                                mAleradyWallet = true;
                                changeButton();
                                return;
                            }
                        }while (mCursor.moveToNext());
                    }

                    mDbOpenHelperName.close();
                    mCursor.close();
                    mAleradyWallet = false;
                    setErrNoVisible();
                    mIsNameValid = true;
                    changeButton();
                }
            }
        });

        mEInputPW = (EditText)findViewById(R.id.edit_pw);
        mEConfirmPW = (EditText)findViewById(R.id.confirm_pw);

        mEInputPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String sPw = s.toString().trim();

                mTvPwMatch.setVisibility(View.INVISIBLE);
                if(Utils.isPasswordValid(sPw) ){
                    mTvPwNone.setVisibility(View.GONE);
                    mIsPw1Valid = true;
                }else{
                    mTvPwNone.setVisibility(View.VISIBLE);
                    mIsPw1Valid = false;
                }
                changeButton();
            }
        });

        mEConfirmPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String confirmPw = s.toString().trim();
                mTvPwMatch.setVisibility(View.INVISIBLE);
                if(!Utils.isPasswordValid(confirmPw) ){
                    mTvPwMatch.setText(R.string.rule_pw);
                    mTvPwMatch.setVisibility(View.VISIBLE);
                    mIsPw2Valid = false;

                }else{
                    mIsPw2Valid = true;

                }
                changeButton();
            }
        });

    }

    private void setErrNameAlready(){
        mTvNameNone.setVisibility(View.GONE);
        mTvNameAlready.setVisibility(View.VISIBLE);
        mTvLengthErr.setVisibility(View.INVISIBLE);

        mTvPwMatch.setVisibility(View.INVISIBLE);
    }

    private void setErrNameValid() {
        mTvNameNone.setVisibility(View.GONE);
        mTvNameAlready.setVisibility(View.GONE);
        mTvLengthErr.setVisibility(View.VISIBLE);

        mTvPwMatch.setVisibility(View.INVISIBLE);
    }

    private void setErrNameNone() {
        mTvNameNone.setVisibility(View.VISIBLE);
        mTvNameAlready.setVisibility(View.GONE);
        mTvLengthErr.setVisibility(View.GONE);

        mTvPwMatch.setVisibility(View.INVISIBLE);
    }

    private void setErrPwNone(){
        mTvNameNone.setVisibility(View.GONE);
        mTvNameAlready.setVisibility(View.GONE);
        mTvLengthErr.setVisibility(View.INVISIBLE);

        mTvPwMatch.setVisibility(View.INVISIBLE);
    }

    private void setErrPwMatch(){
        mTvNameNone.setVisibility(View.GONE);
        mTvNameAlready.setVisibility(View.GONE);
        mTvLengthErr.setVisibility(View.GONE);

        mTvPwMatch.setVisibility(View.VISIBLE);
    }

    private void setErrNoVisible(){
        mTvNameNone.setVisibility(View.GONE);
        mTvNameAlready.setVisibility(View.GONE);
        mTvLengthErr.setVisibility(View.INVISIBLE);

        mTvPwMatch.setVisibility(View.INVISIBLE);
    }

    private void setErrRecoverPwVisible(){
        mTvNameNone.setVisibility(View.GONE);
        mTvNameAlready.setVisibility(View.GONE);
        mTvLengthErr.setVisibility(View.INVISIBLE);
        mTvPwNone.setText(R.string.error_bos_pw);
        mTvPwNone.setVisibility(View.VISIBLE);
        mTvPwMatch.setVisibility(View.INVISIBLE);
    }

    public void createWallet(View view) {

        String wName = mEInputName.getText().toString().trim();
        String wPw1 = mEInputPW.getText().toString().trim();
        String wPw2 = mEConfirmPW.getText().toString().trim();



        if(TextUtils.isEmpty(wName)){
            setErrNameNone();
            mEInputName.requestFocus();
            return;
        }

        if(!Utils.isNameValid(wName)){
            setErrNameValid();
            mEInputName.requestFocus();
            return;
        }

        if(mAleradyWallet){

            setErrNameAlready();
            mEInputName.requestFocus();
            return;
        }





        if(TextUtils.isEmpty(wPw1) ){

            setErrPwNone();
            mEInputPW.requestFocus();
            return;
        }



        if(!wPw1.equals(wPw2)){


            mTvPwMatch.setText(R.string.error_match_pw);
            mTvPwMatch.setVisibility(View.VISIBLE);
            mEConfirmPW.requestFocus();
            return;
        }



        if(isBosRecover && mIsNext){

            if(mThread != null &&  !mThread.getStoped()){

                return;
            }

            String tmp = mKey.substring(3);
            String tmp2 = tmp.substring(0,tmp.length()-2);

            try {
                String dec =  AESCrypt.decrypt(wPw1,tmp2);
                createWallet(wName,KeyPair.fromSecretSeed(dec).getAccountId(),mKey);
            } catch (GeneralSecurityException e) {

                setErrRecoverPwVisible();
                e.printStackTrace();

            }

        }else if(isSeedRecover && mIsNext){
            if(mThread != null &&  !mThread.getStoped()){

                return;
            }
            try {
                final String aes =  AESCrypt.encrypt(wPw1, mKey);
                createWallet(wName,KeyPair.fromSecretSeed(mKey).getAccountId(),aes);
            } catch (GeneralSecurityException e) {
                Toast.makeText(getApplicationContext(), R.string.error_create_wallet, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }


        } else{
            //create walllet
            if(!mIsNext){
                return;
            }

            if(mThread != null &&  !mThread.getStoped()){

               return;
            }

            pair = KeyPair.random();

            try {

                final String aes =  AESCrypt.encrypt(wPw1, new String(pair.getSecretSeed()));
                createWallet(wName,pair.getAccountId(),aes);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }

        }

    }

    private void  requestMoney(){
        new Thread(){
            @Override
            public void run() {
                String friendbotUrl = String.format(
                        Constants.Domain.BOS_HORIZON_TEST+"/friendbot?addr=%s",
                        pair.getAccountId());

                try {
                    InputStream response = new URL(friendbotUrl).openStream();
                    String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
                    System.out.println("SUCCESS! You have a new account :)\n" + body);
                    handler.sendEmptyMessage(MSG_REQUEST_COMPLETE);
                } catch (IOException e) {
                    handler.sendEmptyMessage(MSG_REQUEST_ERROR);
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void confirmPwAlert() {

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext );
        alert.setTitle(R.string.title_setup);
        alert.setMessage(R.string.rember_recover).setCancelable(false).setPositiveButton(R.string.ok,
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
        alert.setMessage(R.string.restore).setCancelable(false).setPositiveButton(R.string.ok,
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

    private void showDialogWalt(){

        mProgDialog = new ProgressDialog(mContext);
        mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mProgDialog.setMessage(getResources().getString(R.string.d_walit));
        mProgDialog.setCancelable(false);
        mProgDialog.show();
    }

    private void createWallet(String wname, String pubkey, String enckey){

        showDialogWalt();
        mThread = new DbInsertThread(wname, pubkey, enckey);
        mThread.start();

    }

    final Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INSERT_COMPLETE:

                    if(isBosRecover){
                        mProgDialog.dismiss();
                        mThread.stopThread();
                        confirmRecoverAlert();
                    }else{
                        if( BuildConfig.TOKEN_GET && !isSeedRecover && !isBosRecover){

                            requestMoney();
                        }else{
                            mProgDialog.dismiss();
                            mThread.stopThread();
                            confirmPwAlert();
                        }

                    }
                    break;

                case MSG_STOP:
                    mProgDialog.dismiss();
                    mThread.stopThread();

                    break;

                case MSG_REQUEST_ERROR:
                    mProgDialog.dismiss();
                    Toast.makeText(getApplicationContext(), R.string.error_request_wallet, Toast.LENGTH_LONG).show();
                    break;


                case MSG_REQUEST_COMPLETE:

                    mProgDialog.dismiss();
                    confirmPwAlert();
                    break;

            }

        }
    };



     class DbInsertThread extends Thread {
        boolean stopped = false;
        String pubkey;
        String insertName;
        String encKey;

        public DbInsertThread(String name, String pkey, String ekey) {
            this.stopped = false;
            this.pubkey = pkey;
            this.encKey = ekey;
            this.insertName = name;
        }

        public void stopThread(){
            stopped = true;
        }

        public boolean getStoped(){
            return stopped;
        }

        @Override
        public void run() {

                mDbOpenHelper = new DbOpenHelper(mContext);
                mDbOpenHelper.open(Constants.DB.MY_WALLETS);
                int count = mDbOpenHelper.getWalletCount();
                String time = Utils.getCreateTime(System.currentTimeMillis());

                mDbOpenHelper = new DbOpenHelper(mContext);
                mDbOpenHelper.open(Constants.DB.MY_WALLETS);
                walletId = mDbOpenHelper.insertColumnWallet(insertName,pubkey,encKey, ++count, "0", time);


                handler.sendEmptyMessage(MSG_INSERT_COMPLETE);
                stopped = false;

        }
    }

    private void changeButton(){
         
        if(mIsNameValid && mIsPw1Valid && mIsPw2Valid && !mAleradyWallet){
            mBtnCreate.setBackgroundColor(getResources().getColor(R.color.cerulean));
            mIsNext =true;
        }else{
            mBtnCreate.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
            mIsNext =false;
        }
    }
}
