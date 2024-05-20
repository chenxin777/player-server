package com.chenxin.player.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fangchenxin
 * @description 登陆请求参数
 * @date
 * @modify
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -3213249743057047806L;

    /**
     * 账户
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
