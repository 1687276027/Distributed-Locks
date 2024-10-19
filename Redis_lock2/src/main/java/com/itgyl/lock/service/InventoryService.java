package com.itgyl.lock.service;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InventoryService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String port;
    @Resource
    private Redisson redisson;

    private Lock lock = new ReentrantLock();


    public String sale() {
        String message = "";
        String key = "lgyRedisLock";
        RLock redissonLock = redisson.getLock(key);
        redissonLock.lock();
        try {
            Integer inventory = Integer.valueOf(stringRedisTemplate.opsForValue().get("inventory"));
            if (inventory > 0) {
                int num = inventory - 1;
                stringRedisTemplate.opsForValue().set("inventory", String.valueOf(num));
                message = "销售成功,剩余" + num + "件" + ",服务端口号:" + port;
                System.out.println(message);
            } else {
                message = "库存不足";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            redissonLock.unlock();
        }
        return message;
    }
}
