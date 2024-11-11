package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.entity.QdComment;
import cn.guet.ksmvcmain.entity.vo.*;
import cn.guet.ksmvcmain.service.*;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kscommon.entity.AjaxResult;
import com.example.kscommon.entity.RedisConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/articles")
@Tag(name = "文章类")
public class ArticlesController {

    private final ArticlesService service;
    private final ArticleTagService articleTagService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;
    private final ArticleCommentService articleCommentService;
    private final UserViewsService userViewsService;

    public ArticlesController(ArticlesService service, ArticleTagService articleTagService, RedisTemplate<String, Object> redisTemplate, UserService userService, ArticleCommentService articleCommentService, UserViewsService userViewsService) {
        this.service = service;
        this.articleTagService = articleTagService;
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.articleCommentService = articleCommentService;
        this.userViewsService = userViewsService;
    }

    @GetMapping("/all")
    @Operation(summary = "获取全部")
    public AjaxResult getAll(@RequestParam(required = false, defaultValue = "1") Integer pageNum,
                             @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return AjaxResult.success(service.getAllArticles(pageNum, pageSize));
    }

    @GetMapping("/id")
    @Operation(summary = "根据id获取")
    public AjaxResult getById(@RequestParam Integer articleId) {
        return AjaxResult.success(service.getArticleById(articleId));
    }

    @PostMapping("/add")
    @Operation(summary = "发布文章")
    public boolean add(@RequestBody Articles articles) {
        if (StringUtils.isEmpty(articles.getArticleType())) {
            articles.setArticleType("markdown");
        }
        articles.setUserId(SecurityUtil.getUserId());
        articles.setCreateTime(new Date());
        boolean res1 = service.save(articles);
        //保存所有标签
        if (articles.getTags() != null) {
            for (Integer tagId : articles.getTags()) {
                articleTagService.save(new ArticleTag(tagId, articles.getArticleId()));
            }
        }
        return res1;
    }

    @PostMapping("/update")
    @Operation(summary = "修改文章")
    @Transactional
    public boolean update(@RequestBody Articles articles) {
        if (articles.getArticleId() == null) return false;
        Articles one = service.getOne(new LambdaQueryWrapper<Articles>().eq(Articles::getArticleId, articles.getArticleId()));
        if (!SecurityUtil.getLoginUser().getPermission().contains(new SimpleGrantedAuthority("*")) &&
                !SecurityUtil.getLoginUser().getPermission().contains(new SimpleGrantedAuthority("edit:user:article")) &&
                !Objects.equals(one.getUserId(), SecurityUtil.getUserId())) {
            return false;
        }
        articles.setUserId(one.getUserId());
        //先删掉全部标签
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articles.getArticleId()));
        //保存所有标签
        for (Integer tagId : articles.getTags()) {
            articleTagService.save(new ArticleTag(tagId, articles.getArticleId()));
        }
        //保存文章
        return service.updateById(articles);
    }

    @PostMapping("/like/{id}")
    @Operation(summary = "认可(不认可)文章")
    public boolean likeArticle(@PathVariable Integer id) {
        if (isLike(id)) {
            redisTemplate.opsForSet().remove(RedisConstant.ARTICLE_LIKE + id, SecurityUtil.getUserId());
            redisTemplate.opsForSet().remove(RedisConstant.USER_LIKE_ARTICLE + SecurityUtil.getUserId(), id);
        } else {
            redisTemplate.opsForSet().add(RedisConstant.ARTICLE_LIKE + id, SecurityUtil.getUserId());
            redisTemplate.opsForSet().add(RedisConstant.USER_LIKE_ARTICLE + SecurityUtil.getUserId(), id);
        }
        return true;
    }

    @GetMapping("/like/user")
    @Operation(summary = "认可文章的用户")
    public AjaxResult getArticlesLikeUser(@RequestParam Integer articleId) {
        Set<Integer> members = Objects.requireNonNull(redisTemplate.opsForSet().members(RedisConstant.ARTICLE_LIKE + articleId)).stream()
                .map(v -> Integer.valueOf(v.toString())).collect(Collectors.toSet());
        if (members.size() > 0) {
            List<User> users = userService.listByIds(members);
            return AjaxResult.success(users);
        }
        return AjaxResult.success(Collections.emptyList());
    }

    private boolean isLike(Integer id) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(RedisConstant.ARTICLE_LIKE + id, SecurityUtil.getUserId()));
    }

    @GetMapping("/comment")
    @Operation(summary = "文章评论列表")
    public AjaxResult getArticlesComments(@RequestParam Integer articleId) {
        LambdaQueryWrapper<ArticleComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleComment::getArticleId, articleId);
        List<ArticleComment> list = articleCommentService.getAllComment(articleId);
        Map<Integer, QdComment> map = new HashMap<>();
        List<QdComment> ans = new ArrayList<>();
        for (ArticleComment ac : list) {
            map.put(ac.getCommentId(), new QdComment(ac.getCommentId(), ac.getCommentContent(), ac.getCreateTime(), ac.getUpdateTime(),
                    ac.getCommentImg(), ac.getUserId(), ac.getTargetUserId(), ac.getOucId(), ac.getUser(), ac.getTargetUser(), new ArrayList<>()));
        }
        for (Map.Entry<Integer, QdComment> entry : map.entrySet()) {
            if (entry.getValue().getOucId() == -1) {
                ans.add(entry.getValue());
            } else {
                map.get(entry.getValue().getOucId()).getChildrenComment().add(entry.getValue());
            }
        }
        return AjaxResult.success(ans);
    }

    @PostMapping("/comment/send")
    @Operation(summary = "发布评论")
    public AjaxResult sendComment(@RequestBody @Validated ArticleComment articleComment) {
        articleComment.setCreateTime(new Date());
        articleComment.setUpdateTime(new Date());
        articleComment.setUserId(SecurityUtil.getUserId());
        boolean save = articleCommentService.save(articleComment);
        if (save) {
            return AjaxResult.success(articleComment);
        } else {
            return AjaxResult.error();
        }
    }

    @PostMapping("/delete/{id}")
    @Transactional
    @Operation(summary = "删除文章")
    public boolean deleteArticles(@PathVariable Integer id) {
        Articles articles = service.getById(id);
        if (!Objects.equals(articles.getUserId(), SecurityUtil.getUserId())) return false;
        service.removeById(id);
        articleCommentService.remove(new LambdaQueryWrapper<ArticleComment>().eq(ArticleComment::getArticleId, id));
        userViewsService.remove(new LambdaQueryWrapper<UserViews>().eq(UserViews::getArticleId, id));
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        redisTemplate.delete(RedisConstant.ARTICLE_LIKE + id);
        redisTemplate.delete(RedisConstant.ARTICLE_LIKE + id);
        return true;
    }

    @GetMapping("/ids/{ids}")
    @Operation(summary = "根据ids搜索分页查询")
    public AjaxResult getArticleByIds(@PathVariable List<Integer> ids,
                                      @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                      @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false, defaultValue = "") String type) {
        return AjaxResult.success(service.getArticleByIds(ids, pageNum, pageSize, type, '0'));
    }

    @GetMapping("/userPublish/{userId}")
    @Operation(summary = "拿某个用户发布的文章")
    public AjaxResult getUserPublishArticlesId(@PathVariable Integer userId,
                                               @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                               @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false, defaultValue = "") String type,
                                               @RequestParam(required = false, defaultValue = "1") Character flag) {
        List<Articles> list = service.getArticleByUserId(userId, pageNum, pageSize, type, flag);
        for (Articles articles : list) {
            articles.setArticleContent("请去详情页查看...");
        }
        return AjaxResult.success(list);
    }

    @GetMapping("/recommend/all")
    @Operation(summary = "获取推荐文章")
    public AjaxResult getRecommendArticles() {
        return AjaxResult.success(service.getRecommendArticles());
    }

    @PostMapping("/pinned/{articleId}/{flag}")
    @Operation(summary = "(取消)置顶文章")
    public AjaxResult pinnedArticle(@PathVariable Integer articleId, @PathVariable boolean flag) {
        Articles articles = new Articles();
        articles.setArticleId(articleId);
        if (flag) {
            long count = service.count(new LambdaQueryWrapper<Articles>()
                    .eq(Articles::getArticleId, SecurityUtil.getUserId())
                    .eq(Articles::isPinned, true));
            if (count >= 30) return AjaxResult.error("最多置顶30条文章哦");
            articles.setPinned(true);
            boolean res = service.updateById(articles);
            if (!res) return AjaxResult.error("发生了一点错误...");
            return AjaxResult.success("置顶成功");
        } else {
            articles.setPinned(false);
            boolean res = service.updateById(articles);
            if (!res) return AjaxResult.error("发生了一点错误...");
            return AjaxResult.success("取消置顶成功");
        }
    }

}
