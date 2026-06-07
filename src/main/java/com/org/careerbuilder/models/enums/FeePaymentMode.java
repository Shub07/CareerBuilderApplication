package com.org.careerbuilder.models.enums;

/**
 * Mode used when collecting or refunding a fee at the admin desk.
 */
public enum FeePaymentMode {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card"),
    BANK_TRANSFER("Bank Transfer"),
    CHEQUE("Cheque"),
    ONLINE("Online");

    private final String label;

    FeePaymentMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
