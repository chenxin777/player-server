package com.chenxin.player.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fangchenxin
 * @description
 * @date 2024/5/10 23:40
 * @modify
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = -3935606556784861586L;

    /**
     * id
     */
    private Long id;
}
