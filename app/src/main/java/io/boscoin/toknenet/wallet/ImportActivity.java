package io.boscoin.toknenet.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.utils.Utils;


public class ImportActivity extends AppCompatActivity {

    private static final String TAG = "ImportActivity";
    private final int SEED_REQUEST_CODE = 0x0000ffff;
    private final int BOSKEY_REQUEST_CODE = 0x0000fff0;
    private EditText eSeedKey, eBosKey;
    private TextView mTitle, mErrSeedKey, mErrBosKey;
    private ImageButton mSeedDel, mBosDel;
    private String mSeedKey, mBosKey;
    private Button mBtnNext;
    private boolean mIsNextSeed, mIsNextBos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        eSeedKey = findViewById(R.id.input_seedkey);
        eBosKey = findViewById(R.id.input_boskey);
        eSeedKey.addTextChangedListener(whichTextWatcher(eSeedKey, true));
        eBosKey.addTextChangedListener(whichTextWatcher(eBosKey, false));

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitle = findViewById(R.id.title);
        mTitle.setText(R.string.import_wallet);

        mSeedDel = findViewById(R.id.btn_del_seed);
        mBosDel = findViewById(R.id.btn_del_bos);


        mErrSeedKey = findViewById(R.id.txt_err_seckey);
        mErrBosKey = findViewById(R.id.txt_err_bkey);



        mBtnNext = findViewById(R.id.btn_next);

    }



    public void readQRSeed(View view) {
        new IntentIntegrator(this).setCaptureActivity(SmallCaptureActivity.class)
                .setRequestCode(SEED_REQUEST_CODE).initiateScan();
    }

    public void readQRBosKey(View view) {
        new IntentIntegrator(this).setOrientationLocked(true).setCaptureActivity(SmallCaptureActivity.class)
                .setRequestCode(BOSKEY_REQUEST_CODE).initiateScan();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult( resultCode, data);

        if(result != null) {
            if(result.getContents() == null) {

            } else {

                switch (requestCode){
                    case SEED_REQUEST_CODE:
                        eSeedKey.setText(result.getContents());
                        break;

                    case BOSKEY_REQUEST_CODE:
                        eBosKey.setText(result.getContents());
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void recoverWallet(View view) {


        if(mIsNextSeed && !mIsNextBos){
            Intent it = new Intent(ImportActivity.this, PopupActivity.class);
            it.putExtra(Constants.Invoke.RECOVER_WALLET, "seedkey-recover");
            it.putExtra(Constants.Invoke.KEY, mSeedKey);
            startActivity(it);
        } else if(!mIsNextSeed && mIsNextBos){
            Intent it = new Intent(ImportActivity.this, CreateWalletActivity.class);
            it.putExtra(Constants.Invoke.RECOVER_WALLET, "boskey-recover");
            it.putExtra(Constants.Invoke.KEY, mBosKey);
            startActivity(it);
        } else{
            Toast.makeText(this, R.string.p_enter_your, Toast.LENGTH_LONG).show();
            return;
        }


    }

    public void deleteSeedKey(View view) {
        eSeedKey.setText("");
        mErrSeedKey.setVisibility(View.GONE);
        mSeedDel.setVisibility(View.GONE);
        mBtnNext.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
        mIsNextSeed = false;
    }

    public void deleteBosKey(View view) {
        eBosKey.setText("");
        mErrBosKey.setVisibility(View.GONE);
        mBosDel.setVisibility(View.GONE);
        mBtnNext.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
        mIsNextBos = false;
    }


   private TextWatcher whichTextWatcher(final EditText editText, final boolean isSeedkey){
       return new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {

           }

           @Override
           public void afterTextChanged(Editable s) {
                if(isSeedkey){
                    mSeedKey = s.toString();
                    if(!TextUtils.isEmpty(mSeedKey)){
                        try{
                            Utils.decodeCheck(Utils.VersionByte.SEED, mSeedKey.toCharArray());
                            mBtnNext.setBackgroundColor(getResources().getColor(R.color.cerulean));
                            mErrSeedKey.setVisibility(View.GONE);
                            mSeedDel.setVisibility(View.VISIBLE);
                            mIsNextSeed = true;
                        } catch (Exception e){
                            mErrSeedKey.setVisibility(View.VISIBLE);
                            mSeedDel.setVisibility(View.VISIBLE);
                            mBtnNext.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
                            mIsNextSeed = false;
                            return;
                        }
                    }
                }else{
                    mBosKey = s.toString();

                    if(!TextUtils.isEmpty(mBosKey) && Utils.isValidRecoveryKey(mBosKey)){
                        mErrBosKey.setVisibility(View.GONE);
                        mBosDel.setVisibility(View.VISIBLE);
                        mIsNextBos = true;
                        mBtnNext.setBackgroundColor(getResources().getColor(R.color.cerulean));
                    }else{
                        mErrBosKey.setVisibility(View.VISIBLE);
                        mBosDel.setVisibility(View.VISIBLE);
                        mBtnNext.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
                        mIsNextBos = false;
                        return;
                    }
                }
           }
       };
   }
}
