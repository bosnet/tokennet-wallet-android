package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.GeneralSecurityException;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;


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
        setContentView(R.layout.activity_ch_pass_word);

        mContext = this;

        Intent it = getIntent();
        mIdx = it.getLongExtra(Constants.Invoke.EDIT,0);
        mKey = it.getStringExtra(Constants.Invoke.PASSWORD);



        initUI();
    }

    private void initUI() {
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editNew = findViewById(R.id.new_pw);
        editConfirm = findViewById(R.id.confirm_pw);
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
        String newPw = editNew.getText().toString();
        String confirmPw = editConfirm.getText().toString();

        if(TextUtils.isEmpty(newPw) ){
           
            setErrPwNone();
            editNew.requestFocus();
            return;
        }

        if(!newPw.equals(confirmPw)){

            setErrPwMatch();
            editConfirm.requestFocus();
            return;
        }

        if(!Utils.isPasswordValid(newPw) || !Utils.isPasswordValid(confirmPw)){
            final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setMessage(R.string.error_pw).setCancelable(false).setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            editNew.requestFocus();

                        }
                    });
            AlertDialog dialog = alert.create();
            dialog.show();
            return;
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
