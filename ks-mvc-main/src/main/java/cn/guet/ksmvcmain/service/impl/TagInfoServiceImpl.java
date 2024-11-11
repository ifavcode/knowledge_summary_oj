package cn.guet.ksmvcmain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.TagInfo;
import cn.guet.ksmvcmain.service.TagInfoService;
import cn.guet.ksmvcmain.mapper.TagInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author 13080
* @description 针对表【tag_info】的数据库操作Service实现
* @createDate 2023-07-20 16:33:46
*/
@Service
public class TagInfoServiceImpl extends ServiceImpl<TagInfoMapper, TagInfo>
    implements TagInfoService{

}




