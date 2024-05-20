package com.chenxin.player.service;

import com.chenxin.player.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author fangchenxin
 * @description
 * @date 2024/4/29 23:03
 * @modify
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("fcxString", "123");
        valueOperations.set("fcxInt", 11);
        valueOperations.set("fcxDouble", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("fcx");
        valueOperations.set("fcxUser", user);
        // 查
        Object fcxString = valueOperations.get("fcxString");
        System.out.println(fcxString);

        Object fcxInt = valueOperations.get("fcxInt");
        System.out.println(fcxInt);

        Object fcxDouble = valueOperations.get("fcxDouble");
        System.out.println(fcxDouble);

        
    }
}
