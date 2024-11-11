package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 13080
 * @description 针对表【user】的数据库操作Service
 * @createDate 2023-07-11 20:18:08
 */
public interface UserService extends IService<User> {

    User login(String userName);

}
