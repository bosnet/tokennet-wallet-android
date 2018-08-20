package io.boscoin.tokennet.wallet;

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

import io.boscoin.tokennet.wallet.conf.Constants;
import io.boscoin.tokennet.wallet.db.DbOpenHelper;
import io.boscoin.tokennet.wallet.model.AddressBook;
import io.boscoin.tokennet.wallet.utils.Utils;
import io.boscoin.tokennet.wallet.utils.WalletPreference;

public class EditContactActivity extends AppCompatActivity {


    private EditText editName, editAdress;

    private AddressBook mBook;
    private TextView mErrName;
    private boolean mInValidName, mExistName, mIsNext;
    private Cursor mCursor;
    private DbOpenHelper mDbOpenHelper;
    private Context mContext;
    private Button mEditOk;

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
        setContentView(R.layout.activity_edit_contact);

        Intent it = getIntent();
        mBook = (AddressBook) it.getSerializableExtra(Constants.Invoke.ADDRESS_BOOK);


        findViewById(R.id.add_contact).setVisibility(View.GONE);
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editName = findViewById(R.id.edit_cname);
        editAdress = findViewById(R.id.address);
        mErrName = findViewById(R.id.err_name);
        mEditOk = findViewById(R.id.btn_edit_ok);

        editName.setText(mBook.getAddressName());
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString().trim();

                if(TextUtils.isEmpty(name) || !Utils.isNameValid(name)){
                    mErrName.setText(R.string.error_book_length);
                    mErrName.setVisibility(View.VISIBLE);
                    mInValidName = true;
                }else{
                    if(mBook.getAddressName().equals(name)){
                        mErrName.setVisibility(View.GONE);
                        mErrName.setText(R.string.error_same_edit);
                        mErrName.setVisibility(View.VISIBLE);
                        return;
                    }

                    mErrName.setVisibility(View.GONE);
                    mDbOpenHelper = new DbOpenHelper(mContext);
                    mDbOpenHelper.open(Constants.DB.ADDRESS_BOOK);
                    mCursor = null;
                    mCursor = mDbOpenHelper.getColumnAddressName();

                    do{

                        if(name.equals(mCursor.getString(mCursor.getColumnIndex(Constants.DB.BOOK_NAME)))){

                            mErrName.setText(R.string.error_already);
                            mErrName.setVisibility(View.VISIBLE);
                            mDbOpenHelper.close();
                            mExistName = true;
                            changeButton();
                            return;
                        }
                    }while (mCursor.moveToNext());

                    mInValidName = false;
                    mExistName = false;
                    mErrName.setVisibility(View.GONE);

                    mCursor.close();
                    mDbOpenHelper.close();


                }

                changeButton();
            }


        });

        editAdress.setText(mBook.getAddress());

        mEditOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsNext){
                    addAddressBook();
                }

            }


        });
    }



    private void changeButton() {
        if(!mInValidName && !mExistName ){
            mEditOk.setBackgroundColor(getResources().getColor(R.color.cerulean));
            mIsNext = true;
        }else{
            mEditOk.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
            mIsNext = false;
        }
    }



    private void addAddressBook() {
        if(!mInValidName && !mExistName){
            mBook.setAddressName(editName.getText().toString());
            mDbOpenHelper = new DbOpenHelper(mContext);
            mDbOpenHelper.open(Constants.DB.ADDRESS_BOOK);
            mDbOpenHelper.updateColumnAddress(mBook.getAddressId(),mBook.getAddressName(),mBook.getAddress());
            mDbOpenHelper.close();
            finish();
        }
    }
}
