package com.chenxin.player.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * @author fangchenxin
 * @description 导入excel数据
 * @date 2024/4/26 17:29
 * @modify
 */
public class ImportExcel {

    /**
     * @description 读取表格
     * @author fangchenxin
     * @date 2024/4/26 18:05
     */
    public static void main(String[] args) {
        String fileName = "src/main/resources/file/testExcel.xlsx";
        synchronousRead(fileName);
    }

    /**
     * @param fileName
     * @description 监听器读
     * @author fangchenxin
     * @date 2024/4/26 20:03
     */
    public static void readByListener(String fileName) {
        EasyExcel.read(fileName, WebUserInfo.class, new TabListener()).sheet().doRead();
    }

    /**
     * @param fileName
     * @description 同步读
     * @author fangchenxin
     * @date 2024/4/26 20:03
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<WebUserInfo> list = EasyExcel.read(fileName).head(WebUserInfo.class).sheet().doReadSync();
        for (WebUserInfo webUserInfo : list) {
            System.out.println(webUserInfo);
        }
    }
}
