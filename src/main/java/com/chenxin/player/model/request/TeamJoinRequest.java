package com.chenxin.player.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fangchenxin
 * @description
 * @date 2024/5/8 16:39
 * @modify
 */
@Data
public class TeamJoinRequest implements Serializable {
    
    private static final long serialVersionUID = 4809616750747854180L;
    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;


}
