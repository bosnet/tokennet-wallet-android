package io.boscoin.tokennet.wallet.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class Account {
   private static final String TAG = "Account";

   @SerializedName("account_id")
   private String account_id;

   @SerializedName("sequence")
   private String sequence;

   @SerializedName("balances")
   private ArrayList<Balances> balances;

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public ArrayList<Balances> getBalances() {
        return balances;
    }

    public void setBalances(ArrayList<Balances> balances) {
        this.balances = balances;
    }

    public class Balances {
      @SerializedName("balance")
      String balance;

       public String getBalance() {
           return balance;
       }

       public void setBalance(String balance) {
           this.balance = balance;
       }
   }
}
