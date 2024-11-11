package cn.guet.ksmvcmain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.Languages;
import cn.guet.ksmvcmain.service.LanguagesService;
import cn.guet.ksmvcmain.mapper.LanguagesMapper;
import org.springframework.stereotype.Service;

/**
* @author rog
* @description 针对表【languages】的数据库操作Service实现
* @createDate 2024-11-07 09:39:51
*/
@Service
public class LanguagesServiceImpl extends ServiceImpl<LanguagesMapper, Languages>
    implements LanguagesService{

}




