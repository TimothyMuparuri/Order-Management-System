package com.oms.order.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class ContactInfo {

    private String email;
    private String phone;

    protected ContactInfo() {}

    public ContactInfo(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
