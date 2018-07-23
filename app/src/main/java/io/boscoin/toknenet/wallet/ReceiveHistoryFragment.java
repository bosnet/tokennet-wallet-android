package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import io.boscoin.toknenet.wallet.adapter.ReceiveHisViewAdapter;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.model.Payments;


public class ReceiveHistoryFragment extends Fragment {



    private OnListReceiveFragInteractionListener mListener;
    private String mPubkey;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Payments.PayRecords> mPayHistoryList = new ArrayList<>();
    private Context mContext;

    private RecyclerView mReceiveHisRv;
    private ReceiveHisViewAdapter mRhisAdapter;
    private Payments mPayments;
    private String mSartOff, mLastOff;
    private static final int NO_VISIBLE_ITEM = -1;
    private static final int PORT_HTTP = 80;
    private static final int PORT_HTTPS = 443;

    public ReceiveHistoryFragment() {
    }

 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (getArguments() != null) {

            mPubkey = getArguments().getString(Constants.Invoke.PUBKEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_receive_his_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                getRecentHistory(mSartOff);
                mListener.getCurrentBalanceReceive();
            }
        });

        mContext = view.getContext();

        mReceiveHisRv = (RecyclerView)view.findViewById(R.id.list);

        mReceiveHisRv.setLayoutManager(new LinearLayoutManager(mContext));
        getStartHistory();
        mListener.getCurrentBalanceReceive();

        mReceiveHisRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    isLastItem();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });



        return view;
    }

    private void isLastItem() {
       
        int lastVisibleItemPos = ((LinearLayoutManager) mReceiveHisRv.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        int firstVisbleItemPos = ((LinearLayoutManager) mReceiveHisRv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();


        if(lastVisibleItemPos == NO_VISIBLE_ITEM || firstVisbleItemPos == NO_VISIBLE_ITEM){
            return;
        }


        int itemTotalCount = mReceiveHisRv.getAdapter().getItemCount();


        if (lastVisibleItemPos +firstVisbleItemPos >= itemTotalCount) {


            getPrevHistory(mLastOff);
        }
    }

    private void getPrevHistory(String cursor) {
        if(mSwipeRefreshLayout.isRefreshing()){
            return;
        }


        AsyncHttpClient client = new AsyncHttpClient(true, PORT_HTTP,PORT_HTTPS);
        RequestParams params = new RequestParams();
        params.put(Constants.Params.CURSOR, cursor);
        params.put(Constants.Params.ORDER, Constants.Params.DESC);

        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(mPubkey);
        url.append("/");
        url.append(Constants.Params.PAYMENTS);

        client.get(String.valueOf(url),params,new TextHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Gson gson = new GsonBuilder().create();
                mPayments =  gson.fromJson(res, Payments.class);
                if(mPayments.get_embedded().getRecordList().size() > 0){

                    mSartOff = mPayments.get_embedded().getRecordList().get(0).getPaging_token();
                    mLastOff = mPayments.get_embedded().getRecordList().get( mPayments.get_embedded().getRecordList().size() -1).getPaging_token();
                    setReceiveHistoryAfter(mPayments.get_embedded().getRecordList());

                    mRhisAdapter.addReceiveHisList(mPayHistoryList);

                    mSwipeRefreshLayout.setRefreshing(false);
                }else{

                    mSwipeRefreshLayout.setRefreshing(false);
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setReceiveHistoryBefore(ArrayList<Payments.PayRecords> his) {
        for(int i = 0; i< his.size(); i++){
            if(his.get(i).getType_i().equals("0") && !his.get(i).getFunder().equals(mPubkey)){
                mPayHistoryList.add(0, his.get(i));
            } else if(!his.get(i).getFrom().equals(mPubkey)){
                mPayHistoryList.add(0, his.get(i));
            } else if(his.get(i).getFrom().equals(mPubkey) && his.get(i).getTo().equals(mPubkey)){
                mPayHistoryList.add(0, his.get(i));
            }
        }

    }

    private void setReceiveHistoryAfter(ArrayList<Payments.PayRecords> his) {
        for(int i = 0; i< his.size(); i++){
            if(his.get(i).getType_i().equals("0") && !his.get(i).getFunder().equals(mPubkey)){
                mPayHistoryList.add(his.get(i));
            } else if(his.get(i).getType_i().equals("0") && his.get(i).getFunder().equals(mPubkey)){
                continue; //because create account from me
            } else if(!his.get(i).getFrom().equals(mPubkey)){
                mPayHistoryList.add(his.get(i));
            } else if(his.get(i).getFrom().equals(mPubkey) && his.get(i).getTo().equals(mPubkey)){
                mPayHistoryList.add(his.get(i));
            }
        }

    }
    private void getStartHistory() {

        AsyncHttpClient client = new AsyncHttpClient(true, PORT_HTTP,PORT_HTTPS);
        RequestParams params = new RequestParams();
        params.put(Constants.Params.ORDER, Constants.Params.DESC);

        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(mPubkey);
        url.append("/");
        url.append(Constants.Params.PAYMENTS);

        client.get(String.valueOf(url),params,new TextHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Gson gson = new GsonBuilder().create();
                mPayments =  gson.fromJson(res, Payments.class);

                if(mPayments.get_embedded().getRecordList().size() > 0){


                    mSartOff = mPayments.get_embedded().getRecordList().get(0).getPaging_token();
                    mLastOff = mPayments.get_embedded().getRecordList().get( mPayments.get_embedded().getRecordList().size() -1).getPaging_token();
                    setReceiveHistoryAfter(mPayments.get_embedded().getRecordList());
                    mRhisAdapter = new ReceiveHisViewAdapter( mPayHistoryList, mListener,mPubkey);
                    mReceiveHisRv.setAdapter( mRhisAdapter);
                    mReceiveHisRv.setHasFixedSize(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                }else {

                    mSwipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getRecentHistory(String cursor) {


        AsyncHttpClient client = new AsyncHttpClient(true, PORT_HTTP,PORT_HTTPS);
        RequestParams params = new RequestParams();
        params.put(Constants.Params.CURSOR, cursor);
        params.put(Constants.Params.ORDER, Constants.Params.ASC);

        StringBuilder url = new StringBuilder(Constants.Domain.BOS_HORIZON_TEST);
        url.append("/");
        url.append(Constants.Params.ACCOUNTS);
        url.append("/");
        url.append(mPubkey);
        url.append("/");
        url.append(Constants.Params.PAYMENTS);

        client.get(String.valueOf(url),params,new TextHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Gson gson = new GsonBuilder().create();

                mPayments =  gson.fromJson(res, Payments.class);
                if(mPayments.get_embedded().getRecordList().size() > 0){

                    mSartOff = mPayments.get_embedded().getRecordList().get(0).getPaging_token();
                    mLastOff = mPayments.get_embedded().getRecordList().get( mPayments.get_embedded().getRecordList().size() -1).getPaging_token();
                    setReceiveHistoryBefore(mPayments.get_embedded().getRecordList());

                    mRhisAdapter.addReceiveHisList(mPayHistoryList);

                    mSwipeRefreshLayout.setRefreshing(false);
                }else{

                    mSwipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListReceiveFragInteractionListener) {
            mListener = (OnListReceiveFragInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnListReceiveFragInteractionListener {
        void ListReceiveFragInteraction(Payments.PayRecords item);
        void getCurrentBalanceReceive();
    }
}
