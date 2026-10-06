package com.pharmasafe.model;

/** Overall risk shown to the pharmacist for a batch. Ordered from lowest to highest. */
public enum RiskLevel {
    LOW,
    REVIEW_REQUIRED,
    HIGH_PRIORITY;

    public RiskLevel max(RiskLevel other) {
        return this.ordinal() >= other.ordinal() ? this : other;
    }
}
