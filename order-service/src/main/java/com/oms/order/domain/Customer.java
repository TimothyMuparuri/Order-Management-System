package com.oms.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    private String customerId;

    @Embedded
    private ContactInfo contact;

    protected Customer() {}

    public Customer(String customerId, ContactInfo contact) {
        this.customerId = customerId;
        this.contact = contact;
    }

    public String getCustomerId() { return customerId; }
    public ContactInfo getContact() { return contact; }
}
