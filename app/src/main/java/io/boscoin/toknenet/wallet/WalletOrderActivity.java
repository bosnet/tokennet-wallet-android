package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import io.boscoin.toknenet.wallet.adapter.WalletOrderAdapter;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Wallet;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class WalletOrderActivity extends AppCompatActivity {

    private static final String TAG = "WalletOrderActivity";
    private List<Wallet> walletList;
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private Wallet mWallet;
    private WalletOrderAdapter wAdapter;
    private Button mBtnReorder;
    private Context mContext;
   // private WalletWorkerTask mTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_wallet_order);
        RecyclerView walletRV = findViewById(R.id.rv_worder_list);
        walletRV.setLayoutManager(new LinearLayoutManager(this));
        wAdapter = new WalletOrderAdapter();
        WalletTouchHelper dragHelper = new WalletTouchHelper(wAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(dragHelper);
        wAdapter.setTouchHelper(touchHelper);
        walletRV.setAdapter(wAdapter);
        touchHelper.attachToRecyclerView(walletRV);


        mBtnReorder = findViewById(R.id.btn_done);
        mBtnReorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbOpenHelper = new DbOpenHelper(mContext);
                mDbOpenHelper.open(Constants.DB.MY_WALLETS);

                walletList = wAdapter.getWalletList();


                // TODO: 2018. 4. 24. 변경되면 sharedpreference 에 저장 
                int ordering = 0;
                for(Wallet w : walletList ){
                    ordering++;
                    mDbOpenHelper.updateColumnWallet(w.getWalletId(),w.getWalletName(),w.getWalletAccountId(),
                    w.getWalletKey(),ordering,w.getWalletBalance());
                }
                mDbOpenHelper.close();
                WalletPreference.setWalletIsChangeOrder(mContext,true);
            }
        });

        getWalletList();

        wAdapter.setWalletList(walletList);
    }

    private void getWalletList() {
        walletList = new ArrayList<>();
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        mCursor = null;

        if(WalletPreference.getWalletIsChangeOrder(mContext)){
            mCursor = mDbOpenHelper.getColumnWalletByOrder("ASC");
        }else{
            mCursor = mDbOpenHelper.getColumnWalletByOrder("DESC");
        }


        while (mCursor.moveToNext()){

            mWallet = new Wallet(
                    mCursor.getLong(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_ADDRESS)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET)),
                    mCursor.getInt(mCursor.getColumnIndex(Constants.DB.WALLET_ORDER)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_LASTEST))


            );

            walletList.add(mWallet);
        }
        mCursor.close();
        mDbOpenHelper.close();

    }

    /*private void getWallerList2(){
        mTask = new WalletWorkerTask();
    }

    private class WalletWorkerTask extends AsyncTask{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }



        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }


        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e(TAG,"call onCancelled");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTask.onCancelled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTask.onCancelled();
    }*/
}
