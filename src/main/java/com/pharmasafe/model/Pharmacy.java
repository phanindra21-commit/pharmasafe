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

    /** Location, used to measure distance from the pharmacist's area. Wrapper types so the column is nullable. */
    private Double latitude;
    private Double longitude;

    protected Pharmacy() { }

    public Pharmacy(String name, String area, String city, String phone, Double latitude, Double longitude) {
        this.name = name;
        this.area = area;
        this.city = city;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getArea() { return area; }
    public String getCity() { return city; }
    public String getPhone() { return phone; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}
