package io.boscoin.toknenet.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class ConfirmQRActivity extends AppCompatActivity {

    private Context mContext;
    private ArrayList<String> mInfo;
    private TextView walletName , mTvKey, mTitle, mNoti;
    private  String mKey, mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_qr);

        mContext = this;

        Intent it = getIntent();

        mInfo = it.getStringArrayListExtra(Constants.Invoke.WALLET);

        mName = mInfo.get(0);
        mKey = mInfo.get(1);

        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);

        iniUI();

    }

    private void iniUI() {
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        walletName = (TextView)findViewById(R.id.title_wallet);
        walletName.setText(mName);

        mTvKey = (TextView)findViewById(R.id.txt_boskey);
        mTvKey.setText(mKey);

        mNoti = findViewById(R.id.txt_noti);


        mTitle = findViewById(R.id.title);
        if(mKey.startsWith("S")){
            mTitle.setText(R.string.check_seed);
            mNoti.setText(R.string.sec_key_noti);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }else{
            mTitle.setText(R.string.check_reckey);
            mNoti.setText(R.string.rc_key_noti);
        }



        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(mInfo.get(1),
                    BarcodeFormat.QR_CODE, Utils.convertDpToPixel(150,mContext), Utils.convertDpToPixel(150,mContext));
            ImageView imageViewQrCode = (ImageView) findViewById(R.id.img_qr);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public void copyKey(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("key", mInfo.get(1));
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(mContext, mContext.getString(R.string.toast_text_clipboard_address), Toast.LENGTH_SHORT).show();
    }

    public void confirmOk(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
