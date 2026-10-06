package com.pharmasafe.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/** A recall notice against a specific batch. */
@Entity
@Table(name = "recall")
public class Recall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecallSeverity severity;

    private String issuedBy;      // e.g. "State Drug Control Authority (sample)"
    private LocalDate issuedOn;
    private boolean active = true;

    protected Recall() { }

    public Recall(Batch batch, String reason, RecallSeverity severity, String issuedBy, LocalDate issuedOn) {
        this.batch = batch;
        this.reason = reason;
        this.severity = severity;
        this.issuedBy = issuedBy;
        this.issuedOn = issuedOn;
    }

    public Long getId() { return id; }
    public Batch getBatch() { return batch; }
    public String getReason() { return reason; }
    public RecallSeverity getSeverity() { return severity; }
    public String getIssuedBy() { return issuedBy; }
    public LocalDate getIssuedOn() { return issuedOn; }
    public boolean isActive() { return active; }
}
