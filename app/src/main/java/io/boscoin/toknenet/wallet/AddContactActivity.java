package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.stellar.sdk.KeyPair;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

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
    private final int ADDRESS_REQUEST_CODE = 0x0000ff00;
    private ImageButton mDelAdress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        mContext = this;

        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);

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

        mEPubKey.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEPubKey.setRawInputType(InputType.TYPE_CLASS_TEXT);
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

        mDelAdress =  findViewById(R.id.btn_del);


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

                if(TextUtils.isEmpty(mName)|| !Utils.isNameValid(mName)){
                    mNameEmpty = true;
                    mTvNameErr.setText(R.string.error_book_length);
                    mTvNameErr.setVisibility(View.VISIBLE);
                    changeButton();
                    return;
                }else{
                    mTvNameErr.setVisibility(View.GONE);
                    mNameEmpty = false;
                    mDbOpenHelper = new DbOpenHelper(mContext);
                    mDbOpenHelper.open(Constants.DB.ADDRESS_BOOK);
                    mCursor = null;
                    mCursor = mDbOpenHelper.getColumnAddressName();



                    if(mCursor.getCount() > 0){
                        do{

                            if(mName.equals(mCursor.getString(mCursor.getColumnIndex(Constants.DB.BOOK_NAME)))){

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
                    mDelAdress.setVisibility(View.VISIBLE);
                    mAddressEmpty = false;
                    try{
                        Utils.decodeCheck(Utils.VersionByte.ACCOUNT_ID, mPubKey.toCharArray());
                        mIsAddress = true;
                        mTvAddressErr.setVisibility(View.GONE);



                    }catch (Exception e){
                        mTvAddressErr.setText(R.string.error_invalid_pubkey);
                        mTvAddressErr.setVisibility(View.VISIBLE);
                        mIsAddress = false;
                        return;
                    }

                    changeButton();
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

    public void importAddress(View view) {
        new IntentIntegrator(this).setCaptureActivity(SmallCaptureActivity.class)
                .setRequestCode(ADDRESS_REQUEST_CODE).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult( resultCode, data);

        if(result != null) {
            if(result.getContents() == null) {

            } else {

                switch (requestCode){
                    case ADDRESS_REQUEST_CODE:
                        mEPubKey.setText(result.getContents());
                        break;

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void delAddress(View view) {
        mEPubKey.setText("");
        mDelAdress.setVisibility(View.GONE);
        mAddressEmpty = true;
        changeButton();
    }
}
