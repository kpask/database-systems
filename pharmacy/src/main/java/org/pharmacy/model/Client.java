package org.pharmacy.model;

// package org.pharmacy;
public class Client {
    private final long id;
    private final String firstname;
    private final String lastname;
    private final String country;
    private final String city;
    private final String street;
    private final String postalcode;

    public Client(long id, String first_name, String last_name, String country, String city, String street, String postal_code){
        this.id = id;
        this.firstname = first_name;
        this.lastname = last_name;
        this.country = country;
        this.city = city;
        this.street = street;
        this.postalcode = postal_code;
    }

    public long getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getPostalCode() {
        return postalcode;
    }

}
