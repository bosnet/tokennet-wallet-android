package io.boscoin.toknenet.wallet;

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
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.dummy.DummyContent;
import io.boscoin.toknenet.wallet.model.Account;
import io.boscoin.toknenet.wallet.model.Payments;
import io.boscoin.toknenet.wallet.utils.Utils;

public class WalletActivity extends AppCompatActivity implements
        AllHistoryFragment.OnListAllFragInteractionListener,AllHistoryFragment.CurrentBalanceListener,
        PaymentFragment.OnListPayFragInteractionListener, ReceiptFragment.OnListReceiptFragInteractionListener, View.OnClickListener {

    private static final String TAG = "HistoryActivity";
    private DbOpenHelper mDbOpenHelper;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallet);

        mContext = this;

        initUI();

    }


    private void initUI() {
        Intent it = getIntent();

        mIdx = it.getLongExtra(Constants.Invoke.HISTORY,0);
        Log.e(TAG,"mIdx = "+mIdx);
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        Cursor cursor = mDbOpenHelper.getColumnWallet(mIdx);

        wName = findViewById(R.id.tv_wname);
        wName.setText(cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_NAME)));

        wBalance = findViewById(R.id.tv_balances);
        mBal = cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_LASTEST));

        wBalance.setText(Utils.dispayBalance(mBal));

        wPubKey = findViewById(R.id.tv_pub_key);
        mMyPublicKey = cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_ADDRESS));
        wPubKey.setText(Utils.contractionAddress(mMyPublicKey));

        mAccountId = cursor.getLong(cursor.getColumnIndex("_id"));

        mBosKey = cursor.getString((cursor.getColumnIndex((Constants.DB.WALLET_KET))));
        mTime = cursor.getString((cursor.getColumnIndex((Constants.DB.WALLET_LAST_TIME))));
        mOrder = cursor.getInt(cursor.getColumnIndex((Constants.DB.WALLET_ORDER)));

        AllHistoryFragment allf = new AllHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Invoke.PUBKEY, mMyPublicKey);
        allf.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,allf).commit();

        mDbOpenHelper.close();
        cursor.close();

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
        AsyncHttpClient client = new AsyncHttpClient();
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

                String val =  mCurBal.replaceAll(" BOS", "");

                wBalance.setText(Utils.dispayBalance(mCurBal));

                mDbOpenHelper = new DbOpenHelper(mContext);
                mDbOpenHelper.open(Constants.DB.MY_WALLETS);
                mDbOpenHelper.updateColumnWalletBalance(mAccountId, val);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG,"resp = "+responseString);
                Log.e(TAG,"status = "+statusCode);
                if(statusCode == Constants.Status.NOT_FOUND){
                    wBalance.setText(" 0 BOS");
                }else{
                    Toast.makeText(mContext, mContext.getString(R.string.error_create_wallet), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    public void viewHistoryAll(View view) {
        AllHistoryFragment allf = new AllHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Invoke.PUBKEY, mMyPublicKey);
        allf.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,allf).commit();
        mAllLine.setVisibility(View.VISIBLE);
        mSendLine.setVisibility(View.INVISIBLE);
        mReceiveLine.setVisibility(View.INVISIBLE);
        mTvAll.setTextColor(getResources().getColor(R.color.white));
        mTvSend.setTextColor(getResources().getColor(R.color.white_op50));
        mTvReceive.setTextColor(getResources().getColor(R.color.white_op50));

    }

    public void viewReceiveHistory(View view) {
        ReceiptFragment recipf = new ReceiptFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,recipf).commit();
        mAllLine.setVisibility(View.INVISIBLE);
        mSendLine.setVisibility(View.INVISIBLE);
        mReceiveLine.setVisibility(View.VISIBLE);
        mTvAll.setTextColor(getResources().getColor(R.color.white_op50));
        mTvSend.setTextColor(getResources().getColor(R.color.white_op50));
        mTvReceive.setTextColor(getResources().getColor(R.color.white));
    }

    public void viewSendHistory(View view) {
        PaymentFragment payf = new PaymentFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,payf).commit();
        mAllLine.setVisibility(View.INVISIBLE);
        mSendLine.setVisibility(View.VISIBLE);
        mReceiveLine.setVisibility(View.INVISIBLE);
        mTvAll.setTextColor(getResources().getColor(R.color.white_op50));
        mTvSend.setTextColor(getResources().getColor(R.color.white));
        mTvReceive.setTextColor(getResources().getColor(R.color.white_op50));
    }

    public void goHistoryView(View view) {
        return;
    }

    public void goSendView(View view) {
        Intent it = new Intent(WalletActivity.this, SendActivity.class);
        it.putExtra(Constants.Invoke.SEND,mAccountId);
        startActivity(it);
    }

    public void goReceiveView(View view) {

        Intent it = new Intent(WalletActivity.this, ReceiveActivity.class);
        it.putExtra(Constants.Invoke.WALLET, mIdx);
        startActivity(it);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    public void goAddressView(View view) {
        Toast.makeText(mContext, "Next Step", Toast.LENGTH_SHORT).show();

    }

    public void addressCopy(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("address", mMyPublicKey);
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(mContext, mContext.getString(R.string.toast_text_clipboard_address), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void ListAllFragInteraction(Payments.PayRecords item) {

    }

    @Override
    public void getCurrentBalanceAll() {
        getWalletBalances();
    }

    @Override
    public void ListPayFragInteraction(DummyContent.DummyItem item) {

    }


    @Override
    public void ListReceiptFragInteraction(DummyContent.DummyItem item) {

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
                startActivity(it);
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
        //startActivity(it);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EDIT_REQUEST && resultCode == Constants.RssultCode.CHANGE_NAME){
            checkWallet();
        } else if(requestCode == EDIT_REQUEST && resultCode == Constants.RssultCode.DELETE_WALLET){
            finish();
        } else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void checkWallet() {
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        Cursor cursor = mDbOpenHelper.getColumnWallet(mIdx);
        wName = findViewById(R.id.tv_wname);
        wName.setText(cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_NAME)));
        mDbOpenHelper.close();
        cursor.close();
    }
}
