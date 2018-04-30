package io.boscoin.toknenet.wallet.model;

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

        @SerializedName("pagig_token")
        private String pagig_token;

        @SerializedName("created_at")
        private String created_at;

        @SerializedName("transaction_hash")
        private String transaction_hash;

       @SerializedName("type")
       private String type;

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

        public String getPagig_token() {
            return pagig_token;
        }

        public void setPagig_token(String pagig_token) {
            this.pagig_token = pagig_token;
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

