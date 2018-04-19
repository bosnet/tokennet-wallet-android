package io.boscoin.toknenet.wallet.model;


public class Wallet {
    private String walletName, walletKey, walletAccountId, walletBalance;
    private long walletId;
    private int walletOrder;


    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public String getWalletKey() {
        return walletKey;
    }

    public void setWalletKey(String walletKey) {
        this.walletKey = walletKey;
    }

    public String getWalletAccountId() {
        return walletAccountId;
    }

    public void setWalletAccountId(String walletAccountId) {
        this.walletAccountId = walletAccountId;
    }

    public String getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(String walletBalance) {
        this.walletBalance = walletBalance;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public int getWalletOrder() {
        return walletOrder;
    }

    public void setWalletOrder(int walletOrder) {
        this.walletOrder = walletOrder;
    }

    public Wallet(long _id , String wname, String wpubkey, String wboskey, int worder, String wasset) {
        this.walletId = _id;
        this.walletName = wname;
        this.walletAccountId = wpubkey;
        this.walletKey = wboskey;
        this.walletOrder = worder;
        this.walletBalance = wasset;
    }
}