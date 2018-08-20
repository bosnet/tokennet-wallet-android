package io.boscoin.tokennet.wallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.GeneralSecurityException;

import io.boscoin.tokennet.wallet.conf.Constants;
import io.boscoin.tokennet.wallet.crypt.AESCrypt;
import io.boscoin.tokennet.wallet.db.DbOpenHelper;
import io.boscoin.tokennet.wallet.utils.Utils;
import io.boscoin.tokennet.wallet.utils.WalletPreference;


public class ChPassWordActivity extends AppCompatActivity {

    private long mIdx;
    private Context mContext;
    private DbOpenHelper mDbOpenHelper;
    private String mKey;
    private EditText editNew, editConfirm;
    private TextView mTitle;
    private TextView mTvPwNone, mTvPwMatch;


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

        setContentView(R.layout.activity_ch_pass_word);

        Intent it = getIntent();
        mIdx = it.getLongExtra(Constants.Invoke.EDIT,0);
        mKey = it.getStringExtra(Constants.Invoke.PASSWORD);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editNew = findViewById(R.id.new_pw);
        editNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newPw = s.toString().trim();
                if(Utils.isPasswordValid(newPw) ){
                    mTvPwNone.setVisibility(View.GONE);
                }else{
                    mTvPwNone.setVisibility(View.VISIBLE);
                }
            }
        });



        editConfirm = findViewById(R.id.confirm_pw);

        editConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String cPw = s.toString().trim();
                mTvPwMatch.setVisibility(View.GONE);
                if(!Utils.isPasswordValid(cPw) ){
                    mTvPwMatch.setText(R.string.rule_pw);
                    mTvPwMatch.setVisibility(View.VISIBLE);
                }
            }
        });

        mTitle = findViewById(R.id.title);
        mTitle.setText(R.string.change_wallet_pw);

        mTvPwNone = findViewById(R.id.txt_err_pw_none);
        mTvPwMatch = findViewById(R.id.txt_err_pw_confirm);
    }

    private void setErrPwNone(){
        mTvPwNone.setVisibility(View.VISIBLE);

        mTvPwMatch.setVisibility(View.INVISIBLE);
    }

    private void setErrPwMatch(){

        mTvPwNone.setVisibility(View.INVISIBLE);
        mTvPwMatch.setVisibility(View.VISIBLE);
    }

    public void checkPassWord(View view) {
        String newPw = editNew.getText().toString().trim();
        String confirmPw = editConfirm.getText().toString().trim();

        if(TextUtils.isEmpty(newPw) ){
           
            setErrPwNone();
            editNew.requestFocus();
            return;
        }

        if(!newPw.equals(confirmPw)){


            mTvPwMatch.setText(R.string.error_match_pw);
            mTvPwMatch.setVisibility(View.VISIBLE);
            editConfirm.requestFocus();
            return;
        }


        if(Utils.isPasswordValid(newPw) ){
            mTvPwNone.setVisibility(View.GONE);
        }

        if(!newPw.equals(confirmPw)){
            Toast.makeText(getApplicationContext(), R.string.error_match_pw, Toast.LENGTH_LONG).show();
            return;
        }

        changeWalletPassWord(newPw);
    }

    private void changeWalletPassWord(String pw) {

        try {
            final String aes =  AESCrypt.encrypt(pw, mKey);
            mDbOpenHelper = new DbOpenHelper(this);
            mDbOpenHelper.open(Constants.DB.MY_WALLETS);
            mDbOpenHelper.updateColumnWalletKey(mIdx,aes);
            mDbOpenHelper.close();
            setResult(RESULT_OK);
            finish();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error_create_wallet, Toast.LENGTH_LONG).show();
            return;
        }

    }
}
