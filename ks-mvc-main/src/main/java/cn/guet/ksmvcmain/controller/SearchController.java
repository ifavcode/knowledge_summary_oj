package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.entity.vo.Articles;
import cn.guet.ksmvcmain.entity.vo.SearchHistory;
import cn.guet.ksmvcmain.service.ArticlesService;
import cn.guet.ksmvcmain.service.SearchHistoryService;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kscommon.entity.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/search")
@Tag(name = "搜索类")
public class SearchController {

    private final ArticlesService articlesService;
    private final SearchHistoryService searchHistoryService;

    public SearchController(ArticlesService articlesService, SearchHistoryService searchHistoryService) {
        this.articlesService = articlesService;
        this.searchHistoryService = searchHistoryService;
    }


    @GetMapping("/articles")
    @Operation(summary = "根据关键字搜索文章(提示搜索)")
    public AjaxResult searchArticles(@RequestParam String keyWord) {
        LambdaQueryWrapper<Articles> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Articles::getArticleTitle, keyWord)
                .or()
                .like(Articles::getRawText, keyWord)
                .last("limit 10")
                .select(Articles::getArticleTitle, Articles::getArticleId);
        List<Articles> list = articlesService.list(wrapper).stream().filter(v -> !v.getArticleTitle().isEmpty()).toList();
        return AjaxResult.success(list);
    }

    @GetMapping("/history/all")
    @Operation(summary = "搜索历史记录")
    public AjaxResult getSearchHistory() {
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, SecurityUtil.getUserId())
                .orderByDesc(SearchHistory::getCreateTime)
                .last("limit 30");
        List<SearchHistory> list = searchHistoryService.list(wrapper);
        return AjaxResult.success(list);
    }

    @PostMapping("/history/{id}")
    @Operation(summary = "删除一条搜索历史记录")
    public boolean deleteSearchHistory(@PathVariable Integer id) {
        return searchHistoryService.removeById(id);
    }

    @PostMapping("/history/clear")
    @Operation(summary = "清空搜索历史记录")
    public boolean deleteAllSearchHistory() {
        return searchHistoryService.remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, SecurityUtil.getUserId()));
    }

    @PostMapping("/history/add")
    @Operation(summary = "增加搜索历史记录")
    public boolean addSearchHistory(@RequestBody SearchHistory searchHistory) {
        searchHistory.setUserId(SecurityUtil.getUserId());
        long count = searchHistoryService.count(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getSearchContent, searchHistory.getSearchContent())
                .eq(SearchHistory::getUserId, searchHistory.getUserId())
        );
        if (count > 0) return false;
        searchHistory.setCreateTime(new Date());
        return searchHistoryService.save(searchHistory);
    }

    @GetMapping("/history/hot")
    @Operation(summary = "热门搜索20条")
    public AjaxResult getHotSearchHistory() {
        return AjaxResult.success(searchHistoryService.getHotSearch());
    }

    @GetMapping("/keyWord/{userId}")
    @Operation(summary = "搜索某个用户文章（关键字）")
    public AjaxResult getUserArticlesByKeyWord(@PathVariable Integer userId,
                                               @RequestParam String keyWord,
                                               @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                               @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        List<Articles> list = articlesService.getUserArticlesByKeyWord(userId, keyWord, pageNum, pageSize);
        return AjaxResult.success(list);
    }
}
