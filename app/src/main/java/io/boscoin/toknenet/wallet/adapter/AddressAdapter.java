package io.boscoin.toknenet.wallet.adapter;

import android.content.Context;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import io.boscoin.toknenet.wallet.ContactActivity;
import io.boscoin.toknenet.wallet.R;
import io.boscoin.toknenet.wallet.model.AddressBook;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private static final String TAG = "AddressAdapter";
    private List<AddressBook> mAddBookList;
    private Context mContext;
    private ContactActivity.MenuClickListener listener;
    private boolean mIsHide;

    public AddressAdapter(List<AddressBook> mAddBookList, Context con, ContactActivity.MenuClickListener listener, boolean hide) {
        this.mAddBookList = mAddBookList;
        this.mContext = con;
        this.listener = listener;
        this.mIsHide =  hide;
    }

    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_book_item, parent, false);
        AddressViewHolder avh = new AddressViewHolder(v);
        return avh;
    }

    @Override
    public void onBindViewHolder(final AddressViewHolder holder, final int position) {

        holder.mbookTi.setText(mAddBookList.get(position).getAddressName());
        holder.mbookAddress.setText(mAddBookList.get(position).getAddress());

        holder.mBtnmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final WeakReference<ContactActivity.MenuClickListener> listenerWeakReference;
                listenerWeakReference = new WeakReference<>(listener);
                PopupMenu popup = new PopupMenu(mContext, holder.mBtnmore);
                popup.getMenuInflater()
                        .inflate(R.menu.navigation, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.navi_edit:

                                listenerWeakReference.get().onEditClicked(position);
                                break;

                            case R.id.navi_send:

                                listenerWeakReference.get().onSendClicked(position);
                                break;

                            case R.id.navi_del:

                                listenerWeakReference.get().onDeleteClicked(position);
                                break;

                        }

                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.mAddBookList.size();
    }

    public class AddressHolder extends RecyclerView.ViewHolder{
        public AddressHolder(View itemView) {
            super(itemView);
        }
    }

    public void setAddBooktList(List<AddressBook> bookList) {
        this.mAddBookList = bookList;
        notifyDataSetChanged();
    }

    public class AddressViewHolder extends  RecyclerView.ViewHolder{
        TextView mbookTi, mbookAddress;
        LinearLayout mBtnmore;



        public AddressViewHolder(View itemView) {
            super(itemView);

            mbookTi = itemView.findViewById(R.id.book_title);
            mbookAddress = itemView.findViewById(R.id.book_address);
            mBtnmore = itemView.findViewById(R.id.btn_more);
            if(mIsHide){
                mBtnmore.setVisibility(View.GONE);
            }else{
                mBtnmore.setVisibility(View.VISIBLE);
            }


        }
    }
}
