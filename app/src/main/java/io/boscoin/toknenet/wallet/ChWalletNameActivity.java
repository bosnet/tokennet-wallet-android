package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;

public class ChWalletNameActivity extends AppCompatActivity {

    private static final String TAG = "ChWalletNameActivity";
    private long mIdx;
    private TextView mTitle;
    private DbOpenHelper mDbOpenHelper;
    private EditText editName, editNewName;
    private String mWalletName;
    private TextView mErrAlreadyName, mErrNameLength;
    private boolean mIsValidName, mExistName, mIsNext;
    private Cursor mCursor;
    private Context mContext;
    private Button mBtnChange;
    private String mChName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ch_wallet_name);

        mContext = this;

        Intent it = getIntent();
        mIdx = it.getLongExtra(Constants.Invoke.EDIT,0);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        Cursor cursor = mDbOpenHelper.getColumnWallet(mIdx);
        mWalletName = cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_NAME));
        mDbOpenHelper.close();
        cursor.close();

        initUI();

    }

    private void initUI() {

        mTitle = findViewById(R.id.title);
        mTitle.setText(R.string.change_wallet_name);

        mErrAlreadyName = findViewById(R.id.txt_err_name);
        mErrNameLength = findViewById(R.id.txt_err_name_length);
        mBtnChange = findViewById(R.id.btn_change);

        editName = findViewById(R.id.ch_wname);
        editName.setText(mWalletName);

        editNewName= findViewById(R.id.new_name);

        editNewName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mChName = s.toString();
                if(TextUtils.isEmpty(mChName) || !Utils.isNameValid(mChName)){
                    mErrNameLength.setVisibility(View.VISIBLE);
                    mErrAlreadyName.setVisibility(View.GONE);
                    mIsValidName = false;
                }else{
                    mErrNameLength.setVisibility(View.GONE);
                    mDbOpenHelper = new DbOpenHelper(mContext);
                    mDbOpenHelper.open(Constants.DB.MY_WALLETS);
                    mCursor = null;
                    mCursor = mDbOpenHelper.getColumnWalletName();

                    do{

                        if(mChName.equals(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)))){

                            mErrAlreadyName.setVisibility(View.VISIBLE);
                            mErrNameLength.setVisibility(View.GONE);
                            mDbOpenHelper.close();
                            mExistName = true;
                            changeButton();
                            return;
                        }
                    }while (mCursor.moveToNext());

                    mIsValidName = true;
                    mExistName = false;

                    mErrNameLength.setVisibility(View.GONE);
                    mErrAlreadyName.setVisibility(View.GONE);

                    mCursor.close();
                    mDbOpenHelper.close();


                }

                changeButton();
            }
        });

        mBtnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsNext){
                    changeName();
                }
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void changeName() {
        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mDbOpenHelper.updateColumnWalletName(mIdx,mChName);
        mDbOpenHelper.close();
        Intent it = new Intent();
        it.putExtra(Constants.Invoke.EDIT, mChName);
        setResult(Constants.RssultCode.CHANGE_NAME, it);
        finish();
    }

    private void changeButton() {
        if(mIsValidName && !mExistName ){
            mBtnChange.setBackgroundColor(getResources().getColor(R.color.cerulean));
            mIsNext = true;
        }else{
            mErrNameLength.setVisibility(View.VISIBLE);
            mErrAlreadyName.setVisibility(View.GONE);
            mBtnChange.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
            mIsNext = false;
        }
    }
}
