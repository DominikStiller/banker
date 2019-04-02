package de.domistiller.banker.model;

import java.util.Optional;

/**
 * Represents a customer
 */
public class Customer {

    private int id;
    private String name;
    private Optional<String> address;
    private Optional<String> email;
    private Optional<String> phone;
    private Optional<Integer> numAcounts;

    public Customer(String name) {
        this.id = 0;
        this.name = name;
        this.address = Optional.empty();
        this.email = Optional.empty();
        this.phone = Optional.empty();
    }

    public Customer(int id, String name, String address, String email, String phone, int numAccounts) {
        this.id = id;
        this.name = name;
        this.address = Optional.ofNullable(address);
        this.email = Optional.ofNullable(email);
        this.phone = Optional.ofNullable(phone);
        this.numAcounts = Optional.of(numAccounts);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address.orElse("");
    }

    public void setAddress(String address) {
        this.address = Optional.ofNullable(address);
    }

    public String getEmail() {
        return email.orElse("");
    }

    public void setEmail(String email) {
        this.email = Optional.ofNullable(email);
    }

    public String getPhone() {
        return phone.orElse("");
    }

    public void setPhone(String phone) {
        this.phone = Optional.ofNullable(phone);
    }

    public int getNumAcounts() {
        return numAcounts.orElse(0);
    }
}
