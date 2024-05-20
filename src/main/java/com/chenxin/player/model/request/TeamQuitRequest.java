package com.chenxin.player.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fangchenxin
 * @description 退出队伍请求体
 * @date 2024/5/9 15:15
 * @modify
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -8281068182256484162L;

    /**
     * 队伍id
     */
    private Long teamId;
}
