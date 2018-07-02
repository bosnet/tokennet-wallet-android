package io.boscoin.toknenet.wallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.stellar.sdk.KeyPair;

import java.io.Serializable;
import java.security.GeneralSecurityException;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;

public class ConfirmPassWordActivity extends AppCompatActivity {



    private long mIdx;
    private Context mContext;
    private DbOpenHelper mDbOpenHelper;
    private String mBosKey, mSeed;
    private EditText editPw;
    private TextView mTvErr, mTitle;
    private Button mBtnNext;
    private boolean mIsNext;
    private KeyPair keyPair;

    private static final int REQUEST_PASSWORD = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ch_wallet_pass_word);

        mContext = this;

        Intent it = getIntent();
        mIdx = it.getLongExtra(Constants.Invoke.EDIT,0);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        Cursor cursor = mDbOpenHelper.getColumnWallet(mIdx);
        mBosKey = cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_KET));
        Log.e(TAG,"key = "+mBosKey);
        mDbOpenHelper.close();
        cursor.close();

        initUI();
    }

    private void initUI() {
        editPw = findViewById(R.id.input_pw);
        mTvErr = findViewById(R.id.txt_err_key);
        mBtnNext = findViewById(R.id.btn_next);
        mTitle = findViewById(R.id.title);
        mTitle.setText(R.string.change_wallet_pw);

        editPw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String seedkey = s.toString();
                String tmp = mBosKey.substring(3);
                String tmp2 = tmp.substring(0,tmp.length()-2);

                try {
                    String dec =  AESCrypt.decrypt(seedkey,tmp2);
                    keyPair = KeyPair.fromSecretSeed(dec);
                    mSeed = new String(keyPair.getSecretSeed());
                    changeButton(true);
                } catch (GeneralSecurityException e) {

                    changeButton(false);
                }
            }
        });

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsNext){
                    Intent it = new Intent(ConfirmPassWordActivity.this, ChPassWordActivity.class);

                    it.putExtra(Constants.Invoke.EDIT, mIdx);
                    it.putExtra(Constants.Invoke.PASSWORD, mSeed);
                    startActivityForResult(it, REQUEST_PASSWORD);
                }
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void changeButton(boolean next) {
        if(next){
            mBtnNext.setBackgroundColor(getResources().getColor(R.color.cerulean));
            mTvErr.setVisibility(View.INVISIBLE);
            mIsNext = true;
        }else{
            mBtnNext.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
            mTvErr.setVisibility(View.VISIBLE);
            mIsNext = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_PASSWORD && resultCode == Activity.RESULT_OK){
            finish();
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }


}
