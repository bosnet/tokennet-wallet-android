package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Wallet;


public class QRActivity extends AppCompatActivity {

    private Context mContext;
    private TextView walletName , bosKey;
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        mContext = this;

        Intent it = getIntent();

        long idx = it.getLongExtra(Constants.Invoke.WALLET,0);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mCursor = mDbOpenHelper.getColumnWallet(idx);

        walletName = (TextView)findViewById(R.id.title_wallet);
        walletName.setText(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)));

        bosKey = (TextView)findViewById(R.id.txt_boskey);
        bosKey.setText(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET)));

        mDbOpenHelper.close();
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET)),
                              BarcodeFormat.QR_CODE, 600, 600);
            ImageView imageViewQrCode = (ImageView) findViewById(R.id.img_qr);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    public void viewWallet(View view) {
        Intent it = new Intent(QRActivity.this, WalletListActivity.class);
        startActivity(it);
    }
}
