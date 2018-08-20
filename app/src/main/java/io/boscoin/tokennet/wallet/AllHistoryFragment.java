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
import io.boscoin.toknenet.wallet.adapter.AllHisViewAdapter;
import io.boscoin.toknenet.wallet.conf.Constants;

import io.boscoin.toknenet.wallet.db.DbOpenHelper;
import io.boscoin.toknenet.wallet.model.Payments;


public class AllHistoryFragment extends Fragment {



    private OnListAllFragInteractionListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mPubkey;
    private Context mContext;
    private RecyclerView recyclerView;
    private AllHisViewAdapter mAhisAdapter;
    private Payments mPayments;
    private ArrayList<Payments.PayRecords> mPayHistoryList = new ArrayList<>();
    private static final int NO_VISIBLE_ITEM = -1;
    private static final int PORT_HTTP = 80;
    private static final int PORT_HTTPS = 443;

    public AllHistoryFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mPubkey = getArguments().getString(Constants.Invoke.PUBKEY);
            Log.e(TAG, "pubkey = "+mPubkey);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_all_his_list, container, false);


        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                String cur = mPayHistoryList.get(0).getPaging_token();


                getRecentHistory(cur);

                mListener.getCurrentBalanceAll();
            }
        });


        mContext = view.getContext();
        recyclerView = (RecyclerView)view.findViewById(R.id.list);


        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        getStartHistory();

        mListener.getCurrentBalanceAll();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        int lastVisibleItemPos = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        int firstVisbleItemPos = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();


        if(lastVisibleItemPos == NO_VISIBLE_ITEM || firstVisbleItemPos == NO_VISIBLE_ITEM){
            return;
        }

        int itemTotalCount = recyclerView.getAdapter().getItemCount();


        if (lastVisibleItemPos +firstVisbleItemPos >= itemTotalCount) {


             int size = mPayHistoryList.size();
             String cur = mPayHistoryList.get(size-1).getPaging_token();
             getPrevHistory(cur);
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListAllFragInteractionListener) {
            mListener = (OnListAllFragInteractionListener) context;

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
                    mPayHistoryList.addAll(0, mPayments.get_embedded().getRecordList());


                    mAhisAdapter = new AllHisViewAdapter( mPayHistoryList, mListener,mPubkey,  mContext);

                    recyclerView.setAdapter( mAhisAdapter);
                    recyclerView.setHasFixedSize(true);
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
                    mPayHistoryList.addAll(0,mPayments.get_embedded().getRecordList());
                    mAhisAdapter.addAllHisList(mPayHistoryList);
                    
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

    private void getPrevHistory(String cursor){
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
                int addCount ;
                int prevCount = mPayHistoryList.size();
                Gson gson = new GsonBuilder().create();
                mPayments =  gson.fromJson(res, Payments.class);
                addCount = mPayments.get_embedded().getRecordList().size();
                if( addCount  > 0){

                    mPayHistoryList.addAll(mPayments.get_embedded().getRecordList());
                   
                    mAhisAdapter.addAllHisList(mPayHistoryList);
                  
                   

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

    public interface OnListAllFragInteractionListener {
        void ListAllFragInteraction(Payments.PayRecords item);
        void getCurrentBalanceAll();

    }



}
