package com.chenxin.player.once;


import com.chenxin.player.model.domain.User;
import com.chenxin.player.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @author fangchenxin
 * @description
 * @date 2024/4/28 18:15
 * @modify
 */
@Component
public class InsertUsers {

    @Resource
    private UserService userService;

    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("test");
            user.setUserAccount("test");
            user.setAvatarUrl("src/public/icons/jfj.jpeg");
            user.setGender(0);
            user.setUserPassword("11111111");
            user.setPhone("111");
            user.setEmail("111@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("11111");
            user.setTags("[]");
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
