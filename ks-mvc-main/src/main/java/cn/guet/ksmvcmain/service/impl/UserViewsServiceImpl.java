package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.ArticlesData;
import cn.guet.ksmvcmain.entity.vo.*;
import cn.guet.ksmvcmain.service.ArticleCommentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.service.UserViewsService;
import cn.guet.ksmvcmain.mapper.UserViewsMapper;
import com.example.kscommon.entity.RedisConstant;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 13080
 * @description 针对表【user_views】的数据库操作Service实现
 * @createDate 2023-07-25 21:32:04
 */
@Service
public class UserViewsServiceImpl extends ServiceImpl<UserViewsMapper, UserViews>
        implements UserViewsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleCommentService articleCommentService;

    public UserViewsServiceImpl(RedisTemplate<String, Object> redisTemplate, ArticleCommentService articleCommentService) {
        this.redisTemplate = redisTemplate;
        this.articleCommentService = articleCommentService;
    }


    @Override
    public List<UserViews> getUserViewsAll(Integer userId) {
        MPJLambdaWrapper<UserViews> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(User.class, User::getUserId, UserViews::getUserId)
                .leftJoin(Articles.class, Articles::getArticleId, UserViews::getArticleId)
                .leftJoin(ArticleTag.class, ArticleTag::getArticleId, Articles::getArticleId)
                .leftJoin(TagInfo.class, TagInfo::getTagId, ArticleTag::getTagId)
                .selectAssociation(User.class, UserViews::getUser)
                .selectAssociation(Articles.class, UserViews::getArticles, v -> {
                    v.collection(TagInfo.class, Articles::getTagInfoList);
                    return v;
                })
                .selectMax(UserViews::getCreateTime, UserViews::getCreateTime)
                .select(UserViews::getUserId)
                .select(UserViews::getViewId)
                .select(UserViews::getArticleId)
                .groupBy(UserViews::getArticleId)
                .orderByDesc(UserViews::getCreateTime)
                .eq(UserViews::getUserId, userId);
        List<UserViews> list = baseMapper.selectJoinList(UserViews.class, wrapper);
        /**
         * todo 分页优化
         */
        for (UserViews userViews : list) {
            if(userViews.getArticles() == null) continue;
            //浏览数
            Long view = redisTemplate.opsForHyperLogLog().size(RedisConstant.ARTICLE_VIEW + userViews.getArticles().getArticleId());
            //认可数
            Long like = redisTemplate.opsForSet().size(RedisConstant.ARTICLE_LIKE + userViews.getArticles().getArticleId());
            //评论数
            long comment = articleCommentService.count(new LambdaQueryWrapper<ArticleComment>().eq(ArticleComment::getArticleId,
                    userViews.getArticles().getArticleId()));
            userViews.getArticles().setArticlesData(new ArticlesData(view, like, comment));
        }
        return list;
    }
}




