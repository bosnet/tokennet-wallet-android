package io.boscoin.toknenet.wallet.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import io.boscoin.toknenet.wallet.R;
import io.boscoin.toknenet.wallet.WalletListActivity;
import io.boscoin.toknenet.wallet.model.Wallet;

public class WalletListAdapter extends RecyclerView.Adapter<WalletListAdapter.WalletViewHolder>{

    private List<Wallet> mWalletList;

    private WalletListActivity.ClickListener listener;

    public WalletListAdapter(List<Wallet> list, WalletListActivity.ClickListener listener){
        this.mWalletList = list;
        this.listener = listener;
    }


    @Override
    public WalletListAdapter.WalletViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wlist_item, parent, false);
        WalletViewHolder wvh = new WalletViewHolder(v);
        return wvh;
    }

    @Override
    public void onBindViewHolder(WalletListAdapter.WalletViewHolder holder, int position) {
        holder.walletName.setText(mWalletList.get(position).getWalletName());
        holder.walletTime.setText( mWalletList.get(position).getWalletTime());
        String bal = mWalletList.get(position).getWalletBalance() + " BOS";

        holder.walletBalance.setText(bal);



    }

    @Override
    public int getItemCount() {
        return mWalletList.size();
    }

    public class WalletViewHolder extends RecyclerView.ViewHolder {


        private CardView cv;
        private TextView walletName;
        private TextView walletBalance;
        private TextView btnSend;
        private TextView btnReceive;
        private TextView walletTime;
        private WeakReference<WalletListActivity.ClickListener> listenerRef;

        public WalletViewHolder(View itemView) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            cv = (CardView)itemView.findViewById(R.id.cv_wallet);
            walletName = (TextView)itemView.findViewById(R.id.wallet_name);
            walletBalance = (TextView)itemView.findViewById(R.id.wallet_balance);
            walletTime = itemView.findViewById(R.id.wallet_time);
            btnReceive = itemView.findViewById(R.id.btn_receive);
            btnSend = itemView.findViewById(R.id.btn_send);

            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listenerRef.get().onSendClicked(getAdapterPosition());
                }
            });

            btnReceive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listenerRef.get().onReceivedClicked(getAdapterPosition());
                }
            });

            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listenerRef.get().onItemClicked(getAdapterPosition());
                }
            });
        }
    }
}
