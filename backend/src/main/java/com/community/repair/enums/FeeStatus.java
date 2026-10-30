package com.community.repair.enums;

/** 费用状态，与流程状态解耦 */
public enum FeeStatus {
    NONE("无费用"),
    PENDING("待缴费"),
    PAID("已缴费");

    private final String label;

    FeeStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
