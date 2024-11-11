package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 13080
* @description 针对表【user_info】的数据库操作Service
* @createDate 2023-07-31 21:01:13
*/
public interface UserInfoService extends IService<UserInfo> {

    UserInfo getUserInfoById(Integer userId);

    List<UserInfo> getAuthorsByKeyWord(String keyWord);
}
