package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.vo.UserRole;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.service.UserRoleService;
import cn.guet.ksmvcmain.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

/**
* @author 13080
* @description 针对表【user_role】的数据库操作Service实现
* @createDate 2023-07-11 21:53:48
*/
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole>
    implements UserRoleService{

}




