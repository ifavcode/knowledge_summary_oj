package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.vo.Role;
import cn.guet.ksmvcmain.entity.vo.User;
import cn.guet.ksmvcmain.entity.vo.UserRole;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.UserInfo;
import cn.guet.ksmvcmain.service.UserInfoService;
import cn.guet.ksmvcmain.mapper.UserInfoMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 13080
 * @description 针对表【user_info】的数据库操作Service实现
 * @createDate 2023-07-31 21:01:13
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
        implements UserInfoService {

    @Override
    public UserInfo getUserInfoById(Integer userId) {
        MPJLambdaWrapper<UserInfo> wrapper = new MPJLambdaWrapper<>();
        wrapper.rightJoin(User.class, User::getUserId, UserInfo::getUserId)
                .rightJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .rightJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .select(User::getUserId, User::getUserName, User::getNickName, User::getUserDesc, User::getUserAvatar, User::getFinalUpTime)
                .selectAll(UserInfo.class)
                .selectCollection(Role.class, UserInfo::getRoles)
                .eq(User::getUserId, userId);
        return baseMapper.selectJoinOne(UserInfo.class, wrapper);
    }

    @Override
    public List<UserInfo> getAuthorsByKeyWord(String keyWord) {
        MPJLambdaWrapper<UserInfo> wrapper = new MPJLambdaWrapper<>();
        wrapper.rightJoin(User.class, User::getUserId, UserInfo::getUserId)
                .select(User::getUserId, User::getUserName, User::getNickName, User::getUserDesc, User::getUserAvatar, User::getFinalUpTime)
                .selectAll(UserInfo.class)
                .like(User::getNickName, keyWord)
                .or()
                .like(User::getUserDesc, keyWord);
        return baseMapper.selectJoinList(UserInfo.class, wrapper);
    }

}




