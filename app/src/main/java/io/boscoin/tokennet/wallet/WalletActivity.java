package io.boscoin.tokennet.wallet;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import io.boscoin.tokennet.wallet.conf.Constants;
import io.boscoin.tokennet.wallet.db.DbOpenHelper;
import io.boscoin.tokennet.wallet.model.Account;
import io.boscoin.tokennet.wallet.model.Payments;
import io.boscoin.tokennet.wallet.utils.DetailDialog;
import io.boscoin.tokennet.wallet.utils.Utils;
import io.boscoin.tokennet.wallet.utils.WalletPreference;

public class WalletActivity extends AppCompatActivity implements
        AllHistoryFragment.OnListAllFragInteractionListener, SendHistoryFragment.OnListSendFragInteractionListener
        , ReceiveHistoryFragment.OnListReceiveFragInteractionListener, View.OnClickListener {


    private DbOpenHelper mDbOpenHelper;
    private DbOpenHelper mDbOpenWalletHelper;
    private TextView wName , wBalance, wPubKey;
    private String mMyPublicKey, mBosKey, mBal, mCurBal, mTime;
    private long mAccountId;
    private Context mContext;
    private long mIdx;
    private int mOrder;
    private View mAllLine, mSendLine, mReceiveLine;
    private LinearLayout mBtnAll, mBtnSend, mBtnReceive;
    private TextView mTvAll, mTvSend, mTvReceive;
    private RelativeLayout mNavHis, mNavSend, mNavReceive, mNavContact;
    private ImageView mIcHis, mIcSend, mIcReceive, mIcContact;
    private TextView navTvhis, navTvSend, navTvReceive, navTvContact;
    private static final int EDIT_REQUEST = 5;
    private static final int SEND_REQUEST = 15;
    private ProgressDialog mProgDialog;
    private Cursor mCursor;
    private boolean mIsGetBalanceProcess;
    private static final int PORT_HTTP = 80;
    private static final int PORT_HTTPS = 443;

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

        setContentView(R.layout.activity_wallet);

        Intent it = getIntent();

        mIdx = it.getLongExtra(Constants.Invoke.HISTORY,0);
 

        try{
            mDbOpenHelper = new DbOpenHelper(this);

            mDbOpenHelper.open(Constants.DB.MY_WALLETS);
            mCursor = mDbOpenHelper.getColumnWallet(mIdx);

            wName = findViewById(R.id.tv_wname);
            wName.setText(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)));

            wBalance = findViewById(R.id.tv_balances);

            mBal = mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_LASTEST));

            String tmp = Utils.fitDigit(mBal);
            String amount = tmp + " BOS";

            wBalance.setText(Utils.dispayBalance(amount));

            wPubKey = findViewById(R.id.tv_pub_key);
            mMyPublicKey = mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_ADDRESS));
            wPubKey.setText(Utils.contractionAddress(mMyPublicKey));

            mAccountId = mCursor.getLong(mCursor.getColumnIndex("_id"));

            mBosKey = mCursor.getString((mCursor.getColumnIndex((Constants.DB.WALLET_KET))));
            mTime = mCursor.getString((mCursor.getColumnIndex((Constants.DB.WALLET_LAST_TIME))));
            mOrder = mCursor.getInt(mCursor.getColumnIndex((Constants.DB.WALLET_ORDER)));

        }catch (Exception e){

            e.printStackTrace();
        } finally {
            mDbOpenHelper.close();
            mDbOpenHelper = null;

            mCursor.close();
            mCursor = null;
        }



        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAllLine = findViewById(R.id.line_all);
        mSendLine = findViewById(R.id.line_send);
        mReceiveLine = findViewById(R.id.line_receive);

        mTvAll = findViewById(R.id.txt_all);
        mTvReceive = findViewById(R.id.txt_receive);
        mTvSend = findViewById(R.id.txt_send);

        setNavBottom();

        viewHistoryAll();


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
        mIcHis.setBackgroundResource(R.drawable.ic_icon_history_normal);
        mIcSend.setBackgroundResource(R.drawable.ic_icon_send_disable);
        mIcReceive.setBackgroundResource(R.drawable.ic_icon_recieve_disable);
        mIcContact.setBackgroundResource(R.drawable.ic_icon_contacts_disable);

        navTvhis = findViewById(R.id.nav_his);
        navTvSend = findViewById(R.id.nav_send);
        navTvReceive = findViewById(R.id.nav_receive);
        navTvContact = findViewById(R.id.nav_contact);

        navTvhis.setTextColor(getResources().getColor(R.color.cerulean));
        navTvSend.setTextColor(getResources().getColor(R.color.brownish_grey));
        navTvReceive.setTextColor(getResources().getColor(R.color.brownish_grey));
        navTvContact.setTextColor(getResources().getColor(R.color.brownish_grey));

    }

    private void getWalletBalances() {
        if(mIsGetBalanceProcess == true){
            if(mProgDialog.isShowing()){
                mProgDialog.dismiss();
            }
            return;
        }
        mIsGetBalanceProcess = true;

        AsyncHttpClient client = new AsyncHttpClient(true, PORT_HTTP,PORT_HTTPS);
        RequestParams params = new RequestParams();
        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(mMyPublicKey);

        client.get(String.valueOf(url),new TextHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Gson gson = new GsonBuilder().create();
                Account account =   gson.fromJson(res, Account.class);
                mCurBal = account.getBalances().get(0).getBalance()+" BOS";

                final String val =  mCurBal.replaceAll(" BOS", "");

                String tmp = Utils.fitDigit(val);
                String amount = tmp+" BOS";


                wBalance.setText(Utils.dispayBalance(amount));

                try{

                   DbOpenHelper.updateColumnWalletBalance(mContext,mAccountId,val);

                }catch (Exception e){

                    e.printStackTrace();

                    mIsGetBalanceProcess = false;
                    if(mProgDialog != null){
                        mProgDialog.dismiss();
                    }
                }finally {

                    if(mProgDialog != null){
                        mProgDialog.dismiss();
                    }

                    mIsGetBalanceProcess = false;

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                if(mProgDialog != null){
                    mProgDialog.dismiss();
                }
                if(statusCode == Constants.Status.NOT_FOUND){
                    wBalance.setText(" 0 BOS");
                    viewEmptyWallet();
                }else{
                    Toast.makeText(mContext, mContext.getString(R.string.error_create_wallet), Toast.LENGTH_SHORT).show();
                }
                mIsGetBalanceProcess = false;
            }
        });
    }

    private void viewHistoryAll(){
        AllHistoryFragment allf = new AllHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Invoke.PUBKEY, mMyPublicKey);
        allf.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,allf).commit();
    }

    private void viewEmptyWallet(){

        EmptyWalletFragment fragAll = new EmptyWalletFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Invoke.WALLET, mAccountId);
        fragAll.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,fragAll).commit();

    }

    public void viewHistoryAll(View view) {


        AllHistoryFragment fragAll = new AllHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Invoke.PUBKEY, mMyPublicKey);
        fragAll.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,fragAll).commit();

        mAllLine.setVisibility(View.VISIBLE);
        mSendLine.setVisibility(View.INVISIBLE);
        mReceiveLine.setVisibility(View.INVISIBLE);
        mTvAll.setTextColor(getResources().getColor(R.color.white));
        mTvSend.setTextColor(getResources().getColor(R.color.white_op50));
        mTvReceive.setTextColor(getResources().getColor(R.color.white_op50));

    }

    public void viewReceiveHistory(View view) {

        ReceiveHistoryFragment fragReceive = new ReceiveHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Invoke.PUBKEY, mMyPublicKey);
        fragReceive.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,fragReceive).commit();

        mAllLine.setVisibility(View.INVISIBLE);
        mSendLine.setVisibility(View.INVISIBLE);
        mReceiveLine.setVisibility(View.VISIBLE);
        mTvAll.setTextColor(getResources().getColor(R.color.white_op50));
        mTvSend.setTextColor(getResources().getColor(R.color.white_op50));
        mTvReceive.setTextColor(getResources().getColor(R.color.white));
    }

    public void viewSendHistory(View view) {


        SendHistoryFragment fragSend = new SendHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Invoke.PUBKEY, mMyPublicKey);
        fragSend.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,fragSend).commit();

        mAllLine.setVisibility(View.INVISIBLE);
        mSendLine.setVisibility(View.VISIBLE);
        mReceiveLine.setVisibility(View.INVISIBLE);
        mTvAll.setTextColor(getResources().getColor(R.color.white_op50));
        mTvSend.setTextColor(getResources().getColor(R.color.white));
        mTvReceive.setTextColor(getResources().getColor(R.color.white_op50));
    }


    public void addressCopy(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("address", mMyPublicKey);
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(mContext, mContext.getString(R.string.toast_text_clipboard_address), Toast.LENGTH_SHORT).show();
    }









    @Override
    public void onClick(View v) {
        Intent it;
        switch (v.getId()){
            case R.id.menu_trans_his:
                break;

            case R.id.menu_send:
                it = new Intent(WalletActivity.this, SendActivity.class);
                it.putExtra(Constants.Invoke.SEND, mAccountId);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivityForResult(it, SEND_REQUEST);
                break;

            case R.id.menu_receive:
                it = new Intent(WalletActivity.this, ReceiveActivity.class);
                it.putExtra(Constants.Invoke.WALLET, mAccountId);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;

            case R.id.menu_contact:
                it = new Intent(WalletActivity.this, ContactActivity.class);
                it.putExtra(Constants.Invoke.ADDRESS_BOOK, mAccountId);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;
        }
    }

    public void editWallet(View view) {
        Intent it = new Intent(WalletActivity.this, EditWalletActivity.class);
        it.putExtra(Constants.Invoke.EDIT, mIdx);
        startActivityForResult(it,EDIT_REQUEST);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EDIT_REQUEST && resultCode == Constants.ResultCode.CHANGE_NAME){
            checkWallet();
        } else if(requestCode == EDIT_REQUEST && resultCode == Constants.ResultCode.DELETE_WALLET){
            finish();
        } else if(requestCode == SEND_REQUEST){

            getWalletBalances();
            getRecentHistory();

        } else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void getRecentHistory() {
        AllHistoryFragment fragAll = new AllHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Invoke.PUBKEY, mMyPublicKey);
        fragAll.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,fragAll).commitAllowingStateLoss();

        mAllLine.setVisibility(View.VISIBLE);
        mSendLine.setVisibility(View.INVISIBLE);
        mReceiveLine.setVisibility(View.INVISIBLE);
        mTvAll.setTextColor(getResources().getColor(R.color.white));
        mTvSend.setTextColor(getResources().getColor(R.color.white_op50));
        mTvReceive.setTextColor(getResources().getColor(R.color.white_op50));
    }

    private void checkWallet() {


        try{
            mDbOpenHelper = new DbOpenHelper(this);
            mDbOpenHelper.open(Constants.DB.MY_WALLETS);
            mCursor = mDbOpenHelper.getColumnWallet(mIdx);
            wName = findViewById(R.id.tv_wname);
            wName.setText(mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)));

        }catch (Exception e){

            e.printStackTrace();
        }finally {
            mDbOpenHelper.close();
            mCursor.close();
            mCursor = null;
            mDbOpenHelper = null;
        }

    }

    @Override
    public void ListAllFragInteraction(Payments.PayRecords item) {
        DetailDialog dialog = new DetailDialog(mContext,item,mMyPublicKey);
        dialog.show();
    }

    @Override
    public void getCurrentBalanceAll() {


        if(mProgDialog != null && mProgDialog.isShowing()){
            mProgDialog.dismiss();
            mIsGetBalanceProcess = false;
        }
        mProgDialog = new ProgressDialog(mContext);
        mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgDialog.setMessage(getResources().getString(R.string.d_walit));
        mProgDialog.setCancelable(false);
        mProgDialog.show();
        getWalletBalances();
    }

    @Override
    public void ListSendFragInteraction(Payments.PayRecords item) {
        DetailDialog dialog = new DetailDialog(mContext,item,mMyPublicKey);
        dialog.show();
    }

    @Override
    public void getCurrentBalanceSend() {
        if(mProgDialog != null && mProgDialog.isShowing()){
            mProgDialog.dismiss();
            mIsGetBalanceProcess = false;
        }
        mProgDialog = new ProgressDialog(mContext);
        mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgDialog.setMessage(getResources().getString(R.string.d_walit));
        mProgDialog.setCancelable(false);
        mProgDialog.show();
        getWalletBalances();
    }

    @Override
    public void ListReceiveFragInteraction(Payments.PayRecords item) {
        DetailDialog dialog = new DetailDialog(mContext,item,mMyPublicKey);
        dialog.show();
    }

    @Override
    public void getCurrentBalanceReceive() {
        if(mProgDialog != null && mProgDialog.isShowing()){
            mProgDialog.dismiss();
            mIsGetBalanceProcess = false;
        }
        mProgDialog = new ProgressDialog(mContext);
        mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgDialog.setMessage(getResources().getString(R.string.d_walit));
        mProgDialog.setCancelable(false);
        mProgDialog.show();
        getWalletBalances();

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDbOpenHelper != null){
            mDbOpenHelper.close();
            mDbOpenHelper = null;
        }
        if(mCursor != null){
            mCursor.close();
            mCursor = null;
        }

    }
}
