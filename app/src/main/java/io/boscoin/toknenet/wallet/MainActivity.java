package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);
    }

    public void makeWallet(View view) {
        Intent it = new Intent(MainActivity.this, CreateNoticeActivity.class);
        startActivity(it);

    }

    public void importWallet(View view) {
        Intent it = new Intent(MainActivity.this, ImportActivity.class);
        startActivity(it);
    }
}
