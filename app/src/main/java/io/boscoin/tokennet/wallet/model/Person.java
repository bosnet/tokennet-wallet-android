package io.boscoin.toknenet.wallet.model;

public class Person {
   public long _id;
   public String name;
   public String address;
   public String memo;

    public Person(long _id , String name, String address, String memo) {
        this._id = _id;
        this.name = name;
        this.address = address;
        this.memo = memo;
    }
}