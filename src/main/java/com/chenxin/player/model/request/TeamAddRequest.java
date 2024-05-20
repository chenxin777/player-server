package com.chenxin.player.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author fangchenxin
 * @description
 * @date 2024/5/8 16:39
 * @modify
 */
@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 1971879213094121889L;
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
     * 密码
     */
    private String password;

    /**
     * 状态 0公开 1私有 2加密
     */
    private Integer status;

}
