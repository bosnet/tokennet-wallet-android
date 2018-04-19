package io.boscoin.toknenet.wallet;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import io.boscoin.toknenet.wallet.adapter.AllHisViewAdapter;
import io.boscoin.toknenet.wallet.conf.Constants;
import io.boscoin.toknenet.wallet.model.Payments;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListAllFragInteractionListener}
 * interface.
 */
public class AllHistoryFragment extends Fragment {

    private static final String TAG = "AllHistoryFragment";
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListAllFragInteractionListener mListener;
    private CurrentBalanceListener mGetListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mPubkey;
    private Context mContext;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AllHistoryFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG,"CREATE");
        if (getArguments() != null) {
            mPubkey = getArguments().getString(Constants.Invoke.PUBKEY);
            Log.e(TAG, "pubkey = "+mPubkey);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_all_his_list, container, false);

        // Retrieve the SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                getCurrentHistoryAll();
                mGetListener.getCurrentBalanceAll();
            }
        });


        mContext = view.getContext();
        recyclerView = (RecyclerView)view.findViewById(R.id.list);

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(mContext, mColumnCount));
        }

        getCurrentHistoryAll();
        mGetListener.getCurrentBalanceAll();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListAllFragInteractionListener) {
            mListener = (OnListAllFragInteractionListener) context;
            mGetListener = (CurrentBalanceListener) context;
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


    private void getCurrentHistoryAll() {
        AsyncHttpClient client = new AsyncHttpClient();
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
                Payments payments =  gson.fromJson(res, Payments.class);

                recyclerView.setAdapter(new AllHisViewAdapter(payments.get_embedded().getRecordList(), mListener));
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });
    }





    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListAllFragInteractionListener {
        // TODO: Update argument type and name
        void ListAllFragInteraction(Payments.PayRecords item);
    }

    public interface CurrentBalanceListener {
        void getCurrentBalanceAll();
    }
}
