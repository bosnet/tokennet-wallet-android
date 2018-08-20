package io.boscoin.toknenet.wallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.SendHistoryFragment.OnListSendFragInteractionListener;
import io.boscoin.toknenet.wallet.R;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Payments;
import io.boscoin.toknenet.wallet.utils.Utils;

import java.util.ArrayList;


public class SendHisViewAdapter extends RecyclerView.Adapter<SendHisViewAdapter.ViewHolder> {


    private ArrayList<Payments.PayRecords> mValues;
    private final OnListSendFragInteractionListener mListener;
    private String mPubKey;
    private static final String CREATE_ACCOUNT = "create_account";
    private Context mContext;


    public SendHisViewAdapter(ArrayList<Payments.PayRecords> items, OnListSendFragInteractionListener listener,  String pubkey) {
        this.mValues = items;
        this.mListener = listener;
        this.mPubKey = pubkey;
    }

    public SendHisViewAdapter(ArrayList<Payments.PayRecords> items, OnListSendFragInteractionListener listener
            , String pubkey, Context contx) {
        this.mValues = items;
        this.mListener = listener;
        this.mPubKey = pubkey;
        this.mContext = contx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_payment_send_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if(CREATE_ACCOUNT.equals(mValues.get(position).getType())){
            if(mValues.get(position).getFunder().equals(mPubKey)) {

                sentCreateDisplay(holder,position);
            }
        }else if(mValues.get(position).getFrom().equals(mPubKey)){

                sentDisplay(holder,position);

        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {

                    mListener.ListSendFragInteraction(holder.mItem);
                }
            }
        });
    }

    private void sentCreateDisplay(SendHisViewAdapter.ViewHolder holder, int pos) {
        String funderName;
        holder.mTvType.setText(R.string.sent);

        funderName = DbOpenHelper.getAddressName(mContext, mValues.get(pos).getAccount());

        if(funderName != null){
            holder.mTvAddress.setText(funderName);
        }else{
            holder.mTvAddress.setText(Utils.contractionAddress(mValues.get(pos).getAccount()));
        }


        String tmp = Utils.fitDigit(mValues.get(pos).getStarting_balance());
        String amount = tmp+" BOS";

        holder.mTvAmount.setText(Utils.changeColorRed(amount));
        holder.mTvTime.setText(Utils.convertUtcToLocal(mValues.get(pos).getCreated_at()));
    }

    private void sentDisplay(SendHisViewAdapter.ViewHolder holder, int pos) {
        String sentName;

        holder.mTvType.setText(R.string.sent);
        sentName = DbOpenHelper.getAddressName(mContext, mValues.get(pos).getTo());

        if(sentName != null){
            holder.mTvAddress.setText(sentName);
        }else{
            holder.mTvAddress.setText(Utils.contractionAddress(mValues.get(pos).getTo()));
        }


        String tmp = Utils.fitDigit(mValues.get(pos).getAmount());
        String amount = tmp+" BOS";

        holder.mTvAmount.setText(Utils.changeColorRed(amount));
        holder.mTvTime.setText(Utils.convertUtcToLocal(mValues.get(pos).getCreated_at()));
    }

    @Override
    public int getItemCount() {

        return mValues.size();
    }

    public void addSendHisList(ArrayList<Payments.PayRecords> list) {
        this.mValues = list;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTvType, mTvAddress, mTvAmount, mTvTime;
        public final View mView;
        public Payments.PayRecords mItem;

        public ViewHolder(View view) {
            super(view);

            mView = itemView;
            mTvType = itemView.findViewById(R.id.txt_type);
            mTvAddress = itemView.findViewById(R.id.txt_address);
            mTvAmount = itemView.findViewById(R.id.txt_amount);
            mTvTime = itemView.findViewById(R.id.txt_time);
        }

    }
}
