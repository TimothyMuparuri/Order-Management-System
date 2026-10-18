package com.oms.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ContactInfo {

    @Column(length = 200)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 400)
    private String address;

    protected ContactInfo() {}

    public ContactInfo(String email, String phone) {
        this(email, phone, null);
    }

    public ContactInfo(String email, String phone, String address) {
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
}
