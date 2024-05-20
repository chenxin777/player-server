package com.chenxin.player.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author fangchenxin
 * @description 网站用户信息
 * @date 2024/4/26 17:41
 * @modify
 */
@Data
public class WebUserInfo {

    @ExcelProperty("成员编号")
    private Long planetCode;

    @ExcelProperty("成员昵称")
    private String username;
    
}
