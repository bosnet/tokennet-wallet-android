package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import io.boscoin.toknenet.wallet.adapter.WalletListAdapter;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Account;
import io.boscoin.toknenet.wallet.model.Wallet;
import io.boscoin.toknenet.wallet.utils.Utils;
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

    private static final int SETTING_REQUEST = 1;
    private static final int WALLET_DETAIL_VIEW = 2;
    private static final int SEND_REQUEST = 16;
    private ProgressDialog mProgDialog;
    private DbOpenHelper mDbOpenWalletHelper;
    private static int mCount = 0, mLastCount = 0;
    private static int mRepCount = 0, mStartIdx;
    private long mWalletIdx;
    private static final int PORT_HTTP = 80;
    private static final int PORT_HTTPS = 443;
    private static final int MAX_WALLET = 100;
    private int mMaxWallcount;
    private boolean isUp, isDown;
    private SwipeRefreshLayout mListSwipeRefresh;
    private int ADD_COUNT = 10;


    private BroadcastReceiver changeLanguageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String lang = WalletPreference.getWalletLanguage(mContext);

            Utils.changeLanguage(mContext, lang);

            Intent it = new Intent(WalletListActivity.this , WalletListActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(it);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setLanguage();


        initUI();


        registerChangeLangdReceiver();
    }

    private void setLanguage() {
        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);
    }

    private void initUI() {

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

                startActivityForResult(it, SETTING_REQUEST);
            }
        });

        findViewById(R.id.btn_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getWalletCount() >= MAX_WALLET){
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
                if(getWalletCount() >= MAX_WALLET){
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

                    getBalances();
                } 

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(dy < 0){
                    isUp = true;
                    isDown = false;
                } else if(dy > 0 ){
                    isUp = false;
                    isDown = true;
                }
                

            }
        });

        mListSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.wlistswiperefresh);
        mListSwipeRefresh.setColorSchemeResources(R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);

        mListSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               mListSwipeRefresh.setRefreshing(false);
               getResumeBalances();
            }
        });
    }

    private int getWalletCount(){
        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mMaxWallcount = mDbOpenHelper.getWalletCount();
        return mMaxWallcount;
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
                    
                    DbOpenHelper.updateColumnWalletBalance(mContext,wallet.getWalletId(), curBal);
                }catch (Exception e){



                    dismissDialog();
                }finally {


                    mStartIdx++;


                    if( mStartIdx >= lastPos ){

                        walletList.clear();
                        getWalletList();
                        mAdapter.setWalletList(walletList);
                        mCount = 0;

                        mStartIdx =0;

                        dismissDialog();
                        


                    }

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                mStartIdx++;
                if(  mStartIdx >= lastPos ){

                    mAdapter.notifyItemRangeChanged(pos, lastPos);
                    mCount = 0;

                    mStartIdx = 0;

                }

                dismissDialog();

            }
        });
    }

    private void showDialogWalt(){


        if(mProgDialog == null){
            mProgDialog = new ProgressDialog(mContext);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgDialog.setMessage(getResources().getString(R.string.d_walit));
            mProgDialog.setCancelable(true);
            mProgDialog.show();
        }


    }

    private void dismissDialog(){
        if(mProgDialog != null){
            mProgDialog.dismiss();
            mProgDialog = null;
        }
    }

    private void initializeData() {

       getWalletList();

    }

    @Override
    protected void onResume() {
        super.onResume();


        getResumeBalances();


    }

    private void getResumeBalances() {
        //int ADD_COUNT = 10;
        int start = 0;
        getWalletCount();


        mCount = 0;
        int idx = mCount;
        if(mCount == 0 ){
            mLastCount = ADD_COUNT;

            if(mLastCount <= mMaxWallcount -1){
                showDialogWalt();
                for(;  idx<= mLastCount; idx++){
                    Wallet wallet =  mAdapter.getWalletListItem(idx);

                    getWalletBalances(wallet, idx, mLastCount);

                }
            }else{
                mLastCount = mMaxWallcount -1;
                showDialogWalt();
                for(; mCount<= mLastCount; mCount++){
                    Wallet wallet =  mAdapter.getWalletListItem( idx);

                    getWalletBalances(wallet, mCount, mLastCount);

                }
            }
        }
    }

    private void getInitBalances() {


        int itemTotalCount = rv.getAdapter().getItemCount();


        int firstVisibleItemPosition = ((LinearLayoutManager)rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }

    private void getBalances(){



        int firstVisibleItemPosition = ((LinearLayoutManager)rv.getLayoutManager()).findFirstVisibleItemPosition();
        int lastVisibleItemPos = ((LinearLayoutManager)rv.getLayoutManager()).findLastVisibleItemPosition();



        if(isUp){

            if(firstVisibleItemPosition <= (mLastCount -ADD_COUNT) ){


                mCount = firstVisibleItemPosition -ADD_COUNT;

                mLastCount = mCount + ADD_COUNT;

                if(mCount < 0){
                    dismissDialog();

                    return;
                }  


                showDialogWalt();
                mStartIdx = mCount;
                for(; mCount < mLastCount; mCount++){
                    Wallet wallet =  mAdapter.getWalletListItem(mCount);

                    getWalletBalances(wallet, mStartIdx, mLastCount);


                }

            }
        }else{

            if(firstVisibleItemPosition >= mLastCount){

                showDialogWalt();
                mCount = firstVisibleItemPosition;

                mLastCount = mCount + ADD_COUNT;

                if(mLastCount >= mMaxWallcount){
                    mLastCount = mMaxWallcount -1;
                }

                mStartIdx = mCount;
                for(; mCount < mLastCount; mCount++){

                    Wallet wallet =  mAdapter.getWalletListItem(mCount);

                    getWalletBalances(wallet, mStartIdx, mLastCount);


                }

            } 
        }
        





       




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

        if(requestCode == SETTING_REQUEST || requestCode == WALLET_DETAIL_VIEW ){

                walletList.clear();
                getWalletList();
                if(walletList.size() == 0){
                    Intent it = new Intent(WalletListActivity.this, MainActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(it);
                    finish();

                }

                mAdapter.setWalletList(walletList);



        } else if(requestCode == SEND_REQUEST && resultCode == Constants.ResultCode.SEND){

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

    @Override
    protected void onDestroy() {
        unregisterChangeLangReceiver();
        super.onDestroy();
    }

    public interface ClickListener {
        void onSendClicked(int postion);
        void onReceivedClicked(int postion);
        void onItemClicked(int postion);
    }

    private void unregisterChangeLangReceiver() {
        unregisterReceiver(changeLanguageReceiver);
    }

    private void registerChangeLangdReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.Invoke.BROAD_CHANGE_LANG);
        registerReceiver(changeLanguageReceiver, intentFilter);
    }
}
