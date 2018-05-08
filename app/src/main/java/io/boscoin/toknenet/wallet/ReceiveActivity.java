package io.boscoin.toknenet.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.DecimalFormat;

import javax.validation.Constraint;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;

public class ReceiveActivity extends AppCompatActivity {

    private static final String TAG = "ReceiveActivity";
    private long mIdx;
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private TextView mTvName, mTvPubKey;
    private Context mContext;
    private EditText mEAmount;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###.#######");
    private String result="";
    private String mPubKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_receive);

        mContext =  this;

        Intent it = getIntent();

        mIdx = it.getLongExtra(Constants.Invoke.WALLET,0);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mCursor = mDbOpenHelper.getColumnWallet(mIdx);

        mTvName = findViewById(R.id.wallet_name);

        mTvName.setText(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)));

        mTvPubKey = findViewById(R.id.txt_address);

        mPubKey = mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_ADDRESS));
        mTvPubKey.setText(mPubKey);

        mDbOpenHelper.close();

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET)),
                    BarcodeFormat.QR_CODE, Utils.convertDpToPixel(140,mContext), Utils.convertDpToPixel(140,mContext));
            ImageView imageViewQrCode = (ImageView) findViewById(R.id.img_qr);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        mEAmount = findViewById(R.id.edit_amount);


    }


    public void share(View view) {
        Intent it = new Intent(android.content.Intent.ACTION_SEND);
        it.setType("text/plain");
        String message = mPubKey+ "\n"+mEAmount.getText().toString();
        it.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(it);

        Intent chooserIntent = Intent.createChooser(it, getString(R.string.req_amount));
        if (chooserIntent == null) {
            return;
        }
        try {
            startActivity(chooserIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.error_create_wallet, Toast.LENGTH_SHORT).show();
        }


    }

    public void addressCopy(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("address", mPubKey);
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(mContext, mContext.getString(R.string.toast_text_clipboard_address), Toast.LENGTH_SHORT).show();
    }
}
