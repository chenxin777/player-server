package com.chenxin.player.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fangchenxin
 * @description
 * @date 2024/4/26 20:15
 * @modify
 */
public class ImportWebUser {

    public static void main(String[] args) {
        String fileName = "src/main/resources/file/testExcel.xlsx";
        List<WebUserInfo> userInfoList = EasyExcel.read(fileName).head(WebUserInfo.class).sheet().doReadSync();
        System.out.println("总数=" + userInfoList.size());
        Map<String, List<WebUserInfo>> userListMap = userInfoList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getUsername()))
                .collect(Collectors.groupingBy(WebUserInfo::getUsername));
        System.out.println("不重复昵称数=" + userListMap.keySet().size());
        for (Map.Entry<String, List<WebUserInfo>> stringListEntry : userListMap.entrySet()) {
            if (stringListEntry.getValue().size() > 1) {
                System.out.println();
            }
        }
    }


}
