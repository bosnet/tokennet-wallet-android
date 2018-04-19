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

public class HistoryActivity extends AppCompatActivity implements
        AllHistoryFragment.OnListAllFragInteractionListener,AllHistoryFragment.CurrentBalanceListener,
        PaymentFragment.OnListPayFragInteractionListener, ReceiptFragment.OnListReceiptFragInteractionListener
{

    private static final String TAG = "HistoryActivity";
    private DbOpenHelper mDbOpenHelper;
    private TextView wName , wBalance, wPubKey;
    private String mMyPublicKey;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mContext = this;

        initUI();

    }


    private void initUI() {
        Intent it = getIntent();

        long idx = it.getLongExtra(Constants.Invoke.HISTORY,0);
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.MY_WALLETS);
        Cursor cursor = mDbOpenHelper.getColumnWallet(idx);

        wName = findViewById(R.id.tv_wname);
        wName.setText(cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_NAME)));

        wBalance = findViewById(R.id.tv_balances);
        wBalance.setText(cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_LASTEST)));

        wPubKey = findViewById(R.id.tv_pub_key);
        mMyPublicKey = cursor.getString(cursor.getColumnIndex(Constants.DB.WALLET_ADDRESS));
        wPubKey.setText(Utils.contractionAddress(mMyPublicKey));

        AllHistoryFragment allf = new AllHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Invoke.PUBKEY, mMyPublicKey);
        allf.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,allf).commit();
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
                wBalance.setText(account.getBalances().get(0).getBalance()+" BOS");
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
    }

    public void viewReceiveHistory(View view) {
        ReceiptFragment recipf = new ReceiptFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,recipf).commit();
    }

    public void viewSendHistory(View view) {
        PaymentFragment payf = new PaymentFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,payf).commit();
    }

    public void goHistoryView(View view) {
        return;
    }

    public void goSendView(View view) {
        Intent it = new Intent(HistoryActivity.this, SendActivity.class);
        startActivity(it);
    }

    public void goReceiveView(View view) {
        // TODO: 2018. 4. 12. will be needs received activity
        Intent it = new Intent(HistoryActivity.this, QRActivity.class);
        it.putExtra(Constants.Invoke.WALLET, mMyPublicKey);
        startActivity(it);
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
}
