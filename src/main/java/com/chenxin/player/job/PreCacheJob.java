package com.chenxin.player.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenxin.player.mapper.UserMapper;
import com.chenxin.player.model.domain.User;
import com.chenxin.player.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author fangchenxin
 * @description 预热缓存任务
 * @date 2024/4/30 18:14
 * @modify
 */

@Slf4j
@Component
public class PreCacheJob {
   @Resource
   private UserService userService;

   @Resource
   private RedisTemplate<String, Object> redisTemplate;

   @Resource
   private RedissonClient redissonClient;

   private List<Long> mainUserList = Arrays.asList(1L);

   @Scheduled(cron = "0 18 18 * * *")
   public void doCacheRecommendUser() {
      RLock lock = redissonClient.getLock("player:precachejob:docache:lock");
      try {
         // 只有一个线程获取到锁
         if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
            System.out.println("getLock" + Thread.currentThread().getId());
            // 查用户
            for (Long userId : mainUserList) {
               QueryWrapper<User> queryWrapper = new QueryWrapper<>();
               Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
               String redisKey = String.format("player:user:recommend:%s", userId);
               ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
               try {
                  valueOperations.set(redisKey, userPage);
               } catch (Exception ex) {
                  log.error("redis set key error", ex);
               }
            }
         }
      } catch (InterruptedException e) {
         log.error("doCacheRecommendUser error", e);
      } finally {
         // 只能释放自己的锁
         if (lock.isHeldByCurrentThread()) {
            System.out.println("unlock" + Thread.currentThread().getId());
            lock.unlock();
         }
      }




   }


}
