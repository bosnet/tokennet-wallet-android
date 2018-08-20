package io.boscoin.tokennet.wallet.model;

import java.io.Serializable;

public class AddressBook implements Serializable{
    private String addressName,address ;
    private long addressId;

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getAddressId() {
        return addressId;
    }

    public void setAddressId(long addressId) {
        this.addressId = addressId;
    }

    public AddressBook(AddressBook book){
        this.addressId = book.addressId;
        this.addressName = book.addressName;
        this.address = book.address;
    }

    public AddressBook(long id, String name, String address){
        this.addressId = id;
        this.addressName = name;
        this.address = address;
    }
}
