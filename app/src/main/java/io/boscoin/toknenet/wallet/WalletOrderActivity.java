package io.boscoin.toknenet.wallet;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.boscoin.toknenet.wallet.adapter.WalletOrderAdapter;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Wallet;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.utils.WalletPreference;

public class WalletOrderActivity extends AppCompatActivity {


    private List<Wallet> walletList;
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private Wallet mWallet;
    private WalletOrderAdapter wAdapter;
    private TextView mBtnReorder;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_wallet_order);

        String lang = WalletPreference.getWalletLanguage(mContext);
        Utils.changeLanguage(mContext,lang);

        initUI();

    }

    private void initUI() {
        RecyclerView walletRV = findViewById(R.id.rv_worder_list);
        walletRV.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getApplicationContext(),new LinearLayoutManager(this).getOrientation());
        dividerItemDecoration.setDrawable(mContext.getResources().getDrawable(R.drawable.line_divider));

        wAdapter = new WalletOrderAdapter();
        WalletTouchHelper dragHelper = new WalletTouchHelper(wAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(dragHelper);
        wAdapter.setTouchHelper(touchHelper);

        walletRV.addItemDecoration(dividerItemDecoration);

        walletRV.setAdapter(wAdapter);
        touchHelper.attachToRecyclerView(walletRV);


        mBtnReorder = findViewById(R.id.btn_done);
        mBtnReorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbOpenHelper = new DbOpenHelper(mContext);
                mDbOpenHelper.open(Constants.DB.MY_WALLETS);

                walletList = wAdapter.getWalletList();



                int ordering = walletList.size()+1;

                for(Wallet w : walletList ){

                    ordering --;
                    mDbOpenHelper.updateColumnWallet(w.getWalletId(),w.getWalletName(),w.getWalletAccountId(),
                            w.getWalletKey(),ordering,w.getWalletBalance(),w.getWalletTime());
                }
                mDbOpenHelper.close();
                WalletPreference.setWalletIsChangeOrder(mContext,true);
                setResult(Activity.RESULT_OK);
                finish();
            }
        });


        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
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

}
