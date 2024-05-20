package com.chenxin.player.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fangchenxin
 * @description 通用分页请求参数
 * @date 2024/5/8 11:57
 * @modify
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -7392648011964798346L;

    /**
     * 页面大小
     */
    protected int pageSize = 10;

    /**
     * 当前第几页
     */
    protected int pageNum = 1;
}
