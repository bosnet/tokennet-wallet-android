package io.boscoin.toknenet.wallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.stellar.sdk.KeyPair;

import java.security.GeneralSecurityException;
import java.util.ArrayList;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.SendDialogPw;

public class NoticeQRActivity extends AppCompatActivity {

    private static final String TAG = "NoticeActivity";

    private SendDialogPw mPwDialog;
    private Context mContext;
    private String mBosKey, mSeed;
    private KeyPair keyPair;
    private long mIdSeed, mIdBos;
    private boolean checkSeed, checkBos;
    private TextView mTitle;
    private DbOpenHelper mDbOpenHelper;
    private String mName;
    private static final  int KEY_REQUEST = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        mContext = this;

        Intent it = getIntent();
        mIdSeed = it.getLongExtra(Constants.Invoke.QR_SEED,0);
        mIdBos = it.getLongExtra(Constants.Invoke.QR_BOS,0);

        if(mIdSeed > 0 && mIdBos == 0){
            checkSeed = true;
            checkBos = false;
            getWalletInfo(mIdSeed);
        }else{
            checkBos = true;
            checkSeed = false;
            getWalletInfo(mIdBos);
        }

        initUI();

    }

    private void getWalletInfo(long id){
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        Cursor cursor = mDbOpenHelper.getColumnWallet(id);
        mBosKey = cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_KET));
        mName = cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_NAME));
        Log.e(TAG,"key = "+mBosKey);
        mDbOpenHelper.close();
        cursor.close();

    }

    private void initUI() {
        mTitle = findViewById(R.id.title);
        if(checkSeed){
            mTitle.setText(R.string.ck_seed);
        }else{
            mTitle.setText(R.string.ck_rckey);
        }

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void notiOk(View view) {
        if(checkSeed){
            passwordDialog();
        }else{
            ArrayList<String> info = new ArrayList<String>();
            info.add(mName);
            info.add(mBosKey);
            Intent it = new Intent(NoticeQRActivity.this, ConfirmQRActivity.class);
            it.putStringArrayListExtra(Constants.Invoke.WALLET,info);
            startActivityForResult(it, KEY_REQUEST);
        }
    }



    View.OnClickListener PwCancellistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPwDialog.dismiss();

        }
    };


    View.OnClickListener PwOklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            EditText inputPw = mPwDialog.getEditPw();
            TextView tvErrKey = mPwDialog.getmTvErrKey();

            isValidPw(inputPw, tvErrKey);

        }
    };

    private void passwordDialog() {
        mPwDialog = new SendDialogPw(mContext,PwOklistener, PwCancellistener);
        mPwDialog.setCancelable(false);
        mPwDialog.show();
    }

    private void isValidPw(EditText pw, TextView err) {
        String seedkey = pw.getText().toString();
        String tmp = mBosKey.substring(3);
        String tmp2 = tmp.substring(0,tmp.length()-2);
        TextView tvErrView = err;
        try {
            String dec =  AESCrypt.decrypt(seedkey,tmp2);
            keyPair = KeyPair.fromSecretSeed(dec);
            mSeed = new String(keyPair.getSecretSeed());
            Log.e(TAG, "seed key = "+mSeed);
            tvErrView.setVisibility(View.GONE);
            mPwDialog.dismiss();
            ArrayList<String> info = new ArrayList<String>();
            info.add(mName);
            info.add(mSeed);
            Intent it = new Intent(NoticeQRActivity.this, ConfirmQRActivity.class);
            it.putStringArrayListExtra(Constants.Invoke.WALLET,info);

            startActivityForResult(it,KEY_REQUEST);
        } catch (GeneralSecurityException e) {

            tvErrView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == KEY_REQUEST && resultCode == RESULT_OK){
            finish();
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}
