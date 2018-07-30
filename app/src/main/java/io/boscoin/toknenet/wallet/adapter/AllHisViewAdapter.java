package io.boscoin.toknenet.wallet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.AllHistoryFragment.OnListAllFragInteractionListener;
import io.boscoin.toknenet.wallet.R;
import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Wallet;
import io.boscoin.toknenet.wallet.utils.Utils;
import io.boscoin.toknenet.wallet.model.Payments;

import java.util.ArrayList;
import java.util.List;


public class AllHisViewAdapter extends RecyclerView.Adapter<AllHisViewAdapter.ViewHolder> {


    private ArrayList<Payments.PayRecords> mValues;
    private final OnListAllFragInteractionListener mListener;
    private String mPubKey;
    private static final String CREATE_ACCOUNT = "create_account";

    private Context mContext;

    public AllHisViewAdapter(ArrayList<Payments.PayRecords> items, OnListAllFragInteractionListener listener, String pubkey) {
        this.mValues = items;
        this.mListener = listener;
        this.mPubKey = pubkey;
    }

    public AllHisViewAdapter(ArrayList<Payments.PayRecords> items, OnListAllFragInteractionListener listener
            , String pubkey, Context cont) {
        this.mValues = items;
        this.mListener = listener;
        this.mPubKey = pubkey;
        this.mContext = cont;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_payment_all_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if(CREATE_ACCOUNT.equals(mValues.get(position).getType())){
            if(mValues.get(position).getFunder().equals(mPubKey)) {

                sentCreateDisplay(holder,position);
            }else{

                createDisplay(holder,position);
            }
        }else{
            if(mValues.get(position).getFrom().equals(mPubKey)){
                sentDisplay(holder,position);

            }else{

                receiveDisplay(holder,position);
            }
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {

                    mListener.ListAllFragInteraction(holder.mItem);
                }
            }
        });
    }

    private void sentCreateDisplay(ViewHolder holder, int pos) {
        String sendName;
        holder.mTvType.setText(R.string.sent);

        sendName = DbOpenHelper.getAddressName(mContext, mValues.get(pos).getAccount());
        if( sendName!= null){
            holder.mTvAddress.setText(sendName);
        }else{
            holder.mTvAddress.setText(Utils.contractionAddress(mValues.get(pos).getAccount()));
        }


        String tmp = Utils.fitDigit(mValues.get(pos).getStarting_balance());
        String amount = tmp+" BOS";

        holder.mTvAmount.setText(Utils.changeColorRed(amount));
        holder.mTvTime.setText(Utils.convertUtcToLocal(mValues.get(pos).getCreated_at()));
    }

    private void receiveDisplay(ViewHolder holder, int pos) {
        String receveName;
        holder.mTvType.setText(R.string.received);

        receveName = DbOpenHelper.getAddressName(mContext, mValues.get(pos).getFrom());
        if(receveName != null){
            holder.mTvAddress.setText(receveName);
        }else{
            holder.mTvAddress.setText(Utils.contractionAddress(mValues.get(pos).getFrom()));
        }


        String tmp = Utils.fitDigit(mValues.get(pos).getAmount());
        String amount = tmp+" BOS";

        holder.mTvAmount.setText(Utils.changeColorBlue(amount));
        holder.mTvTime.setText(Utils.convertUtcToLocal(mValues.get(pos).getCreated_at()));
    }

    private void createDisplay(ViewHolder holder, int pos) {
        String funderName;

        holder.mTvType.setText(R.string.create_ac);
        funderName = DbOpenHelper.getAddressName(mContext, mValues.get(pos).getFunder());
        if(funderName != null){
            holder.mTvAddress.setText(funderName);
        }else{
            holder.mTvAddress.setText(Utils.contractionAddress(mValues.get(pos).getFunder()));
        }


        String tmp = Utils.fitDigit(mValues.get(pos).getStarting_balance());
        String amount = tmp+" BOS";

        holder.mTvAmount.setText(Utils.changeColorBlue( amount));
        holder.mTvTime.setText(Utils.convertUtcToLocal(mValues.get(pos).getCreated_at()));
    }

    private void sentDisplay(ViewHolder holder, int pos) {
        String sendName;

        holder.mTvType.setText(R.string.sent);


        sendName = DbOpenHelper.getAddressName(mContext, mValues.get(pos).getTo());
        if(sendName != null){
            holder.mTvAddress.setText(sendName);
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
        if(mValues == null){
            return 0;
        }



        return mValues.size();
    }

    public void addAllHisList(ArrayList<Payments.PayRecords> list) {
        this.mValues = list;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTvType, mTvAddress, mTvAmount, mTvTime;
        public final View mView;
        public Payments.PayRecords mItem;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mTvType = itemView.findViewById(R.id.txt_type);
            mTvAddress = itemView.findViewById(R.id.txt_address);
            mTvAmount = itemView.findViewById(R.id.txt_amount);
            mTvTime = itemView.findViewById(R.id.txt_time);

        }

     

    }
}
