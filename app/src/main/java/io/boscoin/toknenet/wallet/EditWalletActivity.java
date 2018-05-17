package io.boscoin.toknenet.wallet;

import android.content.Intent;
import android.database.Cursor;
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
    private boolean isChName;
    private String mChName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    public void checkSeedKey(View view) {

        //setResult()
    }

    public void checkBosKey(View view) {
    }

    public void deleteWallet(View view) {
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
