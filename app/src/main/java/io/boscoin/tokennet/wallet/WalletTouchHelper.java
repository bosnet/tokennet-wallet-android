package io.boscoin.toknenet.wallet;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class WalletTouchHelper extends ItemTouchHelper.Callback{

    private final OnItemMoveListener mItemMoveListener;

    public WalletTouchHelper(OnItemMoveListener mItemMoveListener) {
        this.mItemMoveListener = mItemMoveListener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mItemMoveListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    public interface OnItemMoveListener{
        void onItemMove(int fromPosition, int toPosition);
    }
}
