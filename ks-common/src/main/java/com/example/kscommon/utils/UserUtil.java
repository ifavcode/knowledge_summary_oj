package com.example.kscommon.utils;

import com.example.kscommon.entity.RedisConstant;
import com.example.kscommon.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserUtil {

    private static final ThreadLocal<User> threadLocal = new ThreadLocal<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static void set(User user) {
        threadLocal.set(user);
    }

    public static User get() {
        return threadLocal.get();
    }

    public User getUser(String token) {
        String subject = JwtUtil.parseJWT(token).getSubject();
        if (subject == null || subject.equals("")) {
            return null;
        }
        return (User) redisTemplate.opsForValue().get(RedisConstant.USER_INFO_SPECIAL + subject);
    }
}
