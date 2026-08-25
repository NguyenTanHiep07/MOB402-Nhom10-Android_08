package com.mob10.deliveryserver.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "rejection_reasons")
public class RejectionReason {
    @Id
    @Column(length = 50)
    private String code;
    @Column(nullable = false, length = 200)
    private String label;
    @Column(name = "is_valid", nullable = false)
    private boolean valid;
    @Column(name = "penalty_points", nullable = false)
    private int penaltyPoints;
    @Column(name = "requires_note", nullable = false)
    private boolean requiresNote;
    @Column(nullable = false)
    private boolean active = true;

    protected RejectionReason() {}
    public RejectionReason(String code, String label, boolean valid, int penaltyPoints, boolean requiresNote) {
        this.code = code; this.label = label; this.valid = valid;
        this.penaltyPoints = penaltyPoints; this.requiresNote = requiresNote;
    }
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public boolean isValid() { return valid; }
    public int getPenaltyPoints() { return penaltyPoints; }
    public boolean isRequiresNote() { return requiresNote; }
    public boolean isActive() { return active; }
}
