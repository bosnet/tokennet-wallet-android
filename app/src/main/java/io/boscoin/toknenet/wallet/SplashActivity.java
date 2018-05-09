package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 1000;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = this;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!WalletPreference.getSkipCaution(mContext)){
                    Intent it = new Intent(SplashActivity.this, PreCautionOneActivity.class);
                    startActivity(it);
                    finish();
                }else{
                    Intent it = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(it);
                    finish();
                }
            }
        }, SPLASH_TIME);
    }
}
