package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;



public class EditWalletActivity extends AppCompatActivity {

    private static final String TAG = "EditWalletActivity";
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
        setContentView(R.layout.activity_edit_wallet);

        mContext =  this;

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
                    setResult(Constants.RssultCode.CHANGE_NAME, it);
                }
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EDIT_REQUEST_NAME && resultCode == Constants.RssultCode.CHANGE_NAME){
            mChName = data.getStringExtra(Constants.Invoke.EDIT);
            Log.e(TAG," name = "+mChName);

            mTitle.setText(mChName);
        }else{

            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public void changeWalletName(View view) {
        Intent it = new Intent(EditWalletActivity.this, ChWalletNameActivity.class);
        it.putExtra(Constants.Invoke.EDIT, mIdx);
        startActivityForResult(it, EDIT_REQUEST_NAME);
        //startActivity(it);
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
        mDbOpenHelper.deleteColumnWallet(mIdx);

        mDbOpenHelper.close();
        setResult(Constants.RssultCode.DELETE_WALLET);
        finish();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if(keyCode == KeyEvent.KEYCODE_BACK){
                if(!TextUtils.isEmpty(mChName)){

                    Intent it = new Intent();
                    it.putExtra(Constants.Invoke.EDIT, mChName);
                    setResult(Constants.RssultCode.CHANGE_NAME, it);
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
