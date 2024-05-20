package com.chenxin.player.model.dto;

import com.chenxin.player.model.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author fangchenxin
 * @description 队伍查询封装类
 * @date 2024/5/8 11:12
 * @modify
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {

    /**
     * 队伍id
     */
    private Long id;

    /**
     * 队伍id列表
     */
    private List<Long> idList;

    /**
     * 搜索词，同时对队伍名称和描述搜索
     */
    private String searchText;

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
     * 状态 0公开 1私有 2加密
     */
    private Integer status;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 查询类型
     */
    private Integer queryType;
}
