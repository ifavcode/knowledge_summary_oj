package cn.guet.ksmvcmain.exception.handle;

import com.alibaba.fastjson2.JSON;
import com.example.kscommon.entity.AjaxResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotAuthHandle implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        AjaxResult result = new AjaxResult(HttpStatus.FORBIDDEN.value(), "user not auth");
        response.getWriter().print(JSON.toJSONString(result));
    }
}
