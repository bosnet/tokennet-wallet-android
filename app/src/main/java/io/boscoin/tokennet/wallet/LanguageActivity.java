package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;


public class LanguageActivity extends AppCompatActivity {


    private TextView mTvTitle;
    private RadioButton mEng, mKo;
    private Context mContext;
    private static final String KO = "ko";
    private static final String ENG = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mContext =  this;

        setLanguage();


        initUI();



    }

    private void setLanguage() {
        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);
    }

    private void initUI() {

        setContentView(R.layout.activity_language);


        mTvTitle = findViewById(R.id.title);
        mTvTitle.setText(R.string.setting_lang);

        mEng = findViewById(R.id.radioEng);

        mKo = findViewById(R.id.radioKo);

        if(WalletPreference.getWalletLanguage(mContext).equals(KO)){
            mKo.setChecked(true);
        }else if(WalletPreference.getWalletLanguage(mContext).equals(ENG)){
            mEng.setChecked(true);
        }else{
            mEng.setChecked(true);

        }

        mEng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mKo.isChecked()){
                    mKo.setChecked(false);
                    mEng.setChecked(true);
                    changeLanguage(ENG);

                    finish();
                }
            }
        });

        mKo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEng.isChecked()){
                    mEng.setChecked(false);
                    mKo.setChecked(true);
                    changeLanguage(KO);

                    finish();
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

    public void changeEng(View view) {
        mKo.setChecked(false);
        mEng.setChecked(true);
        changeLanguage(ENG);
    }

    public void changeKo(View view) {
        mEng.setChecked(false);
        mKo.setChecked(true);
        changeLanguage(KO);
    }

    public void changeLanguage(String lang) {

        WalletPreference.setWalletLanguage(mContext, lang);

        Intent changeLangBroadcastIt =  new Intent(Constants.Invoke.BROAD_CHANGE_LANG);
        sendBroadcast(changeLangBroadcastIt);

    }



}
