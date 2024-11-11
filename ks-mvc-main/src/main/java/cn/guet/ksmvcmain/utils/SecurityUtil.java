package cn.guet.ksmvcmain.utils;

import cn.guet.ksmvcmain.entity.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static LoginUser getLoginUser() {
        try {
            Object principal = getAuthentication().getPrincipal();
            return (LoginUser) principal;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer getUserId() {
        try {
            return getLoginUser().getUser().getUserId();
        } catch (Exception e) {
        }
        //默认为AI助手输出
        return 21;
    }

    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
        }
        return "error";
    }
}
