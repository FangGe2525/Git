package com.community.repair.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户。role 取值：OWNER（业主）、CS（客服）、WORKER（维修工）、COMMUNITY（社区工作人员）。
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 密文，绝不以明文落库或回传 */
    private String password;

    private String realName;

    private String role;

    private String phone;

    private String building;

    private String room;

    /** 技能标签，逗号分隔，仅维修工使用 */
    private String skillTags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
