package io.boscoin.tokennet.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import io.boscoin.tokennet.wallet.conf.Constants;
import io.boscoin.tokennet.wallet.db.DbOpenHelper;
import io.boscoin.tokennet.wallet.utils.Utils;
import io.boscoin.tokennet.wallet.utils.WalletPreference;


public class ImportActivity extends AppCompatActivity {


    private final int SEED_REQUEST_CODE = 0x0000ffff;
    private final int BOSKEY_REQUEST_CODE = 0x0000fff0;
    private EditText eSeedKey, eBosKey;
    private TextView mTitle, mErrSeedKey, mErrBosKey;
    private ImageButton mSeedDel, mBosDel;
    private String mSeedKey, mBosKey;
    private Button mBtnNext;
    private boolean mIsNextSeed, mIsNextBos;
    private Context mContext;
    private DbOpenHelper mDbOpenHelper;

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
        setContentView(R.layout.activity_import);

        eSeedKey = findViewById(R.id.input_seedkey);
        eSeedKey.setImeOptions(EditorInfo.IME_ACTION_DONE);
        eSeedKey.setRawInputType(InputType.TYPE_CLASS_TEXT);

        eBosKey = findViewById(R.id.input_boskey);
        eBosKey.setImeOptions(EditorInfo.IME_ACTION_DONE);
        eBosKey.setRawInputType(InputType.TYPE_CLASS_TEXT);

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

    private void changeButton(){
        if(mIsNextSeed && !mIsNextBos){
            mBtnNext.setBackgroundColor(getResources().getColor(R.color.cerulean));
        }else if(!mIsNextSeed && mIsNextBos){
            mBtnNext.setBackgroundColor(getResources().getColor(R.color.cerulean));
        }else{
            mBtnNext.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
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
                            if(isSamePubKey()){
                                mErrSeedKey.setVisibility(View.GONE);
                                Toast.makeText(mContext,R.string.error_same_wallet,Toast.LENGTH_SHORT).show();
                                mBtnNext.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
                                return;
                            }

                            mErrSeedKey.setVisibility(View.GONE);
                            mSeedDel.setVisibility(View.VISIBLE);
                            mIsNextSeed = true;
                            changeButton();
                        } catch (Exception e){
                            mErrSeedKey.setVisibility(View.VISIBLE);
                            mSeedDel.setVisibility(View.VISIBLE);

                            mIsNextSeed = false;
                            changeButton();
                            return;
                        }
                    }
                }else{
                    mBosKey = s.toString();

                    if(!TextUtils.isEmpty(mBosKey) && Utils.isValidRecoveryKey(mBosKey)){
                        if(isSameBosKey()){
                            mErrBosKey.setVisibility(View.GONE);
                            Toast.makeText(mContext,R.string.error_same_wallet,Toast.LENGTH_SHORT).show();
                            mBtnNext.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
                            return;
                        }
                        mErrBosKey.setVisibility(View.GONE);
                        mBosDel.setVisibility(View.VISIBLE);
                        mIsNextBos = true;

                        changeButton();
                    }else{
                        if(TextUtils.isEmpty(mBosKey)){
                            mErrBosKey.setVisibility(View.GONE);
                            mBosDel.setVisibility(View.GONE);
                        }else{
                            mErrBosKey.setVisibility(View.VISIBLE);
                            mBosDel.setVisibility(View.VISIBLE);
                        }

                        mIsNextBos = false;
                        changeButton();
                        return;
                    }
                }
           }
       };
   }
   private boolean isSamePubKey(){
       String pubKey = KeyPair.fromSecretSeed(mSeedKey).getAccountId();
       mDbOpenHelper = new DbOpenHelper(mContext);
       mDbOpenHelper.open(Constants.DB.MY_WALLETS);
       if(mDbOpenHelper.isSamePubKey(pubKey)){
           mDbOpenHelper.close();
           return true;
       }else{
           mDbOpenHelper.close();
           return false;
       }

   }

   private boolean isSameBosKey(){
       mDbOpenHelper = new DbOpenHelper(mContext);
       mDbOpenHelper.open(Constants.DB.MY_WALLETS);
       if(mDbOpenHelper.isSameBosKey(mBosKey)){
           mDbOpenHelper.close();
           return true;
       }else{
           mDbOpenHelper.close();
           return false;
       }
   }
}
