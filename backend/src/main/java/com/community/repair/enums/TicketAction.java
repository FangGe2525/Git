package com.community.repair.enums;

/** 工单日志中的动作类型 */
public enum TicketAction {
    CREATE("创建工单"),
    ASSIGN("派单"),
    REASSIGN("转派"),
    ACCEPT("接单"),
    START("开始处理"),
    FINISH("完工提交"),
    CONFIRM("确认完工"),
    REWORK("返修"),
    RETURN("退回"),
    CANCEL("作废"),
    REMARK("备注"),
    URGE("催办");

    private final String label;

    TicketAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
