package io.boscoin.tokennet.wallet.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.boscoin.tokennet.wallet.R;
import io.boscoin.tokennet.wallet.WalletTouchHelper;
import io.boscoin.tokennet.wallet.model.Wallet;

public class WalletOrderAdapter extends RecyclerView.Adapter<WalletOrderAdapter.WalletOrderViewHolder> implements
        WalletTouchHelper.OnItemMoveListener{

    private List<Wallet> mWalletList;
    private ItemTouchHelper touchHelper;

    @Override
    public WalletOrderAdapter.WalletOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_worder_list_item, parent, false);
        WalletOrderViewHolder walletOrderVh = new WalletOrderViewHolder(v);
        return walletOrderVh;
    }

    @Override
    public void onBindViewHolder(final WalletOrderAdapter.WalletOrderViewHolder holder, int position) {
        holder.walletName.setText(mWalletList.get(position).getWalletName());
        holder.reorder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    touchHelper.startDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWalletList.size();
    }

    @Override
    public void onItemMove(int oldPosition, int newPosition) {
        Wallet targetWallet = mWalletList.get(oldPosition);
        Wallet wallet = new Wallet(targetWallet);
        mWalletList.remove(oldPosition);
        mWalletList.add(newPosition,wallet);
        notifyItemMoved(oldPosition,newPosition);
    }

    public void setWalletList(List<Wallet> walletList) {
        this.mWalletList = walletList;
        notifyDataSetChanged();
    }

    public List<Wallet> getWalletList() {
        return mWalletList;
    }

    public class WalletOrderViewHolder extends RecyclerView.ViewHolder {

        private TextView walletName;
        private ImageView reorder;

        public WalletOrderViewHolder(View itemView) {
            super(itemView);

            walletName = itemView.findViewById(R.id.tv_name);
            reorder = itemView.findViewById(R.id.iv_reorder);

        }
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {

        this.touchHelper = touchHelper;
    }
}
