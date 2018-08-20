package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.TextInputLayout;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.math.BigDecimal;
import java.security.GeneralSecurityException;

import cz.msebera.android.httpclient.Header;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Account;
import io.boscoin.toknenet.wallet.model.Payments;
import io.boscoin.toknenet.wallet.utils.SendDialogComplete;
import io.boscoin.toknenet.wallet.utils.SendDialogConfirm;
import io.boscoin.toknenet.wallet.utils.SendDialogFail;
import io.boscoin.toknenet.wallet.utils.SendDialogPw;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class SendActivity extends AppCompatActivity implements View.OnClickListener{


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
    private SendDialogFail mFailDialog;

    private static final String SEND_FEE = "0.001";
    private static final double MIN_BALANCE = 0.1;
    private KeyPair keyPair;
    private ProgressDialog mProgDialog;
    private String mCurBal, mAmount, mMyPubKey;
    private static final String BALANCE_FAIL = "op_underfunded";
    private static final int ADD = 1;
    private static final int SUB = 2;
    private Payments mPayments;
    private TextInputLayout mAmountInputly;
    private static final int PORT_HTTP = 80;
    private static final int PORT_HTTPS = 443;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mContext = this;

        setLanguage(mContext);

        initUI();
    }

    private void setLanguage(Context context) {
        String lang = WalletPreference.getWalletLanguage(context);
        Utils.changeLanguage(context,lang);
    }

    private void getWalletResource() {
        Intent it = getIntent();
        mWalletId = it.getLongExtra(Constants.Invoke.SEND, 0);
        mPubKey = it.getStringExtra(Constants.Invoke.PUBKEY);

        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mCursor = mDbOpenHelper.getColumnWallet(mWalletId);

        mBosKey = mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET));
        mMyPubKey = mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_ADDRESS));

        mDbOpenHelper.close();
        mCursor.close();
    }


    private void initUI() {
        setContentView(R.layout.activity_send);

        getWalletResource();

        editPubkey = findViewById(R.id.input_address);
        editPubkey.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        editPubkey.setRawInputType(InputType.TYPE_CLASS_TEXT);



        editAmmount = findViewById(R.id.input_ammount);
        mImgDel = findViewById(R.id.del_address);
        mTvAddressErr = findViewById(R.id.err_pubkey);
        mBtnSend = findViewById(R.id.btn_send2);

        mAmountInputly = findViewById(R.id.amount_inputlayout);

        findViewById(R.id.btn_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getAddressCount() > 0 ){
                    Intent it = new Intent(SendActivity.this, ContactActivity.class);
                    it.putExtra(Constants.Invoke.SEND, true);
                    it.putExtra(Constants.Invoke.ADDRESS_BOOK, mWalletId);
                    startActivityForResult(it,ADDRESS_REQUEST_CODE );
                }else{
                    final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setMessage(R.string.error_no_count_address).setPositiveButton(R.string.ok,
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

                if(TextUtils.isEmpty(mPubKey)){

                    mImgDel.setVisibility(View.GONE);
                    mTvAddressErr.setText(R.string.enter_pub_address);
                    mTvAddressErr.setVisibility(View.VISIBLE);
                    mValidAddress = false;
                }else{
                    if(mMyPubKey.equals(mPubKey)){
                        mValidAddress = false;
                        Toast.makeText(mContext,R.string.error_same_send,Toast.LENGTH_SHORT).show();
                        mValidAddress = false;
                        changeButton();
                        return;
                    }

                    mImgDel.setVisibility(View.VISIBLE);
                    try{
                        Utils.decodeCheck(Utils.VersionByte.ACCOUNT_ID, mPubKey.toCharArray());
                        mTvAddressErr.setVisibility(View.GONE);

                        mValidAddress = true;

                    }catch (Exception e){
                        mTvAddressErr.setText(R.string.error_invalid_pubkey);
                        mTvAddressErr.setVisibility(View.VISIBLE);

                        mValidAddress = false;
                    }
                }
                changeButton();

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

                String input = s.toString();

                if(input.contains(".") && s.charAt(s.length()-1) != '.'){

                    if(input.indexOf(".") + 8 <= input.length()-1){

                        String formatted = input.substring(0, input.indexOf(".") + 8);
                        editAmmount.setText(formatted);
                        editAmmount.setSelection(formatted.length());
                    }
                }else if(input.contains(",") && s.charAt(s.length()-1) != ','){
                    if(input.indexOf(",") + 8 <= input.length()-1){
                        String formatted = input.substring(0, input.indexOf(",") + 8);
                        editAmmount.setText(formatted);
                        editAmmount.setSelection(formatted.length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                mAmount = s.toString();

                if(mAmount.startsWith("0") || mAmount.startsWith(".")){
                    mValidAmmount = false;
                }else if(!TextUtils.isEmpty(mAmount)){
                    mValidAmmount = true;
                }else{
                    mValidAmmount = false;
                }
                changeButton();
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

        return count;
    }

    public void showSendBos(View view) {

        if(isNext){
            showPleaseWait();
            getWalletBalances();
        }


    }

    private void showPleaseWait() {

        if(mProgDialog != null){
            mProgDialog.dismiss();
        }

        mProgDialog = new ProgressDialog(mContext);
        mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgDialog.setMessage(getResources().getString(R.string.d_walit));
        mProgDialog.setCancelable(false);
        mProgDialog.show();
    }

    private void getWalletBalances() {

        AsyncHttpClient client = new AsyncHttpClient(true, PORT_HTTP,PORT_HTTPS);
        RequestParams params = new RequestParams();
        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(mMyPubKey);

        client.get(String.valueOf(url),new TextHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Gson gson = new GsonBuilder().create();
                Account account =   gson.fromJson(res, Account.class);
                mCurBal = account.getBalances().get(0).getBalance();


                try{
                    mDbOpenHelper = new DbOpenHelper(mContext);
                    mDbOpenHelper.open(Constants.DB.MY_WALLETS);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDbOpenHelper.updateColumnWalletBalance(mWalletId, mCurBal);

                        }
                    });

                }catch (Exception e){

                    e.printStackTrace();
                }finally {
                    mDbOpenHelper.close();
                    if(mProgDialog != null){
                        mProgDialog.dismiss();
                    }
                }

                mSendValue = editAmmount.getText().toString();



                BigDecimal sendAmount = null;
                BigDecimal curAmount = null;
                try {
                    sendAmount = Utils.MoneyCalcualtion(mSendValue , SEND_FEE, ADD);
                    curAmount = new BigDecimal(mCurBal);
                    mSendTotal = sendAmount.toString();



                    double sendMoney = Double.parseDouble(sendAmount.toString());
                    double curMoney = Double.parseDouble(mCurBal);

                    if(sendMoney >= curMoney){
                        alertDialogFunds();
                        isNext = false;
                        mValidAmmount = false;
                        changeButton();
                    } else {
                        BigDecimal tmp = Utils.MoneyCalcualtion(curAmount,sendAmount,SUB);
                        String tmp2 = tmp.toString();
                        double result = Double.parseDouble(tmp2);

                        if(result < MIN_BALANCE){
                            alertDialogSend();
                            isNext = false;
                            mValidAmmount = false;
                            changeButton();
                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    isNext = false;
                }

                if(isNext){
                    mDestion = editPubkey.getText().toString();

                    sendConfirmDialog();
                }



            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                if(mProgDialog != null){
                    mProgDialog.dismiss();
                }
                if(statusCode == Constants.Status.NOT_FOUND){
                    alertDialogAccount();
                }
                mValidAddress = false;

            }
        });
    }

    private void alertDialogSend() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setMessage(R.string.error_no_send).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void alertDialogFunds() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setMessage(R.string.error_no_funds).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void alertDialogAccount() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setMessage(R.string.error_no_account).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    View.OnClickListener sendOklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConfirmDialog.dismiss();
            sendPwDialog();
        }
    };



    private void isValidPw(EditText pw, TextView err) {
        TextView tvErrView = err;
        String seedkey = pw.getText().toString();

        if(TextUtils.isEmpty(seedkey)){
            tvErrView.setText(R.string.error_no_pw);
            tvErrView.setVisibility(View.VISIBLE);
            return;
        }

        String tmp = mBosKey.substring(3);
        String tmp2 = tmp.substring(0,tmp.length()-2);

        try {
            String dec =  AESCrypt.decrypt(seedkey,tmp2);
            keyPair = KeyPair.fromSecretSeed(dec);
            mSeed = new String(keyPair.getSecretSeed());

            tvErrView.setVisibility(View.GONE);
            mPwDialog.dismiss();
            mProgDialog = new ProgressDialog(mContext);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgDialog.setMessage(getResources().getString(R.string.d_walit));
            mProgDialog.setCancelable(false);
            mProgDialog.show();
            sendBosTransation();
        } catch (GeneralSecurityException e) {

            tvErrView.setText(R.string.error_invalid_password);
            tvErrView.setVisibility(View.VISIBLE);
        }
    }

    private void sendBosTransation() {

        AsyncHttpClient client = new AsyncHttpClient(true, PORT_HTTP,PORT_HTTPS);
        RequestParams params = new RequestParams();
        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(mDestion);

        client.get(String.valueOf(url),new TextHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {

                sendBOS();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                createAccount();

            }
        });
    }

    private void createAccount() {
        new Thread(){
            public void run(){


                Network.use(new Network(BuildConfig.NETWORK_PH));
                Server server = new Server(Constants.Domain.BOS_HORIZON_TEST);

                KeyPair source = KeyPair.fromSecretSeed(mSeed);
                KeyPair destination = KeyPair.fromAccountId(mDestion);

                // If there was no error, load up-to-date information on your account.
                AccountResponse sourceAccount = null;
                try {
                    sourceAccount = server.accounts().account(source);


                    // Start building the transaction.
                    Transaction transaction = new Transaction.Builder(sourceAccount)
                            .addOperation(new CreateAccountOperation.Builder(destination, mSendValue/*"0.002"*/).build())
                            .build();
                    // Sign the transaction to prove you are actually the person sending it.
                    transaction.sign(source);

                    // And finally, send it off to Stellar!
                    try {
                        SubmitTransactionResponse response = server.submitTransaction(transaction);



                        SubmitTransactionResponse.Extras extras = response.getExtras();
                        if(extras == null){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgDialog.dismiss();
                                    sendCompleteDialog();
                                }
                            });
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgDialog.dismiss();
                                    sendFailDialog();
                                }
                            });
                        }



                    } catch (Exception e) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgDialog.dismiss();
                                sendFailDialog();
                            }
                        });

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgDialog.dismiss();
                            sendFailDialog();
                        }
                    });
                }


            }
        }.start();
    }

    private void sendBOS() {
        new Thread(){
            public void run(){


                Network.use(new Network(BuildConfig.NETWORK_PH));
                Server server = new Server(Constants.Domain.BOS_HORIZON_TEST);

                KeyPair source = KeyPair.fromSecretSeed(mSeed);
                KeyPair destination = KeyPair.fromAccountId(mDestion);

                AccountResponse sourceAccount = null;
                try {
                    sourceAccount = server.accounts().account(source);


                    // Start building the transaction.
                    Transaction transaction = new Transaction.Builder(sourceAccount)
                            .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), mSendValue).build())
                            .build();

                    // Sign the transaction to prove you are actually the person sending it.
                    transaction.sign(source);

                    // And finally, send it off to Stellar!
                    try {
                        SubmitTransactionResponse response = server.submitTransaction(transaction);
                        Utils.printResponse(response);

                        SubmitTransactionResponse.Extras extras = response.getExtras();

                        if(extras == null){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    updateTransTime();
                                }
                            });
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgDialog.dismiss();
                                    sendFailDialog();
                                }
                            });
                        }



                    } catch (Exception e) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgDialog.dismiss();
                                sendFailDialog();
                            }
                        });
                        // If the result is unknown (no response body, timeout etc.) we simply resubmit
                        // already built transaction:
                        // SubmitTransactionResponse response = server.submitTransaction(transaction);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgDialog.dismiss();
                            sendFailDialog();
                        }
                    });
                }


            }
        }.start();
    }

    private void updateTransTime(){

        AsyncHttpClient client = new AsyncHttpClient(true, PORT_HTTP,PORT_HTTPS);
        RequestParams params = new RequestParams();

        params.put(Constants.Params.LIMIT, "1");
        params.put(Constants.Params.ORDER, Constants.Params.DESC);

        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(mMyPubKey);
        url.append("/");
        url.append(Constants.Params.PAYMENTS);


        client.get(String.valueOf(url),params,new TextHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Gson gson = new GsonBuilder().create();
                mPayments =  gson.fromJson(res, Payments.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mPayments.get_embedded().getRecordList().size() > 0){
                            String time = Utils.convertUtcToLocal(mPayments.get_embedded().getRecordList().get(0).getCreated_at());
                            mDbOpenHelper = new DbOpenHelper(mContext);
                            mDbOpenHelper.open(Constants.DB.MY_WALLETS);
                            mDbOpenHelper.updateColumnWalletTransTime(mWalletId,time);
                            mDbOpenHelper.close();
                        }

                        mProgDialog.dismiss();
                        sendCompleteDialog();
                    }
                });

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgDialog.dismiss();
                        sendFailDialog();
                    }
                });
            }


        });
    }

    View.OnClickListener Okfaillistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFailDialog.dismiss();
            finish();
        }
    };

    private void sendFailDialog() {
        mFailDialog = new SendDialogFail(mContext, Okfaillistener);
        mFailDialog.setCancelable(false);
        mFailDialog.show();
    }

    View.OnClickListener Oklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCompleteDialog.dismiss();
            setResult(Constants.ResultCode.SEND);
            finish();
        }
    };

    private void sendCompleteDialog(){
        mCompleteDialog = new SendDialogComplete(mContext, mSendTotal, Oklistener);
        mCompleteDialog.setCancelable(false);
        mCompleteDialog.show();
    }

    View.OnClickListener PwOklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            EditText inputPw = mPwDialog.getEditPw();
            TextView tvErrKey = mPwDialog.getmTvErrKey();

            isValidPw(inputPw, tvErrKey);

        }
    };

    View.OnClickListener PwCancellistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPwDialog.dismiss();

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

        if(requestCode == ADDRESS_REQUEST_CODE && resultCode == Constants.ResultCode.ADDRESS){
            String address = data.getStringExtra(Constants.Invoke.SEND);
            editPubkey.setText(address);
        } else{
            IntentResult result = IntentIntegrator.parseActivityResult( resultCode, data);

            if(result != null) {
                if(result.getContents() == null) {


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

                it.putExtra(Constants.Invoke.ADDRESS_BOOK, mWalletId);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;
        }
    }
}
