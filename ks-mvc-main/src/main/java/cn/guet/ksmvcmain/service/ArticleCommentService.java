package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.ArticleComment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 13080
* @description 针对表【article_comment】的数据库操作Service
* @createDate 2023-07-22 20:51:59
*/
public interface ArticleCommentService extends IService<ArticleComment> {

    List<ArticleComment> getAllComment(Integer articleId);
}
