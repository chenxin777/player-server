package com.chenxin.player.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author fangchenxin
 * @description 用户包装类
 * @date 2024/5/8 17:40
 * @modify
 */
@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = -4222429049727994784L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态（0正常）
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 0-普通用户 1-管理员
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;

    /**
     * 标签
     */
    private String tags;

}
