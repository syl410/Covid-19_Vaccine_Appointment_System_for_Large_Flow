package com.pharmacy.appointment.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class RedisService {
    @Autowired
    private JedisPool jedisPool;

    /**
     * add limited user
     * @param venueId
     * @param userId
     */
    public void addLimitedUser(long venueId, long userId) {
        Jedis client = jedisPool.getResource();
        client.sadd("venue_users:" + venueId, String.valueOf(userId));
        client.close();
    }

    /**
     * check if user is in the limited user
     * @param venueId
     * @param userId
     * @return
     */
    public boolean isInLimitedUser(long venueId, long userId) {
        Jedis client = jedisPool.getResource();
        boolean isMember = client.sismember("venue_users:" + venueId, String.valueOf(userId));
        client.close();
        log.info("userId:" + venueId + "  venueId:" + userId + "  in the list of limited user:" + isMember);
        return isMember;
    }

    /**
     * remove limited user
     * @param venueId
     * @param userId
     */
    public void removeLimitedUser(long venueId, long userId) {
        Jedis client = jedisPool.getResource();
        client.srem("venue_users:" + venueId, String.valueOf(userId));
        client.close();
    }

    /**
     * time out for unconfirmed appointment; revert spot
     * @param key
     */
    public void revertSpot(String key) {
        Jedis client = jedisPool.getResource();
        client.incr(key);
        client.close();
    }

    // set Long value
    public void setValue(String key, Long value) {
        Jedis client = jedisPool.getResource();
        client.set(key, value.toString());
        client.close();
    }

    // set Integer value
    public void setValue(String key, Integer value) {
        Jedis client = jedisPool.getResource();
        client.set(key, value.toString());
        client.close();
    }

    // set String value
    public void setValue(String key, String value) {
        Jedis client = jedisPool.getResource();
        client.set(key, value);
        client.close();
    }

    public String getValue(String key) {
        Jedis client = jedisPool.getResource();
        String value = client.get(key);
        client.close();
        return value;
    }

    /**
     * check spot in the Redis and deduct spot
     * @param key
     * @return
     * @throws Exception
     */
    public boolean spotDeductValidator(String key) {
        try(Jedis client = jedisPool.getResource()) {
            String script = "if redis.call('exists',KEYS[1]) == 1 then\n" +
                    "                 local spot = tonumber(redis.call('get', KEYS[1]))\n" +
                    "                 if( spot <=0 ) then\n" +
                    "                    return -1\n" +
                    "                 end;\n" +
                    "                 redis.call('decr',KEYS[1]);\n" +
                    "                 return spot - 1;\n" +
                    "             end;\n" +
                    "             return -1;";

            Long spot = (Long)client.eval(script, Collections.singletonList(key), Collections.emptyList()); // return Object, it was a Long
            if (spot < 0) {
                System.out.println("failed to get the spot.");
                return false;
            } else {
                System.out.println("got one appointment spot.");
            }
            return true;
        } catch (Throwable throwable) {
            System.out.println("Failed to deduct spot：" + throwable.toString());
            return false;
        }
    }

    /**
     * get distributed lock
     * @param key
     * @param reqId
     * @param expTime // expireTime
     * @return
     */
    public boolean getDistributedLock(String key, String reqId, int expTime) {
        Jedis client = jedisPool.getResource();
        String result = client.set(key, reqId, "NX", "PX", expTime);
        client.close();
        if ("OK".equals(result)) return true;
        return false;
    }

    /**
     * release distributed lock
     * @param key
     * @param reqId
     * @return true/false
     */
    public boolean releaseDistributedLock(String key, String reqId) {
        Jedis client = jedisPool.getResource();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = (Long) client.eval(script, Collections.singletonList(key), Collections.singletonList(reqId));
        client.close();
        if (result == 1L) return true;
        return false;
    }
}
