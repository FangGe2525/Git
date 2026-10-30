package com.community.repair.enums;

/** 报修来源 */
public enum TicketSource {
    SCAN("扫码报修"),
    PHONE("电话代报"),
    WALK_IN("上门报修"),
    OTHER("其他");

    private final String label;

    TicketSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
