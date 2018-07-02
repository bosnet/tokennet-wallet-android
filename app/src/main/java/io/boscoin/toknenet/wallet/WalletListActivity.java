package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.boscoin.toknenet.wallet.adapter.WalletListAdapter;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Account;
import io.boscoin.toknenet.wallet.model.Wallet;
import io.boscoin.toknenet.wallet.utils.WalletPreference;


public class WalletListActivity extends AppCompatActivity {

    private Wallet mWallet;
    private RecyclerView rv;
    private DbOpenHelper mDbOpenHelper;
    private List<Wallet> walletList;
    private Cursor mCursor;
    private Context mContext;
    private ImageButton mBtnSetting;
    private WalletListAdapter mAdapter;
    private static final int ORDER_REQUEST = 1;
    private static final int WALLET_DETAIL_VIEW = 2;
    private static final int SEND_REQUEST = 16;
    private ProgressDialog mProgDialog;
    private DbOpenHelper mDbOpenWalletHelper;
    private int mCount = 0;
    private long mWalletIdx;
    private static final int PORT_HTTP = 80;
    private static final int PORT_HTTPS = 443;
    private static final int MAX_WALLET = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_wallet_list);

        rv=(RecyclerView)findViewById(R.id.rv_walletlist);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        mBtnSetting = findViewById(R.id.btn_setting);
        mBtnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(WalletListActivity.this, SettingActivity.class);

                startActivityForResult(it, ORDER_REQUEST);
            }
        });

        findViewById(R.id.btn_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getWalletCount() > MAX_WALLET){
                    final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setMessage(R.string.a_walit_max).setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = alert.create();
                    dialog.show();
                }else{
                    Intent it = new Intent(WalletListActivity.this, ImportActivity.class);
                    startActivity(it);
                }

            }
        });

        findViewById(R.id.btn_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getWalletCount() > MAX_WALLET){
                    final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setMessage(R.string.a_walit_max).setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = alert.create();
                    dialog.show();
                }else{
                    Intent it = new Intent(WalletListActivity.this, CreateNoticeActivity.class);
                    startActivity(it);
                }

               
            }
        });
        
        initializeData();
        initializeAdapter();

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    showDialogWalt();
                    getBalances();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(dx == 0 && dy == 0){

                    showDialogWalt();
                    getBalances();
                }

            }
        });
    }

    private int getWalletCount(){
        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        int count = mDbOpenHelper.getWalletCount();
        return count;
    }

    private void getBalances(){
        int firstVisibleItemPosition = ((LinearLayoutManager)rv.getLayoutManager()).findFirstVisibleItemPosition();
        int lastVisibleItemPos = ((LinearLayoutManager)rv.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        int idx = firstVisibleItemPosition;
        mCount = idx;

        if(lastVisibleItemPos == 0 && mProgDialog.isShowing()){

            mProgDialog.dismiss();
            return;
        }

        for(; idx <= lastVisibleItemPos; idx++){
          Wallet wallet =  mAdapter.getWalletListItem(idx);

          getWalletBalances(wallet, idx, lastVisibleItemPos);

        }




    }



    private void getWalletBalances(final Wallet wallet , final int pos, final int lastPos){


        String pubKey = wallet.getWalletAccountId();




        AsyncHttpClient client = new AsyncHttpClient(true, PORT_HTTP,PORT_HTTPS);
        RequestParams params = new RequestParams();
        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(pubKey);

        client.get(String.valueOf(url),new TextHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Gson gson = new GsonBuilder().create();
                Account account =   gson.fromJson(res, Account.class);
                final String curBal = account.getBalances().get(0).getBalance();

                try{
                    mDbOpenWalletHelper = new DbOpenHelper(mContext);
                    mDbOpenWalletHelper.open(Constants.DB.MY_WALLETS);
                    mDbOpenWalletHelper.getmDB().acquireReference();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDbOpenWalletHelper.updateColumnWalletBalance(wallet.getWalletId(), curBal);

                        }
                    });

                }catch (Exception e){

                    e.printStackTrace();
                    mProgDialog.dismiss();
                }finally {
                    mDbOpenWalletHelper.close();
                    mDbOpenWalletHelper = null;


                    mCount++;


                    if(mCount > lastPos ){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mProgDialog.dismiss();

                                walletList.clear();
                                getWalletList();
                                mAdapter.setWalletList(walletList);
                                mCount = 0;
                            }
                        });


                    }


                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {


                mCount++;
                if(mCount > lastPos ){
                    mProgDialog.dismiss();
                    mAdapter.notifyItemRangeChanged(pos, lastPos);
                    mCount = 0;

                }
                mProgDialog.dismiss();

            }
        });
    }

    private void showDialogWalt(){
        mProgDialog = new ProgressDialog(mContext);
        mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgDialog.setMessage(getResources().getString(R.string.d_walit));

        mProgDialog.show();
    }

    private void initializeData() {

       getWalletList();

    }

    private void initializeAdapter(){
        mAdapter = new WalletListAdapter(walletList, new ClickListener() {
            @Override
            public void onSendClicked(int postion) {
                Intent it = new Intent(WalletListActivity.this, SendActivity.class);
                setSendWalletId(walletList.get(postion).getWalletId());
                it.putExtra(Constants.Invoke.SEND, walletList.get(postion).getWalletId());
                startActivityForResult(it, SEND_REQUEST);

            }

            @Override
            public void onReceivedClicked(int postion) {

                Intent it = new Intent(WalletListActivity.this, ReceiveActivity.class);
                it.putExtra(Constants.Invoke.WALLET, walletList.get(postion).getWalletId());
                startActivity(it);
            }

            @Override
            public void onItemClicked(int postion) {
                Intent it = new Intent(WalletListActivity.this, WalletActivity.class);
                it.putExtra(Constants.Invoke.HISTORY, walletList.get(postion).getWalletId());

                startActivityForResult(it, WALLET_DETAIL_VIEW);
            }
        });
        rv.setAdapter(mAdapter);
    }

    private void setSendWalletId(long id) {
        mWalletIdx = id;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == ORDER_REQUEST || requestCode == WALLET_DETAIL_VIEW ){

            walletList.clear();
            getWalletList();
            if(walletList.size() == 0){
                Intent it = new Intent(WalletListActivity.this, MainActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                finish();

            }

            mAdapter.setWalletList(walletList);

        } else if(requestCode == SEND_REQUEST && resultCode == Constants.RssultCode.SEND){

            Intent it = new Intent( WalletListActivity.this, WalletActivity.class);
            it.putExtra(Constants.Invoke.HISTORY, mWalletIdx);

            startActivityForResult(it, WALLET_DETAIL_VIEW);
        }
    }

    private void getWalletList() {
        walletList = new ArrayList<>();
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mCursor = null;


        mCursor = mDbOpenHelper.getColumnWalletByOrder("DESC");

        while (mCursor.moveToNext()){

            mWallet = new Wallet(
                    mCursor.getLong(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_ADDRESS)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET)),
                    mCursor.getInt(mCursor.getColumnIndex(Constants.DB.WALLET_ORDER)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_LASTEST)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_LAST_TIME))


            );

            walletList.add(mWallet);
        }
        mCursor.close();
        mDbOpenHelper.close();

    }

    public interface ClickListener {
        void onSendClicked(int postion);
        void onReceivedClicked(int postion);
        void onItemClicked(int postion);
    }
}
