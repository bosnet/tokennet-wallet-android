package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class SettingActivity extends AppCompatActivity {


    private Context mContext;
    private static final int LANGUAGE_REQUEST = 20;
    private static boolean mChangeLang = false;


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
        setContentView(R.layout.activity_setting);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        TextView ti = findViewById(R.id.title);
        ti.setText(R.string.title_activity_settings);


        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText("BOScoin wallet Version: "+getAppVersion());
    }

    private String getAppVersion() {
        PackageInfo pinfo = null;
        String version ="";
        PackageManager packageManager = mContext.getPackageManager();

        try {
            pinfo =  packageManager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            version = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version+"v";
    }

    public void changeOrder(View view) {
        Intent it = new Intent(SettingActivity.this, WalletOrderActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(it);
    }

    public void changeLanguage(View view) {


        Intent it = new Intent(SettingActivity.this, LanguageActivity.class);

        startActivity(it);


    }

    public void viewPreCaution(View view) {
        Intent it = new Intent(SettingActivity.this, PreCautionOneActivity.class);
        it.putExtra(Constants.Invoke.SEITING, true);
        startActivity(it);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

}
