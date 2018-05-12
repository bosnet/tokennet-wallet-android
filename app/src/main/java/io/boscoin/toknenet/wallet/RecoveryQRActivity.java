package io.boscoin.toknenet.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.Utils;


public class RecoveryQRActivity extends AppCompatActivity {

    private Context mContext;
    private TextView walletName , bosKey, mTitle;
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private ImageButton mImgBtn;
    private String mBosKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_qr);
        mContext = this;

        Intent it = getIntent();

        long idx = it.getLongExtra(Constants.Invoke.WALLET,0);

        mImgBtn = findViewById(R.id.btn_back);
        mImgBtn.setVisibility(View.GONE);

        mTitle = findViewById(R.id.title);
        mTitle.setText(R.string.check_reckey);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mCursor = mDbOpenHelper.getColumnWallet(idx);

        walletName = (TextView)findViewById(R.id.title_wallet);
        walletName.setText(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)));

        bosKey = (TextView)findViewById(R.id.txt_boskey);
        mBosKey = mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET));
        bosKey.setText(mBosKey);

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

    }

    public void viewWallet(View view) {
        Intent it = new Intent(RecoveryQRActivity.this, WalletListActivity.class);
        startActivity(it);
        finish();
    }

    public void copyRecoveryKey(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("boskey", mBosKey);
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(mContext, mContext.getString(R.string.toast_text_clipboard_address), Toast.LENGTH_SHORT).show();
    }
}
