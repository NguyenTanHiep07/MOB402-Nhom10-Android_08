package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rejection_reasons")
public class RejectionReason {
    @Id
    private String code;
    private String label;
    private boolean valid;
    private int penaltyPoints;
    private boolean requiresNote;
    private boolean active = true;

    public RejectionReason() {}

    public RejectionReason(String code, String label, boolean valid, int penaltyPoints, boolean requiresNote) {
        this.code = code;
        this.label = label;
        this.valid = valid;
        this.penaltyPoints = penaltyPoints;
        this.requiresNote = requiresNote;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public int getPenaltyPoints() { return penaltyPoints; }
    public void setPenaltyPoints(int penaltyPoints) { this.penaltyPoints = penaltyPoints; }
    public boolean isRequiresNote() { return requiresNote; }
    public void setRequiresNote(boolean requiresNote) { this.requiresNote = requiresNote; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
