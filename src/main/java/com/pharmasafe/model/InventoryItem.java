package com.pharmasafe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** How many units of a given batch a given pharmacy holds. */
@Entity
@Table(name = "inventory_item",
       uniqueConstraints = @UniqueConstraint(columnNames = {"pharmacy_id", "batch_id"}))
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(nullable = false)
    private int quantity;

    private LocalDateTime updatedAt;

    protected InventoryItem() { }

    public InventoryItem(Pharmacy pharmacy, Batch batch, int quantity) {
        this.pharmacy = pharmacy;
        this.batch = batch;
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Pharmacy getPharmacy() { return pharmacy; }
    public Batch getBatch() { return batch; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
