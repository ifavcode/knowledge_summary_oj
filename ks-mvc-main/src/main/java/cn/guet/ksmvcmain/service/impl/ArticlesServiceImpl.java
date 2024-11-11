package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.ArticlesData;
import cn.guet.ksmvcmain.entity.vo.*;
import cn.guet.ksmvcmain.service.ArticleCommentService;
import cn.guet.ksmvcmain.service.UserViewsService;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.service.ArticlesService;
import cn.guet.ksmvcmain.mapper.ArticlesMapper;
import com.example.kscommon.entity.RedisConstant;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author 13080
 * @description 针对表【articles】的数据库操作Service实现
 * @createDate 2023-07-20 16:33:40
 */
@Service
@Slf4j
public class ArticlesServiceImpl extends ServiceImpl<ArticlesMapper, Articles>
        implements ArticlesService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleCommentService articleCommentService;
    private final UserViewsService userViewsService;

    public ArticlesServiceImpl(RedisTemplate<String, Object> redisTemplate, ArticleCommentService articleCommentService, UserViewsService userViewsService) {
        this.redisTemplate = redisTemplate;
        this.articleCommentService = articleCommentService;
        this.userViewsService = userViewsService;
    }

    @Override
    public List<Articles> getAllArticles(Integer pageNum, Integer pageSize) {
        MPJLambdaWrapper<Articles> wrapper = baseJoinWrapper();
        wrapper.orderByDesc(Articles::getCreateTime);
        Page<Articles> page = new Page<>(pageNum, pageSize);
        page.setOptimizeJoinOfCountSql(false);
        List<Articles> list = baseMapper.selectJoinPage(page, Articles.class, wrapper).getRecords();
        for (Articles articles : list) {
            Long view = redisTemplate.opsForHyperLogLog().size(RedisConstant.ARTICLE_VIEW + articles.getArticleId());
            Long like = redisTemplate.opsForSet().size(RedisConstant.ARTICLE_LIKE + articles.getArticleId());
            long comment = articleCommentService.count(new LambdaQueryWrapper<ArticleComment>().eq(ArticleComment::getArticleId, articles.getArticleId()));
            articles.setArticlesData(new ArticlesData(view, like, comment));
        }
        return list;
    }

    @Override
    public Articles getArticleById(Integer id) {
        MPJLambdaWrapper<Articles> wrapper = baseJoinWrapper();
        wrapper.eq(Articles::getArticleId, id);
        //增加浏览量 (每分钟)
        Long add = redisTemplate.opsForHyperLogLog().add(RedisConstant.ARTICLE_VIEW + id, getFormatDate() + " " + SecurityUtil.getUserId());
        Long view = redisTemplate.opsForHyperLogLog().size(RedisConstant.ARTICLE_VIEW + id);
        //认可数
        Long like = redisTemplate.opsForSet().size(RedisConstant.ARTICLE_LIKE + id);
        //评论数
        long comment = articleCommentService.count(new LambdaQueryWrapper<ArticleComment>().eq(ArticleComment::getArticleId, id));
        //添加用户浏览记录  新增
        if (SecurityUtil.getUserId() != null && add > 0) {
            userViewsService.save(new UserViews(null, SecurityUtil.getUserId(), id, new Date(), null, null, null));
        }
        Articles articles = baseMapper.selectJoinOne(Articles.class, wrapper);
        articles.setArticlesData(new ArticlesData(view, like, comment));
        return articles;
    }

    @Override
    public List<Articles> getUserLikes(List<Integer> ids) {
        if (ids.size() == 0) return new ArrayList<>();
        MPJLambdaWrapper<Articles> wrapper = baseJoinWrapper();
        wrapper.in(Articles::getArticleId, ids);
        List<Articles> list = baseMapper.selectJoinList(Articles.class, wrapper);
        for (Articles articles : list) {
            Integer id = articles.getArticleId();
            Long view = redisTemplate.opsForHyperLogLog().size(RedisConstant.ARTICLE_VIEW + id);
            //认可数
            Long like = redisTemplate.opsForSet().size(RedisConstant.ARTICLE_LIKE + id);
            //评论数
            long comment = articleCommentService.count(new LambdaQueryWrapper<ArticleComment>().eq(ArticleComment::getArticleId, id));
            articles.setArticlesData(new ArticlesData(view, like, comment));
        }
        return list;
    }

    @Override
    public List<Integer> getUserPublishArticlesId(Integer userId) {
        return baseMapper.getUserPublishArticlesId(userId);
    }

    @Override
    public List<Articles> getArticleByIds(List<Integer> ids, Integer pageNum, Integer pageSize, String type, Character flag) {
        if (ids.size() == 0 || pageSize > 30) return new ArrayList<>();
        MPJLambdaWrapper<Articles> wrapper = baseJoinWrapper();
        if (type.equals("time")) {
            wrapper.orderByDesc(Articles::getCreateTime);
        }
        if (flag == '1') {
            //获取不置顶接口获取
            wrapper.eq(Articles::isPinned, false);
        } else if (flag == '2') {
            //获取置顶接口获取
            wrapper.eq(Articles::isPinned, true);
        }
        wrapper.in(Articles::getArticleId, ids);
        Page<Articles> page = new Page<>(pageNum, pageSize);
        page.setOptimizeJoinOfCountSql(false);
        List<Articles> list = baseMapper.selectJoinPage(page, Articles.class, wrapper).getRecords();
        for (Articles articles : list) {
            Long view = redisTemplate.opsForHyperLogLog().size(RedisConstant.ARTICLE_VIEW + articles.getArticleId());
            Long like = redisTemplate.opsForSet().size(RedisConstant.ARTICLE_LIKE + articles.getArticleId());
            long comment = articleCommentService.count(new LambdaQueryWrapper<ArticleComment>().eq(ArticleComment::getArticleId, articles.getArticleId()));
            articles.setArticlesData(new ArticlesData(view, like, comment));
        }
        switch (type) {
            case "love" ->
                    list.sort((o1, o2) -> Math.toIntExact(o2.getArticlesData().getLike() - o1.getArticlesData().getLike()));
            case "views" ->
                    list.sort((o1, o2) -> Math.toIntExact(o2.getArticlesData().getView() - o1.getArticlesData().getView()));
        }
        return list;
    }

    @Override
    public List<Articles> getArticleByUserId(Integer userId, Integer pageNum, Integer pageSize, String type,Character flag) {
        //根据用户id拿文章集合
        List<Integer> list = getUserPublishArticlesId(userId);
        return getArticleByIds(list, pageNum, pageSize, type, flag);
    }

    @Override
    public List<Articles> getRecommendArticles() {
        Calendar instance = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String today = format.format(instance.getTime());
        instance.add(Calendar.DAY_OF_MONTH, -7);
        String preDay = format.format(instance.getTime());
        MPJLambdaWrapper<Articles> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(User.class, User::getUserId, Articles::getUserId)
                .leftJoin(UserViews.class, UserViews::getArticleId, Articles::getArticleId)
                .select(Articles::getArticleId, Articles::getArticleTitle, Articles::getArticleImage,
                        Articles::getCreateTime, Articles::getRawText, Articles::getUserId)
                .selectCount(Articles::getArticleId, "searchCnt")
                .selectAssociation(User.class, Articles::getPublishUser)
                .groupBy(UserViews::getArticleId)
                .between(UserViews::getCreateTime, preDay, today)
                .and(v -> v.ne(Articles::getArticleImage, "").isNotNull(Articles::getArticleImage))
                .orderByDesc("searchCnt")
                .last("limit 10");
        List<Articles> list = baseMapper.selectJoinList(Articles.class, wrapper);
        for (Articles articles : list) {
            Long view = redisTemplate.opsForHyperLogLog().size(RedisConstant.ARTICLE_VIEW + articles.getArticleId());
            Long like = redisTemplate.opsForSet().size(RedisConstant.ARTICLE_LIKE + articles.getArticleId());
            long comment = articleCommentService.count(new LambdaQueryWrapper<ArticleComment>().eq(ArticleComment::getArticleId, articles.getArticleId()));
            articles.setArticlesData(new ArticlesData(view, like, comment));
        }
        return list;
    }

    @Override
    public List<Articles> getUserArticlesByKeyWord(Integer userId, String keyWord, Integer pageNum, Integer pageSize) {
        MPJLambdaWrapper<Articles> wrapper = baseJoinWrapper();
        wrapper.eq(Articles::getUserId, userId)
                .and(v -> {
                    v.like(Articles::getArticleTitle, keyWord)
                            .or()
                            .like(Articles::getRawText, keyWord)
                            .or()
                            .like(TagInfo::getTagName, keyWord);
                })
                .orderByDesc(Articles::getCreateTime);
        List<Articles> list = baseMapper.selectJoinPage(new Page<>(pageNum, pageSize), Articles.class, wrapper).getRecords();
        for (Articles articles : list) {
            articles.setArticleContent("请去详情页查看");
        }
        return list;
    }

    private MPJLambdaWrapper<Articles> baseJoinWrapper() {
        MPJLambdaWrapper<Articles> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(ArticleTag.class, ArticleTag::getArticleId, Articles::getArticleId)
                .leftJoin(TagInfo.class, TagInfo::getTagId, ArticleTag::getTagId)
                .leftJoin(User.class, User::getUserId, Articles::getUserId)
                .leftJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .selectAssociation(User.class, Articles::getPublishUser, v -> {
                    v.collection(Role.class, User::getRole);
                    return v;
                })
                .selectCollection(TagInfo.class, Articles::getTagInfoList)
                .selectAll(Articles.class);
        return wrapper;
    }

    private String getFormatDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return format.format(new Date());
    }

}




