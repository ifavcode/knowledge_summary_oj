package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.vo.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.ArticleComment;
import cn.guet.ksmvcmain.service.ArticleCommentService;
import cn.guet.ksmvcmain.mapper.ArticleCommentMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 13080
 * @description 针对表【article_comment】的数据库操作Service实现
 * @createDate 2023-07-22 20:51:59
 */
@Service
public class ArticleCommentServiceImpl extends ServiceImpl<ArticleCommentMapper, ArticleComment>
        implements ArticleCommentService {

    @Override
    public List<ArticleComment> getAllComment(Integer articleId) {
        MPJLambdaWrapper<ArticleComment> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(User.class, "u1", User::getUserId, ArticleComment::getUserId)
                .leftJoin(User.class, "u2", User::getUserId, ArticleComment::getTargetUserId)
                .selectAssociation("u1", User.class, ArticleComment::getUser)
                .selectAssociation("u2", User.class, ArticleComment::getTargetUser)
                .eq(ArticleComment::getArticleId, articleId);
        return baseMapper.selectJoinList(ArticleComment.class, wrapper);
    }

}




