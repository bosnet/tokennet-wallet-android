package io.boscoin.toknenet.wallet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.boscoin.toknenet.wallet.adapter.AddressAdapter;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.AddressBook;
import io.boscoin.toknenet.wallet.utils.RecyclerViewItemClickListener;

public class ContactActivity extends AppCompatActivity implements View.OnClickListener{

    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private Context mContext;
    private RelativeLayout mEmpty;
    private RecyclerView mRV;
    private AddressAdapter mAdapter;
    private RelativeLayout mNavHis, mNavSend, mNavReceive, mNavContact;
    private ImageView mIcHis, mIcSend, mIcReceive, mIcContact;
    private TextView navTvhis, navTvSend, navTvReceive, navTvContact;
    private long mWalletIdx;
    private List<AddressBook> bookList;
    private AddressBook mBook;
    private static final int ADD_REQUEST_CODE = 3;
    private static final int EDIT_REQUEST_CODE = 4;
    private boolean mIsEmpty;
    private boolean mIsFromSend;

    public interface MenuClickListener {
        void onEditClicked(int postion);
        void onSendClicked(int postion);
        void onDeleteClicked(int postion);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mContext = this;

        Intent it = getIntent();
        //wallet
        mWalletIdx = it.getLongExtra(Constants.Invoke.ADDRESS_BOOK,0);
        mIsFromSend = it.getBooleanExtra(Constants.Invoke.SEND,false);

        initUI();
    }

    private void initUI() {
        mEmpty = findViewById(R.id.empty);
        mRV = findViewById(R.id.rv_contact_list);

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
        mIcSend.setBackgroundResource(R.drawable.ic_icon_send_disable);
        mIcReceive.setBackgroundResource(R.drawable.ic_icon_recieve_disable);
        mIcContact.setBackgroundResource(R.drawable.ic_icon_contacts_normal);

        navTvhis = findViewById(R.id.nav_his);
        navTvSend = findViewById(R.id.nav_send);
        navTvReceive = findViewById(R.id.nav_receive);
        navTvContact = findViewById(R.id.nav_contact);

        navTvhis.setTextColor(getResources().getColor(R.color.brownish_grey));
        navTvSend.setTextColor(getResources().getColor(R.color.brownish_grey));
        navTvReceive.setTextColor(getResources().getColor(R.color.brownish_grey));
        navTvContact.setTextColor(getResources().getColor(R.color.cerulean));

        findViewById(R.id.add_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ContactActivity.this, AddContactActivity.class);
                startActivityForResult(it, ADD_REQUEST_CODE);
            }
        });



        if(getAddressBookCount() == 0){
            mEmpty.setVisibility(View.VISIBLE);
            mRV.setVisibility(View.GONE);
            mIsEmpty = true;
        }else{
            mIsEmpty = false;
            mEmpty.setVisibility(View.GONE);
            mRV.setVisibility(View.VISIBLE);
            mRV.setLayoutManager(new LinearLayoutManager(mContext));

            DividerItemDecoration dividerItemDecoration =
                    new DividerItemDecoration(getApplicationContext(),new LinearLayoutManager(this).getOrientation());
            dividerItemDecoration.setDrawable(mContext.getResources().getDrawable(R.drawable.line_divider));


            mRV.addItemDecoration(dividerItemDecoration);
            initializeData();

            mAdapter = new AddressAdapter(bookList, mContext, new MenuClickListener() {
                @Override
                public void onEditClicked(int postion) {
                    AddressBook book = bookList.get(postion);
                    Intent it = new Intent(ContactActivity.this, EditContactActivity.class);
                    it.putExtra(Constants.Invoke.ADDRESS_BOOK, book);
                    startActivityForResult(it, EDIT_REQUEST_CODE);
                }

                @Override
                public void onSendClicked(int postion) {
                    Intent it = new Intent(ContactActivity.this, SendActivity.class);
                    it.putExtra(Constants.Invoke.SEND, mWalletIdx);
                    it.putExtra(Constants.Invoke.PUBKEY, bookList.get(postion).getAddress());
                    startActivity(it);
                }

                @Override
                public void onDeleteClicked(int postion) {
                    confirmDelete(postion);
                }
            }, mIsFromSend);
            mRV.setAdapter(mAdapter);
            mRV.setHasFixedSize(true);
        }
        if(mIsFromSend){
            mRV.addOnItemTouchListener(new RecyclerViewItemClickListener(mContext, mRV, new RecyclerViewItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {

                    Intent it = new Intent();
                    it.putExtra(Constants.Invoke.SEND, bookList.get(position).getAddress());
                    setResult(Constants.RssultCode.ADDRESS,it);
                    finish();

                }

                @Override
                public void onLongItemClick(View view, int position) {

                }
            }));
        }

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private int getAddressBookCount(){
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(Constants.DB.ADDRESS_BOOK);
        int count = mDbOpenHelper.getAddressCount();
        mDbOpenHelper.close();
        return count;
    }

    private void confirmDelete(int pos) {
        final int where = pos;
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext );
       // alert.setTitle(R.string.delete_title);
        alert.setMessage(R.string.delete_title);


        alert.setCancelable(false).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDbOpenHelper = new DbOpenHelper(mContext);
                mDbOpenHelper.open(Constants.DB.ADDRESS_BOOK);
                mDbOpenHelper.deleteColumnAddress(bookList.get(where).getAddressId());
                mDbOpenHelper.close();
                dialog.dismiss();

                if(getAddressBookCount() > 0){
                    bookList.clear();
                    getAddressList();
                    mAdapter.setAddBooktList(bookList);
                }else{
                    mEmpty.setVisibility(View.VISIBLE);
                    mRV.setVisibility(View.GONE);
                }

            }
        });

        alert.setCancelable(false).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = alert.create();

        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);
    }

    private void initializeData() {
        getAddressList();
    }

    private void getAddressList() {
        bookList = new ArrayList<>();
        mDbOpenHelper = new DbOpenHelper(mContext);
        mDbOpenHelper.open(Constants.DB.ADDRESS_BOOK);
        mCursor = null;

        mCursor = mDbOpenHelper.getAllColumnsAddress();

        while (mCursor.moveToNext()){

            mBook = new AddressBook(
                    mCursor.getLong(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.BOOK_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(Constants.DB.BOOK_ADDRESS))


            );

            bookList.add(0,mBook);
        }
        mCursor.close();
        mDbOpenHelper.close();
    }

    public void addContact(View view) {
        Intent it = new Intent(ContactActivity.this, AddContactActivity.class);
        startActivityForResult(it, ADD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == ADD_REQUEST_CODE || requestCode == EDIT_REQUEST_CODE){


            if(getAddressBookCount() == 0){

                mEmpty.setVisibility(View.VISIBLE);
                mRV.setVisibility(View.GONE);
            } else{

                mEmpty.setVisibility(View.GONE);
                mRV.setVisibility(View.VISIBLE);
                mRV.setLayoutManager(new LinearLayoutManager(mContext));

                DividerItemDecoration dividerItemDecoration =
                        new DividerItemDecoration(getApplicationContext(),new LinearLayoutManager(this).getOrientation());
                dividerItemDecoration.setDrawable(mContext.getResources().getDrawable(R.drawable.line_divider));


                mRV.addItemDecoration(dividerItemDecoration);
                initializeData();

                mAdapter = new AddressAdapter(bookList, mContext, new MenuClickListener() {
                    @Override
                    public void onEditClicked(int postion) {

                        AddressBook book = bookList.get(postion);
                        Intent it = new Intent(ContactActivity.this, EditContactActivity.class);
                        it.putExtra(Constants.Invoke.ADDRESS_BOOK, book);
                        startActivityForResult(it, EDIT_REQUEST_CODE);
                    }

                    @Override
                    public void onSendClicked(int postion) {
                        Intent it = new Intent(ContactActivity.this, SendActivity.class);
                        it.putExtra(Constants.Invoke.SEND, mWalletIdx);
                        startActivity(it);
                    }

                    @Override
                    public void onDeleteClicked(int postion) {
                        confirmDelete(postion);
                    }
                },mIsFromSend);
                mRV.setAdapter(mAdapter);
                mRV.setHasFixedSize(true);
            }



        }
    }

    @Override
    public void onClick(View v) {
        Intent it;
        switch (v.getId()){
            case R.id.menu_trans_his:
                it = new Intent(ContactActivity.this, WalletActivity.class);
                it.putExtra(Constants.Invoke.HISTORY,mWalletIdx);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;

            case R.id.menu_send:
                it = new Intent(ContactActivity.this, SendActivity.class);
                it.putExtra(Constants.Invoke.SEND, mWalletIdx);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;

            case R.id.menu_receive:
                it = new Intent(ContactActivity.this, ReceiveActivity.class);
                it.putExtra(Constants.Invoke.WALLET, mWalletIdx);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                break;

            case R.id.menu_contact:
                break;
        }
    }
}
