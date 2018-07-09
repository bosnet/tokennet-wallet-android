package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class PopupActivity extends AppCompatActivity {

    private String isRecover, mKey;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        mContext = this;

        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView ti = findViewById(R.id.title);
        ti.setText(R.string.t_new_pw);

        Intent it = getIntent();
        isRecover = it.getStringExtra(Constants.Invoke.RECOVER_WALLET);
        mKey = it.getStringExtra(Constants.Invoke.KEY);
    }

    public void newWallet(View view) {
        Intent it = new Intent(PopupActivity.this, CreateWalletActivity.class);
        it.putExtra(Constants.Invoke.RECOVER_WALLET, "seedkey-recover");
        it.putExtra(Constants.Invoke.KEY, mKey);
        startActivity(it);
    }
}
