package com.pharmasafe.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/** One manufactured batch (lot) of a medicine. */
@Entity
@Table(name = "batch")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String batchNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    private LocalDate manufactureDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private String supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status = BatchStatus.ACTIVE;

    protected Batch() { }

    public Batch(String batchNumber, Medicine medicine, LocalDate manufactureDate,
                 LocalDate expiryDate, String supplier) {
        this.batchNumber = batchNumber;
        this.medicine = medicine;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
    }

    public Long getId() { return id; }
    public String getBatchNumber() { return batchNumber; }
    public Medicine getMedicine() { return medicine; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getSupplier() { return supplier; }
    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }
}
