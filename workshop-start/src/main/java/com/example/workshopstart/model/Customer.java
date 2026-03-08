package com.example.workshopstart.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean vip;

    @Embedded
    private Address address;

    protected Customer() {
    }

    public Customer(boolean vip, Address address) {
        this.vip = vip;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isInternationnal() {
        return !address.getCountry().equals("France");
    }
}
