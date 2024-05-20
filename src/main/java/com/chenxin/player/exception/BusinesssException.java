package com.chenxin.player.exception;

import com.chenxin.player.common.ErrorCode;
import lombok.Data;

/**
 * @author fangchenxin
 * @description
 * @date 2024/4/20 14:28
 * @modify
 */
@Data
public class BusinesssException extends RuntimeException {

    private int code;
    private String description;

    public BusinesssException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinesssException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        code = errorCode.getCode();
        description = errorCode.getDescription();
    }

    public BusinesssException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        code = errorCode.getCode();
        this.description = description;
    }
}
