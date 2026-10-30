package com.community.repair.enums;

/**
 * 工单流程状态。
 * 评价与收费不占状态位：评价看 reviews 表，收费看 feeStatus。
 */
public enum TicketStatus {

    PENDING("待接单"),
    ACCEPTED("已接单"),
    REPAIRING("处理中"),
    VERIFYING("待确认"),
    COMPLETED("已完成"),
    CANCELED("已作废");

    private final String label;

    TicketStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELED;
    }
}
