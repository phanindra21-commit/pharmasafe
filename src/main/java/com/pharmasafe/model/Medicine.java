package com.pharmasafe.model;

import jakarta.persistence.*;

/** A medicine product (brand + generic + strength + form). */
@Entity
@Table(name = "medicine")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brandName;

    @Column(nullable = false)
    private String genericName;

    private String strength;      // e.g. "500 mg"
    private String dosageForm;    // e.g. "Tablet"
    private String manufacturer;

    /**
     * Medicines that share a look-alike/sound-alike group are flagged so the
     * pharmacist double-checks the name before dispensing. Null = no known confusion.
     */
    private String lookAlikeGroup;

    protected Medicine() { }

    public Medicine(String brandName, String genericName, String strength,
                    String dosageForm, String manufacturer, String lookAlikeGroup) {
        this.brandName = brandName;
        this.genericName = genericName;
        this.strength = strength;
        this.dosageForm = dosageForm;
        this.manufacturer = manufacturer;
        this.lookAlikeGroup = lookAlikeGroup;
    }

    public Long getId() { return id; }
    public String getBrandName() { return brandName; }
    public String getGenericName() { return genericName; }
    public String getStrength() { return strength; }
    public String getDosageForm() { return dosageForm; }
    public String getManufacturer() { return manufacturer; }
    public String getLookAlikeGroup() { return lookAlikeGroup; }
}
