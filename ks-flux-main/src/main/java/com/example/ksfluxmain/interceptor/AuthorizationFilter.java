package com.example.ksfluxmain.interceptor;

import com.example.kscommon.entity.RedisConstant;
import com.example.kscommon.entity.User;
import com.example.kscommon.utils.JwtUtil;
import com.example.kscommon.utils.UserUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class AuthorizationFilter implements WebFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private final Cache<String, User> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        List<String> list = exchange.getRequest().getHeaders().get("Authorization");
        String token = "";
        if (list != null && !list.isEmpty()) {
            token = list.get(0);
        }
        if (!token.isEmpty() && !token.isBlank()) {
            Claims claims = JwtUtil.parseJWT(token);
            String userId = claims.getSubject();
            String userKey = RedisConstant.USER_INFO_SPECIAL + userId;
            User user = cache.getIfPresent(userKey);
            if (user == null) {
                user = (User) redisTemplate.opsForValue().get(userKey);
            }
            if (user != null) {
                UserUtil.set(user);
            }
        }
        return chain.filter(exchange);
    }
}
