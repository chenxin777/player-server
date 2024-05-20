package com.chenxin.player.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fangchenxin
 * @description 注册请求参数
 * @date
 * @modify
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -3371187108794102196L;

    /**
     * 账户
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 校验密码
     */
    private String checkPassword;

    /**
     * 星球编号
     */
    private String planetCode;

}
