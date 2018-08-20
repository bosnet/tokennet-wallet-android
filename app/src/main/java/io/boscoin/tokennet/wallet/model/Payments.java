package io.boscoin.tokennet.wallet.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;



public class Payments {

    private RecordsEntity _embedded;

    public RecordsEntity get_embedded() {
        return _embedded;
    }

    public void set_embedded(RecordsEntity _embedded) {
        this._embedded = _embedded;
    }

    public class RecordsEntity {
        private ArrayList<PayRecords> records;

        public ArrayList<PayRecords> getRecordList() {
            return records;
        }

        public void setRecordList(ArrayList<PayRecords> recordList) {
            this.records = recordList;
        }
    }

   public class PayRecords {
        @SerializedName("from")
        private String from;

        @SerializedName("to")
        private String to;

        @SerializedName("amount")
        private String amount;



       @SerializedName("paging_token")
       private String paging_token;

        @SerializedName("created_at")
        private String created_at;

        @SerializedName("transaction_hash")
        private String transaction_hash;

       @SerializedName("type")
       private String type;

       @SerializedName("funder")
       private String funder;

       @SerializedName("starting_balance")
       private String starting_balance;

       @SerializedName("type_i")
       private String type_i;


       @SerializedName("source_account")
       private String source_account;

       @SerializedName("account")
       private String account;


       public String getAccount() {
           return account;
       }

       public void setAccount(String account) {
           this.account = account;
       }

       public String getSource_account() {
           return source_account;
       }

       public void setSource_account(String source_account) {
           this.source_account = source_account;
       }

       public String getPaging_token() {
           return paging_token;
       }

       public void setPaging_token(String paging_token) {
           this.paging_token = paging_token;
       }

       public String getType_i() {
           return type_i;
       }

       public void setType_i(String type_i) {
           this.type_i = type_i;
       }

       public String getStarting_balance() {
           return starting_balance;
       }

       public void setStarting_balance(String starting_balance) {
           this.starting_balance = starting_balance;
       }

       public String getFunder() {
           return funder;
       }

       public void setFunder(String funder) {
           this.funder = funder;
       }

       public String getType() {
           return type;
       }

       public void setType(String type) {
           this.type = type;
       }

       public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }


        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public String getTransaction_hash() {
            return transaction_hash;
        }

        public void setTransaction_hash(String transaction_hash) {
            this.transaction_hash = transaction_hash;
        }
    }
}

