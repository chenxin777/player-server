package com.chenxin.player.service;

import com.chenxin.player.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author fangchenxin
 * @description
 * @date 2024/4/29 00:03
 * @modify
 */
@SpringBootTest
public class InsertUserTest {

    @Resource
    private UserService userService;

    private static ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;

        List<User> userList = new ArrayList<>();
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
            userList.add(user);
        }
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println("用时：" + stopWatch.getTotalTimeMillis());
    }

    @Test
    public void doConcurrentInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
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
                userList.add(user);
                if (j % 10000 == 0) {
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName--" + Thread.currentThread().getName());
                userService.saveBatch(userList, 10000);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println("用时：" + stopWatch.getTotalTimeMillis());
    }


}
