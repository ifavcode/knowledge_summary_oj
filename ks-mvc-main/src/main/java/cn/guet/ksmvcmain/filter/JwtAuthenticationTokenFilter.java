package cn.guet.ksmvcmain.filter;

import cn.guet.ksmvcmain.entity.LoginUser;
import com.example.kscommon.entity.RedisConstant;
import com.example.kscommon.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token == null || "".equals(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        String userId;
        LoginUser loginUser;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = claims.getSubject();
            loginUser = (LoginUser) redisTemplate.opsForValue().get(RedisConstant.USER_INFO + userId);
            if (loginUser == null || loginUser.getUser() == null) {
                SecurityContextHolder.getContext().setAuthentication(null);
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        filterChain.doFilter(request, response);
    }
}
