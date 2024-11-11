package cn.guet.ksgateway.filter;

import com.alibaba.fastjson2.JSONObject;
import com.example.kscommon.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(1)
public class AuthFilter implements GlobalFilter {
    private final static Set<String> ignoreWhite = new HashSet<>() {{
        add("/bigData/**");
        add("/swagger/**");
        add("/swagger-ui.html");
        add("/webjars/**");
        add("/v3/**");
        add("/v3/api-docs/**");
        add("/swagger-resources/**");
        add("/doc.html");
        add("/user/register");
        add("/user/login");
        add("/articles/all");
        add("/articles/id");
        add("/articles/ids/**");
        add("/articles/comment");
        add("/articles/recommend/all");
        add("/tag/all");
        add("/search/**");
        add("/test/**");
        add("/articles/add");
        add("/question/list");
        add("/question/code");
    }};

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String path = exchange.getRequest().getURI().getPath();
        AntPathMatcher matcher = new AntPathMatcher();
        ServerHttpRequest.Builder mutate = exchange.getRequest().mutate();
        for (String white : ignoreWhite) {
            if (matcher.match("/**" + white, path)) {
                return chain.filter(exchange);
            }
        }
        String token = headers.getFirst("Authorization");
        if (token == null || token.equals(" ")) return writeResponseUnAuthorized(exchange);
        try {
            Claims claims = JwtUtil.parseJWT(token);
            if (claims == null) {
                return writeResponseUnAuthorized(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return writeResponseUnAuthorized(exchange);
        }
        addHeader(mutate, "Authorization", token);
        return chain.filter(exchange);
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = URLEncoder.encode(valueStr, StandardCharsets.UTF_8);
        mutate.header(name, valueEncode);
    }

    private Mono<Void> writeResponseUnAuthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        JSONObject message = new JSONObject();
        message.put("code", 401);
        message.put("message", "鉴权失败");
        byte[] bits = message.toJSONString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }
}
