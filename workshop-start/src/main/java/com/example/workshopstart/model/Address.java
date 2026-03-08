package com.example.workshopstart.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private String country;

    protected Address() {
    }

    public Address(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
