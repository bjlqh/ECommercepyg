package com.lqh.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class deleteCart {

    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void deleteCart() {
        /*redisTemplate.delete("CDFC14D695D66F5AC7A6FD6A0D34A67B");
        redisTemplate.delete("lqh");*/
    }
    @Test
    public void getValues() {

    }
}
