package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.vo.Role;
import cn.guet.ksmvcmain.entity.vo.User;
import cn.guet.ksmvcmain.entity.vo.UserRole;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.service.UserService;
import cn.guet.ksmvcmain.mapper.UserMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

/**
 * @author 13080
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2023-07-11 20:18:08
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public User login(String userName) {
        MPJLambdaWrapper<User> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .selectAll(User.class)
                .selectCollection(Role.class, User::getRole)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(User::getUserName, userName);
        return baseMapper.selectJoinOne(User.class, wrapper);
    }

}




