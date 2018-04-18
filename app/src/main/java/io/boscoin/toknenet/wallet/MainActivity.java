package io.boscoin.toknenet.wallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void createWallet(View view) {
        Intent it = new Intent(MainActivity.this, CreateWalletActivity.class);
        startActivity(it);

    }

    public void importWallet(View view) {
        Intent it = new Intent(MainActivity.this, RecoverActivity.class);
        startActivity(it);
    }
}
