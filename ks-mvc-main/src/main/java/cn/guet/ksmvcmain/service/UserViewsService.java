package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.UserViews;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 13080
* @description 针对表【user_views】的数据库操作Service
* @createDate 2023-07-25 21:32:04
*/
public interface UserViewsService extends IService<UserViews> {

    List<UserViews> getUserViewsAll(Integer userId);

}
