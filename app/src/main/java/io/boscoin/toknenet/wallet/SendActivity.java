package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

import cz.msebera.android.httpclient.Header;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.utils.SendDialogComplete;
import io.boscoin.toknenet.wallet.utils.SendDialogConfirm;
import io.boscoin.toknenet.wallet.utils.SendDialogPw;
import io.boscoin.toknenet.wallet.utils.Utils;

public class SendActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "Send2Activity";
    private long mWalletId;
    private String mPubKey, mBosKey, mSeed;
    private EditText editPubkey, editAmmount;
    private ImageView mImgDel;
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private Context mContext;
    private final int ADDRESS_REQUEST_QR_CODE = 14;
    private final int ADDRESS_REQUEST_CODE = 13;
    private TextView mTvAddressErr;
    private RelativeLayout mNavHis, mNavSend, mNavReceive, mNavContact;
    private ImageView mIcHis, mIcSend, mIcReceive, mIcContact;
    private TextView navTvhis, navTvSend, navTvReceive, navTvContact;
    private boolean mValidAddress, mValidAmmount, isNext;
    private Button mBtnSend;
    private String mDestion, mSendValue, mSendTotal;
    private SendDialogConfirm mConfirmDialog;
    private SendDialogPw mPwDialog;
    private SendDialogComplete mCompleteDialog;
    private static final double SEND_FEE = 0.001;
    private KeyPair keyPair;
    private ProgressDialog mProgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        mContext = this;

        Intent it = getIntent();
        mWalletId = it.getLongExtra(Constants.Invoke.SEND, 0);
        mPubKey = it.getStringExtra(Constants.Invoke.PUBKEY);

        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mCursor = mDbOpenHelper.getColumnWallet(mWalletId);

        mBosKey = mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET));

        mDbOpenHelper.close();
        mCursor.close();

        initUI();
    }

    private void initUI() {
        editPubkey = findViewById(R.id.input_address);
        editAmmount = findViewById(R.id.input_ammount);
        mImgDel = findViewById(R.id.del_address);
        mTvAddressErr = findViewById(R.id.err_pubkey);
        mBtnSend = findViewById(R.id.btn_send);


        findViewById(R.id.btn_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getAddressCount() > 0 ){
                    Intent it = new Intent(SendActivity.this, ContactActivity.class);
                    it.putExtra(Constants.Invoke.SEND, true);
                    startActivityForResult(it,ADDRESS_REQUEST_CODE );
                }else{
                    final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setMessage(R.string.error_no_count_address).setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                }
                            });
                    AlertDialog dialog = alert.create();
                    dialog.show();
                    return;
                }
            }
        });

        editPubkey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                mPubKey = s.toString();
                if(!TextUtils.isEmpty(mPubKey)){
                    try{
                        Utils.decodeCheck(Utils.VersionByte.ACCOUNT_ID, mPubKey.toCharArray());
                        mTvAddressErr.setVisibility(View.GONE);
                        mImgDel.setVisibility(View.VISIBLE);
                        mValidAddress = true;
                        changeButton();
                    }catch (Exception e){
                        mTvAddressErr.setText(R.string.error_invalid_pubkey);
                        mTvAddressErr.setVisibility(View.VISIBLE);
                        mImgDel.setVisibility(View.GONE);
                        mValidAddress = false;
                    }
                }
            }
        });

        if(mPubKey != null){
            editPubkey.setText(mPubKey);
        }


        editAmmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String ammount = s.toString();
                if(!TextUtils.isEmpty(ammount)){
                    mValidAmmount = true;
                    changeButton();
                }else{
                    mValidAmmount = false;
                }
            }
        });


        mImgDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPubkey.setText("");
                mImgDel.setVisibility(View.GONE);
            }
        });


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
        mIcSend.setBackgroundResource(R.drawable.ic_icon_send_normal);
        mIcReceive.setBackgroundResource(R.drawable.ic_icon_recieve_disable);
        mIcContact.setBackgroundResource(R.drawable.ic_icon_contacts_disable);

        navTvhis = findViewById(R.id.nav_his);
        navTvSend = findViewById(R.id.nav_send);
        navTvReceive = findViewById(R.id.nav_receive);
        navTvContact = findViewById(R.id.nav_contact);

        navTvhis.setTextColor(getResources().getColor(R.color.brownish_grey));
        navTvSend.setTextColor(getResources().getColor(R.color.cerulean));
        navTvReceive.setTextColor(getResources().getColor(R.color.brownish_grey));
        navTvContact.setTextColor(getResources().getColor(R.color.brownish_grey));

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private int getAddressCount() {
        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.ADDRESS_BOOK);
        int count = mDbOpenHelper.getAddressCount();
        mDbOpenHelper.close();
        return count;
    }

    public void showSendBos(View view) {
        Log.e(TAG, "isNext = "+isNext);
       if(isNext){
           mDestion = editPubkey.getText().toString();
           Log.e(TAG, "mDestion = "+mDestion);
           mSendValue = editAmmount.getText().toString();
           double val = Double.parseDouble(mSendValue)+ SEND_FEE;
           mSendTotal = Double.toString(val);
           sendConfirmDialog();
       }

    }

    View.OnClickListener sendOklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConfirmDialog.dismiss();
            sendPwDialog();
        }
    };

    View.OnClickListener PwOklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            EditText inputPw = mPwDialog.getEditPw();
            TextView tvErrKey = mPwDialog.getmTvErrKey();

            isValidPw(inputPw, tvErrKey);

        }
    };

    private void isValidPw(EditText pw, TextView err) {
        String seedkey = pw.getText().toString();
        String tmp = mBosKey.substring(3);
        String tmp2 = tmp.substring(0,tmp.length()-2);
        TextView tvErrView = err;
        try {
            String dec =  AESCrypt.decrypt(seedkey,tmp2);
            keyPair = KeyPair.fromSecretSeed(dec);
            mSeed = new String(keyPair.getSecretSeed());
            Log.e(TAG, "seed key = "+mSeed);
            tvErrView.setVisibility(View.GONE);
            mPwDialog.dismiss();
            mProgDialog = new ProgressDialog(mContext);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgDialog.setMessage("Please Wait");
            mProgDialog.setCancelable(false);
            mProgDialog.show();
            sendBosTransation();
        } catch (GeneralSecurityException e) {
           // e.printStackTrace();
            tvErrView.setVisibility(View.VISIBLE);
        }
    }

    private void sendBosTransation() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(mDestion);

        client.get(String.valueOf(url),new TextHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Log.e(TAG,"sendBOS");
                sendBOS();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG,"createAccount");
                createAccount();

            }
        });
    }

    private void createAccount() {
        new Thread(){
            public void run(){
                // TODO: 2018. 4. 13. bosnet
                Network.use(new Network(Constants.Network.PASSPHRASE_BOS_TEST));
                Server server = new Server(Constants.Domain.BOS_HORIZON_TEST);

                KeyPair source = KeyPair.fromSecretSeed(mSeed);
                KeyPair destination = KeyPair.fromAccountId(mDestion);

                // If there was no error, load up-to-date information on your account.
                AccountResponse sourceAccount = null;
                try {
                    sourceAccount = server.accounts().account(source);
                    Log.e(TAG,"sourceAccount = "+sourceAccount);

                    // Start building the transaction.
                    Transaction transaction = new Transaction.Builder(sourceAccount)
                            .addOperation(new CreateAccountOperation.Builder(destination, mSendValue/*"0.002"*/).build())
                            .build();
                    // Sign the transaction to prove you are actually the person sending it.
                    transaction.sign(source);

                    // And finally, send it off to Stellar!
                    try {
                        SubmitTransactionResponse response = server.submitTransaction(transaction);
                        System.out.println("Success!");
                        Utils.printResponse(response);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgDialog.dismiss();
                                sendCompleteDialog();
                            }
                        });


                    } catch (Exception e) {
                        System.out.println("Something went wrong!");
                        System.out.println(e.getMessage());

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();
    }

    private void sendBOS() {
        new Thread(){
            public void run(){

                Network.use(new Network(Constants.Network.PASSPHRASE_BOS_TEST));
                Server server = new Server(Constants.Domain.BOS_HORIZON_TEST);

                KeyPair source = KeyPair.fromSecretSeed(mSeed);
                KeyPair destination = KeyPair.fromAccountId(mDestion);

                AccountResponse sourceAccount = null;
                try {
                    sourceAccount = server.accounts().account(source);
                    Log.e(TAG,"sourceAccount = "+sourceAccount);

                    // Start building the transaction.
                    Transaction transaction = new Transaction.Builder(sourceAccount)
                            .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), mSendValue).build())
                            .build();

                    // Sign the transaction to prove you are actually the person sending it.
                    transaction.sign(source);

                    // And finally, send it off to Stellar!
                    try {
                        SubmitTransactionResponse response = server.submitTransaction(transaction);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgDialog.dismiss();
                                sendCompleteDialog();
                            }
                        });


                    } catch (Exception e) {
                        System.out.println("Something went wrong!");
                        System.out.println(e.getMessage());
                        // If the result is unknown (no response body, timeout etc.) we simply resubmit
                        // already built transaction:
                        // SubmitTransactionResponse response = server.submitTransaction(transaction);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();
    }

    View.OnClickListener Oklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCompleteDialog.dismiss();
            finish();
        }
    };

    private void sendCompleteDialog(){
        mCompleteDialog = new SendDialogComplete(mContext, mSendTotal, Oklistener);
        mCompleteDialog.setCancelable(false);
        mCompleteDialog.show();
    }

    View.OnClickListener PwCancellistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPwDialog.dismiss();
            sendPwDialog();
        }
    };

    private void sendPwDialog() {
        mPwDialog = new SendDialogPw(mContext,PwOklistener, PwCancellistener);
        mPwDialog.show();
    }

    private void sendConfirmDialog() {
        mConfirmDialog = new SendDialogConfirm(mContext, mDestion, mSendValue, mSendTotal, sendOklistener);
        mConfirmDialog.show();
    }


    public void getAddress(View view) {
        new IntentIntegrator(this).setCaptureActivity(SmallCaptureActivity.class)
                .setRequestCode(ADDRESS_REQUEST_QR_CODE).initiateScan();
    }

    private void changeButton(){
        if(mValidAddress && mValidAmmount ){
            mBtnSend.setBackgroundColor(getResources().getColor(R.color.cerulean));
            isNext =true;
        }else{
            mBtnSend.setBackgroundColor(getResources().getColor(R.color.pinkish_grey));
            isNext =false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == ADDRESS_REQUEST_CODE && resultCode == Constants.RssultCode.ADDRESS){
            String address = data.getStringExtra(Constants.Invoke.SEND);
            editPubkey.setText(address);
        } else{
            IntentResult result = IntentIntegrator.parseActivityResult( resultCode, data);

            if(result != null) {
                if(result.getContents() == null) {
                     Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();

                } else {

                    switch (requestCode){
                        case ADDRESS_REQUEST_QR_CODE:

                            editPubkey.setText(result.getContents());
                            break;

                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }


    }

    @Override
    public void onClick(View v) {
        Intent it;
        switch (v.getId()){
            case R.id.menu_trans_his:
                it = new Intent(SendActivity.this, WalletActivity.class);
                it.putExtra(Constants.Invoke.HISTORY,mWalletId);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;

            case R.id.menu_send:
                break;

            case R.id.menu_receive:
                it = new Intent(SendActivity.this, ReceiveActivity.class);
                it.putExtra(Constants.Invoke.WALLET, mWalletId);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;

            case R.id.menu_contact:
                it = new Intent(SendActivity.this, ContactActivity.class);
                it.putExtra(Constants.Invoke.SEND, mWalletId);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;
        }
    }
}
