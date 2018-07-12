package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class PreCautionOneActivity extends AppCompatActivity {

    private CheckBox mCheck;
    private Context mContext;
    private boolean isSetting;
    private TextView mTvView;
    private static final int REQUEST_FINISH = 18;
    private DbOpenHelper mDbOpenHelper;

    private BroadcastReceiver finishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PreCautionOneActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mContext = this;

        setLanguage();

        initUI();

        registerFinishedReceiver();
    }

    private void setLanguage() {
        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);
    }

    private void initUI() {
        setContentView(R.layout.activity_pre_caution_one);

        Intent it = getIntent();
        isSetting = it.getBooleanExtra(Constants.Invoke.SEITING, false);

        if(!isSetting){
            mCheck = findViewById(R.id.check);
            mCheck.setVisibility(View.VISIBLE);
            mTvView = findViewById(R.id.txt_caution);
            mTvView.setVisibility(View.VISIBLE);
            mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(mCheck.isChecked()){

                        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                        alert.setMessage(R.string.no_see_page).setCancelable(false).setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        WalletPreference.setSkipCaution(mContext,true);
                                        Intent it = new Intent(PreCautionOneActivity.this,MainActivity.class);
                                        startActivity(it);
                                        finish();
                                    }
                                }).setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mCheck.setChecked(false);
                                    }
                                });
                        AlertDialog dialog = alert.create();
                        dialog.show();

                    }
                }
            });
        } else{
            mCheck = findViewById(R.id.check);
            mCheck.setVisibility(View.GONE);
            mTvView = findViewById(R.id.txt_caution);
            mTvView.setVisibility(View.GONE);
        }

        findViewById(R.id.caution_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSetting)
                    WalletPreference.setSkipCaution(mContext,false);

                Intent it = new Intent(PreCautionOneActivity.this,PreCautionTwoActivity.class);
                it.putExtra(Constants.Invoke.SEITING, isSetting);

                startActivityForResult(it, REQUEST_FINISH);
            }
        });



        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSetting){
                    WalletPreference.setSkipCaution(mContext,false);
                    if(getWalleetCount() > 0){
                        Intent it = new Intent(PreCautionOneActivity.this,WalletListActivity.class);
                        startActivity(it);
                    }else{
                        Intent it = new Intent(PreCautionOneActivity.this,MainActivity.class);
                        startActivity(it);
                    }

                    finish();
                }else{
                    finish();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterFinishedReceiver();
        super.onDestroy();
    }

    private void unregisterFinishedReceiver() {
        unregisterReceiver(finishedReceiver);
    }

    private void registerFinishedReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.Invoke.BROAD_FINISH);
        registerReceiver(finishedReceiver, intentFilter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Constants.ResultCode.FINISH){
            finish();
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private int getWalleetCount() {
        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        int count = mDbOpenHelper.getWalletCount();

        return count;
    }
}
