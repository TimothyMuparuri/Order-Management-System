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

    @Column(length = 200)
    private String name;

    @Embedded
    private ContactInfo contact;

    protected Customer() {}

    public Customer(String customerId, String name, ContactInfo contact) {
        this.customerId = customerId;
        this.name = name;
        this.contact = contact;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public ContactInfo getContact() { return contact; }

    public void setName(String name) { this.name = name; }

    public void setContact(ContactInfo contact) { this.contact = contact; }
}
