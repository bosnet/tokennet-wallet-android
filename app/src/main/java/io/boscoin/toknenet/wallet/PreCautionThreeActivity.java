package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class PreCautionThreeActivity extends AppCompatActivity {

    private CheckBox mCheck;
    private Context mContext;
    private boolean isSetting;
    private TextView mTvView;
    private DbOpenHelper mDbOpenHelper;
    private Button mNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_caution_tree);

        mContext = this;

        Intent it = getIntent();
        isSetting = it.getBooleanExtra(Constants.Invoke.SEITING, false);

        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);

        initUI();

    }

    private void initUI() {
        mNext = findViewById(R.id.caution_next);
        mNext.setText(R.string.ok);
        findViewById(R.id.caution_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSetting){
                    if(getWalleetCount() > 0){
                        Intent finishBroadcastIt =  new Intent(Constants.Invoke.BROAD_FINISH);
                        sendBroadcast(finishBroadcastIt);

                        Intent it = new Intent(PreCautionThreeActivity.this,WalletListActivity.class);
                        startActivity(it);
                        finish();
                    }else{
                        Intent it = new Intent(PreCautionThreeActivity.this,MainActivity.class);
                        startActivity(it);
                    }

                }else{
                    setResult(Constants.ResultCode.FINISH);
                    finish();
                }

            }
        });

        if(!isSetting){
            mCheck = findViewById(R.id.check);
            mCheck.setVisibility(View.VISIBLE);
            mTvView = findViewById(R.id.txt_caution);
            mTvView.setVisibility(View.VISIBLE);
            mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                        alert.setMessage(R.string.no_see_page).setCancelable(false).setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        WalletPreference.setSkipCaution(mContext,true);
                                        Intent it = new Intent(PreCautionThreeActivity.this,MainActivity.class);
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
        }else{
            mCheck = findViewById(R.id.check);
            mCheck.setVisibility(View.GONE);
            mTvView = findViewById(R.id.txt_caution);
            mTvView.setVisibility(View.GONE);
        }


        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSetting){
                    WalletPreference.setSkipCaution(mContext,false);
                    Intent finishBroadcastIt =  new Intent(Constants.Invoke.BROAD_FINISH);
                    sendBroadcast(finishBroadcastIt);
                    if(getWalleetCount() > 0){
                        Intent it = new Intent(PreCautionThreeActivity.this,WalletListActivity.class);
                        startActivity(it);
                    }else{
                        Intent it = new Intent(PreCautionThreeActivity.this,MainActivity.class);
                        startActivity(it);
                    }

                    finish();
                }else{
                    setResult(Constants.ResultCode.FINISH);
                    finish();
                }

            }
        });
    }

    private int getWalleetCount() {
        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        int count = mDbOpenHelper.getWalletCount();

        return count;
    }
}
