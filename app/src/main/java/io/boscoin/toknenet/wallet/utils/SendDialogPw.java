package io.boscoin.toknenet.wallet.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import io.boscoin.toknenet.wallet.R;

public class SendDialogPw extends Dialog{
    private View.OnClickListener mRightClickListener;
    private View.OnClickListener mLeftClickListener;
    private EditText editPw;
    private TextView mTvErr;

    public SendDialogPw(@NonNull Context context, View.OnClickListener rclistener, View.OnClickListener lclistener) {
        super(context);
        this.mRightClickListener = rclistener;
        this.mLeftClickListener = lclistener;
    }

    public SendDialogPw(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SendDialogPw(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.layout_dialog_pw);

        editPw = findViewById(R.id.input_pw);

        editPw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTvErr.setVisibility(View.GONE);
            }
        });


        mTvErr = findViewById(R.id.txt_err_key);
        mTvErr.setVisibility(View.GONE);

        findViewById(R.id.btn_ok).setOnClickListener(mRightClickListener);
        findViewById(R.id.btn_cancel).setOnClickListener(mLeftClickListener);

    }



    public EditText getEditPw() {
        return editPw;
    }

    public TextView getmTvErrKey() {
        return mTvErr;
    }
}
