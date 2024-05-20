package com.chenxin.player.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author fangchenxin
 * @description
 * @date 2024/5/9 00:15
 * @modify
 */
@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = -2338387035664255921L;

    /**
     * 队伍id
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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态 0公开 1私有 2加密
     */
    private Integer status;
}
