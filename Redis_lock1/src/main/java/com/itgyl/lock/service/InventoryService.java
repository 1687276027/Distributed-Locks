package com.itgyl.lock.service;

import com.itgyl.lock.uitl.DistributedLockFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InventoryService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String port;
    @Resource
    private DistributedLockFactory distributedLockFactory;

    private Lock lock = new ReentrantLock();


    /**
     * V1.0 原始版本
     * 不足：可能出现超卖现象，锁失效（线程安全问题）
     * 在单机环境下，可以使用synchronized或Lock来实现。
     * 但是在分布式系统中，因为竞争的线程可能不在同一个节点上（同一个jvm中），
     * 所以需要一个让所有进程都能访问到的锁来实现(比如redis或者zookeeper来构建)
     * 不同进程jvm层面的锁就不管用了，那么可以利用第三方的一个组件，来获取锁，未获取到锁，则阻塞当前想要运行的线程
     * @return
     */
    /*public String sale() {
        String message = "";
        String key = "lgyLock";
        lock.lock();
        try {
            String result = stringRedisTemplate.opsForValue().get("inventory");
            Integer inventory = result == null ? 0 : Integer.valueOf(result);
            if (inventory > 0) {
                System.out.println("库存数量：" + inventory);
                int num = inventory - 1;
                stringRedisTemplate.opsForValue().set("inventory", String.valueOf(num));
                message = "销售成功,剩余" + num + "件" + ",服务端口号:" + port;
            } else {
                message = "库存不足";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return message;
    }*/


    /**
     * V2.0 改进版本：解决不同线程中锁失效问题
     * 不足：在上锁和解锁的过程中会运行其他业务代码，若该订单模块出现故障（宕机）则会发生死锁现象，该key值一直存在，其他线程无法获取到该锁
     * @return
     */
    /*public String sale() {
        String message = "";
        String key = "lgyLock";
        String uuidValue = key + UUID.randomUUID().toString().replace("-", "") + Thread.currentThread().getId();

        // 分布式锁：
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue)) {
            try {
                // 暂停20毫秒，该锁被去哦他线程获取时会将该线程休眠20毫秒后在尝试获取
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            String result = stringRedisTemplate.opsForValue().get("inventory");
            Integer inventory = result == null ? 0 : Integer.valueOf(result);
            if (inventory > 0) {
                System.out.println("库存数量：" + inventory);
                int num = inventory - 1;
                stringRedisTemplate.opsForValue().set("inventory", String.valueOf(num));
                message = "销售成功,剩余" + num + "件" + ",服务端口号:" + port;
            } else {
                message = "库存不足";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放锁
            stringRedisTemplate.delete(key);
        }
        return message;
    }*/


    /**
     * V3.0 改进版本：预防可能发生死锁现象
     * 不足： 若业务未完成锁到期了会提前释放锁，此时其他线程会进行加锁，在加锁后当前线程完成业务进行释放锁，导致误删其他线程的锁
     * @return
     */
    /*public String sale() {
        String message = "";
        String key = "lgyLock";
        String uuidValue = key + UUID.randomUUID().toString().replace("-", "") + Thread.currentThread().getId();

        // 分布式锁：锁设置自动过期时间
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue, 30L, TimeUnit.SECONDS)) {
            try {
                // 暂停20毫秒，锁被其他线程获取后会将当前线程休眠20毫秒后在尝试获取
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            String result = stringRedisTemplate.opsForValue().get("inventory");
            Integer inventory = result == null ? 0 : Integer.valueOf(result);
            if (inventory > 0) {
                System.out.println("库存数量：" + inventory);
                int num = inventory - 1;
                stringRedisTemplate.opsForValue().set("inventory", String.valueOf(num));
                message = "销售成功,剩余" + num + "件" + ",服务端口号:" + port;
            } else {
                message = "库存不足";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放锁
            stringRedisTemplate.delete(key);
        }
        return message;
    }*/


    /**
     * V4.0 改进版本：解决其他线程误删锁问题
     * @return
     */
    /*public String sale() {
        String message = "";
        String key = "lgyLock";
        String uuidValue = key + UUID.randomUUID().toString().replace("-", "") + Thread.currentThread().getId();

        // 分布式锁：锁设置自动过期时间
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue, 30L, TimeUnit.SECONDS)) {
            try {
                // 暂停20毫秒，锁被其他线程获取后会将当前线程休眠20毫秒后在尝试获取
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            String result = stringRedisTemplate.opsForValue().get("inventory");
            Integer inventory = result == null ? 0 : Integer.valueOf(result);
            if (inventory > 0) {
                System.out.println("库存数量：" + inventory);
                int num = inventory - 1;
                stringRedisTemplate.opsForValue().set("inventory", String.valueOf(num));
                message = "销售成功,剩余" + num + "件" + ",服务端口号:" + port;
            } else {
                message = "库存不足";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放锁：比较要释放的锁和当前线程的锁是否一致，若一致则释放
            if (stringRedisTemplate.opsForValue().get(key).equalsIgnoreCase(uuidValue)) {
                stringRedisTemplate.delete(key);
            }
        }
        return message;
    }*/


    /**
     * V5.0 改进版本：保证执行命令的原子性，使用lua脚本实现
     *  不足：不能兼顾可重复性
     * @return
     */
    /*public String sale() {
        String message = "";
        String key = "lgyLock";
        String uuidValue = key + UUID.randomUUID().toString().replace("-", "") + Thread.currentThread().getId();

        // 分布式锁：锁设置自动过期时间
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue, 30L, TimeUnit.SECONDS)) {
            try {
                // 暂停20毫秒，锁被其他线程获取后会将当前线程休眠20毫秒后在尝试获取
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            String result = stringRedisTemplate.opsForValue().get("inventory");
            Integer inventory = result == null ? 0 : Integer.valueOf(result);
            if (inventory > 0) {
                System.out.println("库存数量：" + inventory);
                int num = inventory - 1;
                stringRedisTemplate.opsForValue().set("inventory", String.valueOf(num));
                message = "销售成功,剩余" + num + "件" + ",服务端口号:" + port;
            } else {
                message = "库存不足";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放锁：比较要释放的锁和当前线程的锁是否一致，若一致则释放
            String luaScript =
                    "if (redis.call('get',KEYS[1]) == ARGV[1]) then " +
                            "return redis.call('del',KEYS[1]) " +
                            "else " +
                            "return 0 " +
                            "end";
            stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript, Boolean.class), Arrays.asList(key), uuidValue);
        }
        return message;
    }*/



    /**
     * V6.0 改进版本：保证锁的可重入性
     * 不足：没有自动续期：如无法确保业务锁过期时间大于业务执行时间
     * @return
     */
    public String sale() {
        String message = "";
        Lock redisLock = distributedLockFactory.getDistributedLock("redis");
        redisLock.lock();

        try {
            String result = stringRedisTemplate.opsForValue().get("inventory");
            Integer inventory = result == null ? 0 : Integer.valueOf(result);
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
            redisLock.unlock();
        }
        return message;
    }
}
