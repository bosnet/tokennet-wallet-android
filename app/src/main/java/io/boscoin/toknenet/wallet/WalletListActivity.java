package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import io.boscoin.toknenet.wallet.adapter.WalletListAdapter;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Wallet;


public class WalletListActivity extends AppCompatActivity {

    private Wallet mWallet;
    private RecyclerView rv;
    private DbOpenHelper mDbOpenHelper;
    private List<Wallet> walletList;
    private Cursor mCursor;
    private Context mContext;
    private Button mBtnSetting;

    public interface ClickListener {
        void onSendClicked(int postion);
        void onReceivedClicked(int postion);
        void onItemClicked(int postion);
    }

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
                Intent it = new Intent(WalletListActivity.this, WalletOrderActivity.class);
                startActivity(it);
            }
        });

        initializeData();
        initializeAdapter();
    }

    private void initializeData() {

        walletList = new ArrayList<>();
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);

        mCursor = null;
        mCursor = mDbOpenHelper.getAllColumnsWallet();

        while (mCursor.moveToNext()){

            mWallet = new Wallet(
                    mCursor.getLong(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_ADDRESS)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_KET)),
                    mCursor.getInt(mCursor.getColumnIndex(Constants.DB.WALLET_ORDER)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.WALLET_LASTEST))


            );

            walletList.add(0,mWallet);
        }
        mCursor.close();
        mDbOpenHelper.close();

    }

    private void initializeAdapter(){
        WalletListAdapter adapter = new WalletListAdapter(walletList, new ClickListener() {
            @Override
            public void onSendClicked(int postion) {
                Intent it = new Intent(WalletListActivity.this, SendActivity.class);
                it.putExtra(Constants.Invoke.SEND, walletList.get(postion).getWalletId());
                startActivity(it);
            }

            @Override
            public void onReceivedClicked(int postion) {
                // TODO: 2018. 4. 12. will be needs received activity 
                Intent it = new Intent(WalletListActivity.this, ReceiveActivity.class);
                it.putExtra(Constants.Invoke.WALLET, walletList.get(postion).getWalletId());
                startActivity(it);
            }

            @Override
            public void onItemClicked(int postion) {
                Intent it = new Intent(WalletListActivity.this, HistoryActivity.class);
                it.putExtra(Constants.Invoke.HISTORY, walletList.get(postion).getWalletId());
                startActivity(it);
            }
        });
        rv.setAdapter(adapter);
    }
}
