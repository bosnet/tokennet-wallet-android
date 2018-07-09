package io.boscoin.toknenet.wallet;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.stellar.sdk.KeyPair;

import java.security.GeneralSecurityException;
import java.util.ArrayList;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.SendDialogPw;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class NoticeQRActivity extends AppCompatActivity {



    private SendDialogPw mPwDialog;
    private Context mContext;
    private String mBosKey, mSeed;
    private KeyPair keyPair;
    private long mIdSeed, mIdBos;
    private boolean checkSeed, checkBos;
    private TextView mTvTitle, mTvNoti;
    private DbOpenHelper mDbOpenHelper;
    private String mName;
    private static final  int KEY_REQUEST = 10;
    private ImageView mIcImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;


        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);

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

        setContentView(R.layout.activity_notice);


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
        mTvTitle = findViewById(R.id.title);
        mTvNoti = findViewById(R.id.noti_1);
        if(checkSeed){
            mTvTitle.setText(R.string.ck_seed);
            mTvNoti.setText(R.string.screen_cap);
        }else{
            mTvTitle.setText(R.string.ck_rckey);
            mTvNoti.setText(R.string.noti_key);
        }

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mIcImg = findViewById(R.id.ic_notice);
        if(checkBos){
            mIcImg.setBackgroundResource(R.drawable.ic_icon_lock);
        }

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
        TextView tvErrView = err;
        String seedkey = pw.getText().toString();

        if(TextUtils.isEmpty(seedkey)){
            tvErrView.setText(R.string.error_no_pw);
            tvErrView.setVisibility(View.VISIBLE);
            return;
        }

        String tmp = mBosKey.substring(3);
        String tmp2 = tmp.substring(0,tmp.length()-2);

        try {
            String dec =  AESCrypt.decrypt(seedkey,tmp2);
            keyPair = KeyPair.fromSecretSeed(dec);
            mSeed = new String(keyPair.getSecretSeed());

            tvErrView.setVisibility(View.GONE);
            mPwDialog.dismiss();
            ArrayList<String> info = new ArrayList<String>();
            info.add(mName);
            info.add(mSeed);
            Intent it = new Intent(NoticeQRActivity.this, ConfirmQRActivity.class);
            it.putStringArrayListExtra(Constants.Invoke.WALLET,info);

            startActivityForResult(it,KEY_REQUEST);
        } catch (GeneralSecurityException e) {
            tvErrView.setText(R.string.error_invalid_password);
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
