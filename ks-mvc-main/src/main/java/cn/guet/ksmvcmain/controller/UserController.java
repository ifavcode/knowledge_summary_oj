package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.entity.LoginUser;
import cn.guet.ksmvcmain.entity.ViewStatistics;
import cn.guet.ksmvcmain.entity.vo.*;
import cn.guet.ksmvcmain.service.*;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import cn.guet.ksmvcmain.utils.UploadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kscommon.entity.AjaxResult;
import com.example.kscommon.entity.RedisConstant;
import com.example.kscommon.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Tag(name = "用户类")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final UserRoleService userRoleService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserViewsService userViewsService;
    private final ArticlesService articlesService;
    private final UserInfoService userInfoService;
    private final UploadUtil uploadUtil;
    private final ArticleCommentService articleCommentService;

    public UserController(UserService userService, BCryptPasswordEncoder encoder, AuthenticationManager authenticationManager,
                          UserRoleService userRoleService, RedisTemplate<String, Object> redisTemplate, UserViewsService userViewsService,
                          ArticlesService articlesService, UserInfoService userInfoService, UploadUtil uploadUtil, ArticleCommentService articleCommentService) {
        this.userService = userService;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.userRoleService = userRoleService;
        this.redisTemplate = redisTemplate;
        this.userViewsService = userViewsService;
        this.articlesService = articlesService;
        this.userInfoService = userInfoService;
        this.uploadUtil = uploadUtil;
        this.articleCommentService = articleCommentService;
    }

    TreeMap<Integer, String> colorsTreeMap = new TreeMap<>();

    @PostConstruct
    private void initColorsMap() {
        colorsTreeMap.put(1, "#A5FFBE");
        colorsTreeMap.put(6, "#54EF75");
        colorsTreeMap.put(12, "#32B948");
        colorsTreeMap.put(Integer.MAX_VALUE, "#259138");
    }

    @Operation(summary = "注册用户")
    @PostMapping("/register")
    public AjaxResult registerUser(@RequestBody @Validated User user) {
        long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getUserName, user.getUserName()));
        if (count > 0) {
            return AjaxResult.error("用户名已存在");
        }
        user.setCreateTime(new Date());
        user.setPassword(encoder.encode(user.getPassword()));
        boolean res = userService.save(user) &&
                userRoleService.save(new UserRole(user.getUserId(), 2));
        return AjaxResult.success("注册成功，快去登陆吧~");
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public AjaxResult loginUser(@RequestBody @Validated User user) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if (!authenticate.isAuthenticated()) {
            throw new InvalidCredentialsException("用户名或者密码错误");
        }
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        long expirationTime = 60 * 60 * 24L * 7;//秒 七天过期
        Integer userId = loginUser.getUser().getUserId();
        redisTemplate.opsForValue().set(RedisConstant.USER_INFO + userId, loginUser, expirationTime, TimeUnit.SECONDS);

        //复制用户信息
        com.example.kscommon.entity.User copyUser = new com.example.kscommon.entity.User();
        BeanUtils.copyProperties(loginUser.getUser(), copyUser);
        List<com.example.kscommon.entity.Role> copyRoles = new ArrayList<>();
        for (Role role : loginUser.getUser().getRole()) {
            com.example.kscommon.entity.Role copyRole = new com.example.kscommon.entity.Role();
            BeanUtils.copyProperties(role, copyRole);
            copyRoles.add(copyRole);
        }
        copyUser.setRole(copyRoles);

        redisTemplate.opsForValue().set(RedisConstant.USER_INFO_SPECIAL + userId, copyUser, expirationTime, TimeUnit.SECONDS);
        String jwt = JwtUtil.createJWT(userId, expirationTime);
        var map = new HashMap<String, Object>();
        map.put("authorization", jwt);
        //过期天数
        map.put("expirationTime", expirationTime / (60 * 60 * 24L));
        map.put("user", loginUser.getUser());
        //更新最后一次登陆时间
        User tmpUser = new User();
        tmpUser.setUserId(userId);
        tmpUser.setFinalUpTime(new Date());
        userService.updateById(tmpUser);
        return AjaxResult.success(map);
    }

    @Operation(summary = "个人信息")
    @GetMapping("/info")
    public AjaxResult getUserInfo() {
        return AjaxResult.success(SecurityUtil.getLoginUser().getUser());
    }

    @Operation(summary = "个人信息（根据id）")
    @GetMapping("/info/{userId}")
    public AjaxResult getUserInfoById(@PathVariable Integer userId) {
        return AjaxResult.success(userInfoService.getUserInfoById(userId));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public boolean userLogout() {
        redisTemplate.delete(RedisConstant.USER_INFO + SecurityUtil.getUserId());
        redisTemplate.delete(RedisConstant.USER_INFO_SPECIAL + SecurityUtil.getUserId());
        return true;
    }

    @GetMapping("/views")
    @Operation(summary = "用户浏览记录")
    public AjaxResult getViewsAll(@RequestParam(required = false) Integer userId) {
        if (userId == null) {
            userId = SecurityUtil.getUserId();
        }
        List<UserViews> list = userViewsService.getUserViewsAll(userId);
        for (UserViews userViews : list) {
            if (userViews.getArticles() == null) continue;
            Boolean isLike = redisTemplate.opsForSet().isMember(RedisConstant.ARTICLE_LIKE + userViews.getArticles().getArticleId(), userId);
            userViews.getArticles().setIsLike(isLike);
            userViews.getArticles().setArticleContent("请去详情页查看!!!");
        }
        return AjaxResult.success(list);
    }

    @GetMapping("/likes")
    @Operation(summary = "用户认可列表")
    public AjaxResult getLikesAll(@RequestParam(required = false) Integer userId) {
        if (userId == null) {
            userId = SecurityUtil.getUserId();
        }
        List<Integer> list = Objects.requireNonNull(redisTemplate.opsForSet().members(RedisConstant.USER_LIKE_ARTICLE + userId))
                .stream().map(v -> Integer.valueOf(v.toString())).toList();
        List<Articles> userLikes = articlesService.getUserLikes(list);
        for (Articles articles : userLikes) {
            Boolean isLike = redisTemplate.opsForSet().isMember(RedisConstant.ARTICLE_LIKE + articles.getArticleId(), userId);
            articles.setIsLike(isLike);
            articles.setArticleContent("请去详情页查看!!!");
        }
        return AjaxResult.success(userLikes);
    }

    @GetMapping("/articles/data")
    @Operation(summary = "获取浏览量,认可量,评论量")
    public AjaxResult getVLCsData(@RequestParam(required = false) Integer userId) {
        if (userId == null) userId = SecurityUtil.getUserId();
        long views = userViewsService.count(new LambdaQueryWrapper<UserViews>().eq(UserViews::getUserId, userId));
        long likes = Objects.requireNonNull(redisTemplate.opsForSet().members(RedisConstant.USER_LIKE_ARTICLE + userId))
                .stream().map(v -> Integer.valueOf(v.toString())).count();
        long comments = articleCommentService.count(new LambdaQueryWrapper<ArticleComment>().eq(ArticleComment::getUserId, userId));
        var map = new HashMap<String, Object>() {{
            put("views", views);
            put("likes", likes);
            put("comments", comments);
        }};
        return AjaxResult.success(map);
    }


    @PostMapping("/views/statistics")
    @Operation(summary = "浏览记录统计")
    public AjaxResult getUserViewsStatistics(@RequestBody ViewStatistics viewStatistics) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(viewStatistics.getEndTime());
        instance.add(Calendar.HOUR, 16);
        viewStatistics.setEndTime(instance.getTime());
        LambdaQueryWrapper<UserViews> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(UserViews::getCreateTime, viewStatistics.getStartTime(), viewStatistics.getEndTime())
                .eq(UserViews::getUserId, viewStatistics.getUserId());
        List<UserViews> list = userViewsService.list(wrapper);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Map<String, Object> map = new HashMap<>();
        Map<String, Integer> cntMap = new HashMap<>();
        Map<String, String> colorMap = new HashMap<>();
        int totalCnt = 0;
        for (UserViews userViews : list) {
            String dateStr = format.format(userViews.getCreateTime());
            cntMap.merge(dateStr, 1, Integer::sum);
            totalCnt++;
        }
        for (Map.Entry<String, Integer> entry : cntMap.entrySet()) {
            String color = colorsTreeMap.ceilingEntry(entry.getValue()).getValue();
            colorMap.put(entry.getKey(), color);
        }
        map.put("totalCnt", totalCnt);
        map.put("views", cntMap);
        map.put("colors", colorMap);
        return AjaxResult.success(map);
    }

    @GetMapping("/articles/info")
    @Operation(summary = "用户发布的文章赞同，浏览信息")
    public AjaxResult getUserArticlesData(@RequestParam(required = false) Integer userId) {
        Map<String, Long> map = getUserArticlesDataMethod(userId);
        return AjaxResult.success(map);
    }

    private Map<String, Long> getUserArticlesDataMethod(Integer userId) {
        if (userId == null) {
            userId = SecurityUtil.getUserId();
        }
        List<Integer> ids = articlesService.getUserPublishArticlesId(userId);
        //发布的文章被多少人认可了,多少人浏览了
        long totalLike = 0;
        long totalViews = 0;
        if (ids.size() > 0) {
            totalViews = userViewsService.count(new LambdaQueryWrapper<UserViews>().in(UserViews::getArticleId, ids));
        }
        for (Integer id : ids) {
            Long likeNum = redisTemplate.opsForSet().size(RedisConstant.ARTICLE_LIKE + id);
            if (likeNum != null) {
                totalLike += likeNum;
            }
        }
        Map<String, Long> map = new HashMap<>();
        map.put("totalLike", totalLike);
        map.put("totalViews", totalViews);
        return map;
    }

    @PostMapping("/edit")
    @Operation(summary = "修改用户信息", description = "只能修改自己的信息")
    @Transactional
    public boolean editUserInfo(@RequestBody UserInfo userInfo) {
        if (!Objects.equals(userInfo.getUserId(), SecurityUtil.getUserId())) return false;
        User user = new User();
        user.setUserId(userInfo.getUserId());
        user.setUserDesc(userInfo.getUserDesc());
        user.setNickName(userInfo.getNickName());
        return userInfoService.saveOrUpdate(userInfo) && userService.updateById(user);
    }

    @PostMapping("/avatar/edit")
    @Operation(summary = "修改用户头像", description = "修改自己的 噢")
    public boolean editUserAvatar(MultipartFile file) throws IOException {
        String imageUrl = uploadUtil.uploadImage(file);
        User user = new User();
        user.setUserId(SecurityUtil.getUserId());
        user.setUserAvatar(imageUrl);
        return userService.updateById(user);
    }

    @GetMapping("/authors")
    @Operation(summary = "关键词查用户")
    public AjaxResult getAuthors(@RequestParam String keyWord, @RequestParam(required = false, defaultValue = "") String type,
                                 @RequestParam(required = false) String flag) {
        List<UserInfo> list = userInfoService.getAuthorsByKeyWord(keyWord);
        Map<Integer, Map<String, Long>> authorToArticlesNum = new HashMap<>();
        for (UserInfo userInfo : list) {
            long count = articlesService.count(new LambdaQueryWrapper<Articles>().eq(Articles::getUserId, userInfo.getUserId()));
            authorToArticlesNum.computeIfAbsent(userInfo.getUserId(), v -> new HashMap<>())
                    .put("articleTotal", count);
            Map<String, Long> map = getUserArticlesDataMethod(userInfo.getUserId());
            authorToArticlesNum.get(userInfo.getUserId()).putAll(map);
        }
        if (List.of("articleTotal", "totalLike", "totalViews").contains(type)) {
            if (flag.equals("m")) {
                list.sort((o1, o2) -> Math.toIntExact(authorToArticlesNum.get(o2.getUserId()).get(type) -
                        authorToArticlesNum.get(o1.getUserId()).get(type)));
            } else if (flag.equals("l")) {
                list.sort((o1, o2) -> Math.toIntExact(authorToArticlesNum.get(o1.getUserId()).get(type) -
                        authorToArticlesNum.get(o2.getUserId()).get(type)));
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("authorToArticlesNum", authorToArticlesNum);
        res.put("userList", list);
        return AjaxResult.success(res);
    }

}
