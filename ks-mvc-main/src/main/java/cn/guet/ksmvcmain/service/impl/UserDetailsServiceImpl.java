package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.LoginUser;
import cn.guet.ksmvcmain.entity.vo.Role;
import cn.guet.ksmvcmain.entity.vo.User;
import cn.guet.ksmvcmain.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;

    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.login(username);
        if (Objects.isNull(user)) {
            throw new BadCredentialsException("用户名或者密码错误");
        }
        StringBuilder sb = new StringBuilder();
        for (Role role : user.getRole()) {
            sb.append(role.getRoleAuth()).append(",");
        }
        if(user.getFinalUpTime() == null){
            user.setFinalUpTime(new Date());
        }
        return new LoginUser(user, AuthorityUtils.commaSeparatedStringToAuthorityList(sb.toString()));
    }
}
