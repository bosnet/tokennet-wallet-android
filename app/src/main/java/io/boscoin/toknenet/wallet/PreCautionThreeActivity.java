package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class PreCautionThreeActivity extends AppCompatActivity {

    private CheckBox mCheck;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_caution_tree);

        mContext = this;

        findViewById(R.id.caution_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(PreCautionThreeActivity.this,MainActivity.class);
                startActivity(it);
            }
        });

        mCheck = findViewById(R.id.check);
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

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletPreference.setSkipCaution(mContext,false);
                Intent it = new Intent(PreCautionThreeActivity.this,MainActivity.class);
                startActivity(it);
                finish();
            }
        });
    }
}
