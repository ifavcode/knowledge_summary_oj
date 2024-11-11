package cn.guet.ksmvcmain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.ArticleTag;
import cn.guet.ksmvcmain.service.ArticleTagService;
import cn.guet.ksmvcmain.mapper.ArticleTagMapper;
import org.springframework.stereotype.Service;

/**
* @author 13080
* @description 针对表【article_tag】的数据库操作Service实现
* @createDate 2023-07-20 16:33:34
*/
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag>
    implements ArticleTagService{

}




