package com.chenxin.player.service;

import org.junit.jupiter.api.Test;
import org.redisson.RedissonRedLock;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author fangchenxin
 * @description
 * @date 2024/5/7 16:35
 * @modify
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("fcx");
        System.out.println(rList.get(0));
        rList.remove(0);

        RMap<String, Object> map = redissonClient.getMap("test-map");
        map.put("fcx", 111);
        System.out.println(map.get("fcx"));
    }

    @Test
    void testWatchDog() {
        RedissonRedLock lock = (RedissonRedLock) redissonClient.getLock("player:precachejob:docache:lock");
        try {
            // 只有一个线程获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                Thread.sleep(300000);
                System.out.println("getLock" + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}
