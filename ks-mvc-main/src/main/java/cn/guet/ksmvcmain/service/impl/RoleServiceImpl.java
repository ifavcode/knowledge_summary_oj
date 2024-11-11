package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.vo.Role;
import cn.guet.ksmvcmain.service.RoleService;
import cn.guet.ksmvcmain.mapper.RoleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author 13080
* @description 针对表【role】的数据库操作Service实现
* @createDate 2023-07-11 21:55:54
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService{

}




