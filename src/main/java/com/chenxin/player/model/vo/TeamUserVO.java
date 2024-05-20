package com.chenxin.player.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author fangchenxin
 * @description 队伍和用户信息封装类
 * @date 2024/5/8 17:37
 * @modify
 */
@Data
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = -5128886167720344510L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 状态 0公开 1私有 2加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人用户信息
     */
    private UserVO createUser;

    /**
     * 是否已加入队伍,针对登录用户判断
     */
    private Boolean hasJoin = false;

    /**
     * 用户加入数
     */
    private Integer hasJoinNum;
}
