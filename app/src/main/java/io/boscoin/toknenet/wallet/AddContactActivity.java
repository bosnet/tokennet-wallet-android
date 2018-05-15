package io.boscoin.toknenet.wallet;

import android.content.Context;
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

import org.stellar.sdk.KeyPair;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;

public class AddContactActivity extends AppCompatActivity {

    private static final String TAG = "AddContactActivity";
    private EditText mEName, mEPubKey;
    private Context mContext;
    private Cursor mCursor;
    private DbOpenHelper mDbOpenHelper;
    private TextView mTvNameErr, mTvAddressErr;
    private boolean mSameName, mNameEmpty, mAddressEmpty, mIsAddress;
    private String mName, mPubKey;
    private Button mBtnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        mContext = this;

        initUI();



    }

    private void initUI() {
        findViewById(R.id.add_contact).setVisibility(View.INVISIBLE);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEName = findViewById(R.id.input_cname);
        mEPubKey = findViewById(R.id.input_address);
        mTvNameErr = findViewById(R.id.err_name);
        mTvAddressErr = findViewById(R.id.err_pubkey);
        mBtnOk = findViewById(R.id.btn_add_ok);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInput();
                addAddressBook();

            }
        });



        mEName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mName = s.toString();

                if(TextUtils.isEmpty(mName)){
                    mNameEmpty = true;
                    return;
                }else{
                    mNameEmpty = false;
                    mDbOpenHelper = new DbOpenHelper(mContext);
                    mDbOpenHelper.open(Constants.DB.ADDRESS_BOOK);
                    mCursor = null;
                    mCursor = mDbOpenHelper.getColumnAddressName();

                    Log.e(TAG,"count = "+mCursor.getCount());

                    if(mCursor.getCount() > 0){
                        do{

                            if(mName.equals(mCursor.getString(mCursor.getColumnIndex(Constants.DB.BOOK_NAME)))){
                                Log.e(TAG,"값이 존재");
                                mTvNameErr.setText(R.string.error_already);
                                mTvNameErr.setVisibility(View.VISIBLE);
                                mDbOpenHelper.close();
                                mSameName = true;
                                return;
                            }
                        }while (mCursor.moveToNext());
                    }

                    mCursor.close();
                    mDbOpenHelper.close();
                    mSameName = false;
                    mTvNameErr.setVisibility(View.GONE);
                    changeButton();
                }

            }
        });

        mEPubKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPubKey = s.toString();
                if(TextUtils.isEmpty(mPubKey)){
                    mAddressEmpty = true;
                    return;
                }else{
                    mAddressEmpty = false;
                    try{
                        Utils.decodeCheck(Utils.VersionByte.ACCOUNT_ID, mPubKey.toCharArray());
                        mIsAddress = true;
                        mTvAddressErr.setVisibility(View.GONE);

                        changeButton();

                    }catch (Exception e){
                        mTvAddressErr.setText(R.string.error_invalid_pubkey);
                        mTvAddressErr.setVisibility(View.VISIBLE);
                        mIsAddress = false;
                        return;
                    }

                }

            }
        });

    }

    private void addAddressBook() {

        if(!mSameName && mIsAddress && !mNameEmpty && !mAddressEmpty){
            mDbOpenHelper = new DbOpenHelper(mContext);
            mDbOpenHelper.open(Constants.DB.ADDRESS_BOOK);
            mDbOpenHelper.insertColumnAddress(mName, mPubKey);
            mDbOpenHelper.close();
            finish();
        }

    }



    private void checkInput(){
        String name = mEName.getText().toString();
        String address = mEPubKey.getText().toString();

        if(TextUtils.isEmpty(name)){
            mTvNameErr.setText(R.string.enter_name);
            mTvNameErr.setVisibility(View.VISIBLE);
            return;
        }

        if(TextUtils.isEmpty(address)){
            mTvAddressErr.setText(R.string.enter_pub_address);
            mTvAddressErr.setVisibility(View.VISIBLE);
            return;
        }
    }

    private void changeButton(){
        if(!mSameName && mIsAddress && !mNameEmpty && !mAddressEmpty){
            mBtnOk.setBackgroundColor(getResources().getColor(R.color.cerulean));
        }else{
            mBtnOk.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
        }
    }
}
