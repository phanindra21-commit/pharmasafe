package com.pharmasafe.model;

import jakarta.persistence.*;

/** A pharmacy participating in the PharmaSafe network. */
@Entity
@Table(name = "pharmacy")
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String area;   // locality, e.g. "Tarnaka"
    private String city;
    private String phone;

    protected Pharmacy() { }

    public Pharmacy(String name, String area, String city, String phone) {
        this.name = name;
        this.area = area;
        this.city = city;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getArea() { return area; }
    public String getCity() { return city; }
    public String getPhone() { return phone; }
}
