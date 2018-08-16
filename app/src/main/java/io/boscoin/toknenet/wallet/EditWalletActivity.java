package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Wallet;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;


public class EditWalletActivity extends AppCompatActivity {


    private long mIdx;
    private DbOpenHelper mDbOpenHelper;
    private TextView mTitle;
    private static final int EDIT_REQUEST_NAME = 0x00000f;
    private static final int EDIT_REQUEST_PASS_WORD = 0x0000ff;
    private boolean isChName;
    private String mChName;
    private Context mContext;

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
        setContentView(R.layout.activity_edit_wallet);

        Intent it = getIntent();
        mIdx = it.getLongExtra(Constants.Invoke.EDIT,0);
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        Cursor cursor = mDbOpenHelper.getColumnWallet(mIdx);

        mTitle = findViewById(R.id.title);
        mTitle.setText(cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_NAME)));

        mDbOpenHelper.close();
        cursor.close();



        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(mChName)){

                    Intent it = new Intent();
                    it.putExtra(Constants.Invoke.EDIT, mChName);
                    setResult(Constants.ResultCode.CHANGE_NAME, it);
                }
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EDIT_REQUEST_NAME && resultCode == Constants.ResultCode.CHANGE_NAME){
            mChName = data.getStringExtra(Constants.Invoke.EDIT);


            mTitle.setText(mChName);
        }else{

            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public void changeWalletName(View view) {
        Intent it = new Intent(EditWalletActivity.this, ChWalletNameActivity.class);
        it.putExtra(Constants.Invoke.EDIT, mIdx);
        startActivityForResult(it, EDIT_REQUEST_NAME);

    }

    public void changePassWord(View view) {
        Intent it = new Intent(EditWalletActivity.this, ConfirmPassWordActivity.class);
        it.putExtra(Constants.Invoke.EDIT, mIdx);
        startActivityForResult(it, EDIT_REQUEST_PASS_WORD);
    }

    public void checkSeedKey(View view) {
        Intent it = new Intent(EditWalletActivity.this, NoticeQRActivity.class);
        it.putExtra(Constants.Invoke.QR_SEED, mIdx);
        startActivity(it);
    }

    public void checkBosKey(View view) {
        Intent it = new Intent(EditWalletActivity.this, NoticeQRActivity.class);
        it.putExtra(Constants.Invoke.QR_BOS, mIdx);
        startActivity(it);
    }

    public void deleteWallet(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext );
        alert.setTitle(R.string.delete_w);
        alert.setMessage(R.string.if_you_delete).setCancelable(false).setPositiveButton(R.string.delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteWallet();
                    }
                }).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
        });
        AlertDialog dialog = alert.create();

        dialog.show();


        int dialogTitleId = dialog.getContext().getResources().getIdentifier("android:id/alertTitle",null,null);
        TextView tvTitle = dialog.findViewById(dialogTitleId);
        tvTitle.setTextColor(getResources().getColor(R.color.red_two_87));
        tvTitle.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        tvTitle.setTextSize(14);

        int dialogMsgId = dialog.getContext().getResources().getIdentifier("@android:id/message",null,null);
        TextView tvMsg = dialog.findViewById(dialogMsgId);
        tvMsg.setTextColor(getResources().getColor(R.color.black_87));
        tvMsg.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
        tvMsg.setTextSize(14);

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);

    }

    private void deleteWallet() {

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);

        Cursor c = mDbOpenHelper.getColumnWallet(mIdx);
        long  count = c.getLong(c.getColumnIndex(Constants.DB.WALLET_ORDER));
        mDbOpenHelper.deleteColumnWallet(mIdx);


        mDbOpenHelper.close();

        reOrderingWallet(count);

        setResult(Constants.ResultCode.DELETE_WALLET);
        finish();

    }

    private void reOrderingWallet(long divCount) {
        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);


        Cursor cursor = mDbOpenHelper.getAllColumnsWallet();

        if(cursor.getCount() > 0){
            do{

                long wOrder = cursor.getLong(cursor.getColumnIndex(Constants.DB.WALLET_ORDER));
                long idx = cursor.getLong(cursor.getColumnIndex("_id"));

                if(  wOrder > divCount){
                    mDbOpenHelper.updateColumnWalletOrder(idx, Long.toString( --wOrder));
                }
            }while (cursor.moveToNext());

        }
        mDbOpenHelper.close();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if(keyCode == KeyEvent.KEYCODE_BACK){
                if(!TextUtils.isEmpty(mChName)){

                    Intent it = new Intent();
                    it.putExtra(Constants.Invoke.EDIT, mChName);
                    setResult(Constants.ResultCode.CHANGE_NAME, it);
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
