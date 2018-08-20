package io.boscoin.tokennet.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.DecimalFormat;
import java.util.Locale;

import javax.validation.Constraint;

import io.boscoin.tokennet.wallet.conf.Constants;
import io.boscoin.tokennet.wallet.db.DbOpenHelper;
import io.boscoin.tokennet.wallet.utils.Utils;
import io.boscoin.tokennet.wallet.utils.WalletPreference;

public class ReceiveActivity extends AppCompatActivity implements View.OnClickListener{


    private long mWalletIdx;
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private TextView mTvName, mTvPubKey, mTvTitle;
    private Context mContext;
    private EditText mEAmount;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###.#######");
    private String result="";
    private String mPubKey;
    private RelativeLayout mNavHis, mNavSend, mNavReceive, mNavContact;
    private ImageView mIcHis, mIcSend, mIcReceive, mIcContact;
    private TextView navTvhis, navTvSend, navTvReceive, navTvContact;
    private static final String AMOUNT_NONE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        mContext =  this;


        setLanguage();







        initUI();
        setNavBottom();
    }

    private void setLanguage() {
        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);
    }

    private void initUI() {

        setContentView(R.layout.activity_receive);

        Intent it = getIntent();

        mWalletIdx = it.getLongExtra(Constants.Invoke.WALLET,0);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mCursor = mDbOpenHelper.getColumnWallet(mWalletIdx);

        mTvName = findViewById(R.id.wallet_name);

        mTvName.setText(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)));

        mTvPubKey = findViewById(R.id.txt_address);

        mTvTitle = findViewById(R.id.title);
        mTvTitle.setText(R.string.receive);

        mPubKey = mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_ADDRESS));
        mTvPubKey.setText(mPubKey);

        mDbOpenHelper.close();

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(mPubKey,
                    BarcodeFormat.QR_CODE, Utils.convertDpToPixel(150,mContext), Utils.convertDpToPixel(150,mContext));
            ImageView imageViewQrCode = (ImageView) findViewById(R.id.img_qr);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        mEAmount = findViewById(R.id.edit_amount);
        mEAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {





            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                String input = s.toString().trim();

                if(input.contains(".") && s.charAt(s.length()-1) != '.'){

                    if(input.indexOf(".") + 8 <= input.length()-1){

                        String formatted = input.substring(0, input.indexOf(".") + 8);
                        mEAmount.setText(formatted);
                        mEAmount.setSelection(formatted.length());
                    }
                }else if(input.contains(",") && s.charAt(s.length()-1) != ','){
                    if(input.indexOf(",") + 8 <= input.length()-1){
                        String formatted = input.substring(0, input.indexOf(",") + 8);
                        mEAmount.setText(formatted);
                        mEAmount.setSelection(formatted.length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {


                createReceiveQR(s.toString());
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createReceiveQR(String amount) {


        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        if(TextUtils.isEmpty(amount)){
            try {
                Bitmap bitmap = barcodeEncoder.encodeBitmap(mPubKey,
                        BarcodeFormat.QR_CODE, Utils.convertDpToPixel(150,mContext), Utils.convertDpToPixel(150,mContext));
                ImageView imageViewQrCode = (ImageView) findViewById(R.id.img_qr);
                imageViewQrCode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }else{
            String message;
            try {
                Bitmap bitmap = barcodeEncoder.encodeBitmap(mPubKey,
                        BarcodeFormat.QR_CODE, Utils.convertDpToPixel(150,mContext), Utils.convertDpToPixel(150,mContext));
                ImageView imageViewQrCode = (ImageView) findViewById(R.id.img_qr);
                imageViewQrCode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

    }

    private void setNavBottom() {
        mNavHis = findViewById(R.id.menu_trans_his);
        mNavSend = findViewById(R.id.menu_send);
        mNavReceive = findViewById(R.id.menu_receive);
        mNavContact = findViewById(R.id.menu_contact);

        mNavHis.setOnClickListener(this);
        mNavSend.setOnClickListener(this);
        mNavReceive.setOnClickListener(this);
        mNavContact.setOnClickListener(this);

        mIcHis = findViewById(R.id.ic_history);
        mIcSend = findViewById(R.id.ic_send);
        mIcReceive = findViewById(R.id.ic_receive);
        mIcContact = findViewById(R.id.ic_contact);
        mIcHis.setBackgroundResource(R.drawable.ic_icon_history_disable);
        mIcSend.setBackgroundResource(R.drawable.ic_icon_send_disable);
        mIcReceive.setBackgroundResource(R.drawable.ic_icon_recieve);
        mIcContact.setBackgroundResource(R.drawable.ic_icon_contacts_disable);

        navTvhis = findViewById(R.id.nav_his);
        navTvSend = findViewById(R.id.nav_send);
        navTvReceive = findViewById(R.id.nav_receive);
        navTvContact = findViewById(R.id.nav_contact);

        navTvhis.setTextColor(getResources().getColor(R.color.brownish_grey));
        navTvSend.setTextColor(getResources().getColor(R.color.brownish_grey));
        navTvReceive.setTextColor(getResources().getColor(R.color.cerulean));
        navTvContact.setTextColor(getResources().getColor(R.color.brownish_grey));
    }


    public void share(View view) {



        String message;
        Intent it = new Intent(android.content.Intent.ACTION_SEND);
        it.setType("text/plain");

        String amount = getResources().getString(R.string.ammount);

        if(TextUtils.isEmpty(mEAmount.getText().toString())){

             message = mPubKey ;
        }else{
             message = mPubKey+ "\n"+amount+" : "+mEAmount.getText().toString()+" BOS";
        }


        it.putExtra(Intent.EXTRA_TEXT, message);


        Intent chooserIntent = Intent.createChooser(it, getString(R.string.req_amount));
        if(chooserIntent == null){
            return;
        }
        try{
            startActivity(chooserIntent);
        } catch (android.content.ActivityNotFoundException e){
            Toast.makeText(this, R.string.error_create_wallet, Toast.LENGTH_SHORT).show();
        }

    }

    public void copy(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if(TextUtils.isEmpty(mEAmount.getText().toString())){
            String amount = getResources().getString(R.string.ammount);
            ClipData clipData = ClipData.newPlainText("address", mPubKey);
            clipboard.setPrimaryClip(clipData);
        }else{
            String amount = getResources().getString(R.string.ammount);
            ClipData clipData = ClipData.newPlainText("address", mPubKey+"\n"+amount+" : "+mEAmount.getText().toString()+" BOS");
            clipboard.setPrimaryClip(clipData);
        }

        Toast.makeText(mContext, mContext.getString(R.string.toast_text_clipboard_address), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        Intent it;
        switch (v.getId()){
            case R.id.menu_trans_his:
                it = new Intent(ReceiveActivity.this, WalletActivity.class);
                it.putExtra(Constants.Invoke.HISTORY,mWalletIdx);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;

            case R.id.menu_send:
                it = new Intent(ReceiveActivity.this, SendActivity.class);
                it.putExtra(Constants.Invoke.SEND, mWalletIdx);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;

            case R.id.menu_receive:
                break;

            case R.id.menu_contact:
                it = new Intent(ReceiveActivity.this, ContactActivity.class);
                it.putExtra(Constants.Invoke.ADDRESS_BOOK, mWalletIdx);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;
        }
    }


}
