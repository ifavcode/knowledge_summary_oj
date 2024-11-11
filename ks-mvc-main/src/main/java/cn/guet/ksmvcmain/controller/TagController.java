package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.entity.QuestionTag;
import cn.guet.ksmvcmain.entity.vo.TagInfo;
import cn.guet.ksmvcmain.service.QuestionTagService;
import cn.guet.ksmvcmain.service.TagInfoService;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import cn.guet.ksmvcmain.utils.UploadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.kscommon.entity.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/tag")
@Tag(name = "标签类")
@RequiredArgsConstructor
public class TagController {

    private final TagInfoService service;
    private final UploadUtil uploadUtil;
    private final QuestionTagService questionTagService;

    @GetMapping("/all")
    @Operation(summary = "获取所有标签")
    public AjaxResult getAll() {
        return AjaxResult.success(service.list());
    }

    @PostMapping("/add")
    @Operation(summary = "新增多个标签")
    public AjaxResult addAll(@RequestBody List<TagInfo> tagInfoList) {
        Integer userId = SecurityUtil.getUserId();
        List<TagInfo> ans = new ArrayList<>();
        for (TagInfo tagInfo : tagInfoList) {
            LambdaQueryWrapper<TagInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TagInfo::getTagName, tagInfo.getTagName());
            if (service.count(wrapper) > 0) continue;
            tagInfo.setTagId(userId);
            tagInfo.setCreateTime(new Date());
            service.save(tagInfo);
            if (tagInfo.getTagId() != null) {
                ans.add(tagInfo);
            }
        }
        return AjaxResult.success(ans);
    }

    @DeleteMapping("/delete/{tagIds}")
    @Operation(summary = "删除多个标签")
    public boolean deleteAll(@PathVariable List<Integer> tagIds) {
        return service.removeBatchByIds(tagIds);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    public AjaxResult uploadImage(MultipartFile file) throws IOException {
        return AjaxResult.success(uploadUtil.uploadImage(file));
    }

    /**
     * 2024/11/11 15点12分
     */
    @PostMapping("/question/add")
    @Transactional
    public AjaxResult addQuestionTag(@RequestBody List<TagInfo> tagList, @RequestParam Integer questionId) {
        questionTagService.remove(Wrappers.<QuestionTag>lambdaQuery().eq(QuestionTag::getQuestionId, questionId));
        for (TagInfo tagInfo : tagList) {
            boolean exists = service.exists(Wrappers.<TagInfo>lambdaQuery().eq(TagInfo::getTagName, tagInfo.getTagName()));
            TagInfo saveTag = null;
            boolean saved = true;
            if (!exists) {
                saveTag = new TagInfo(null, tagInfo.getTagName(), new Date(), SecurityUtil.getUserId());
                saved = service.save(saveTag);
            } else {
                saveTag = service.getOne(Wrappers.<TagInfo>lambdaQuery().eq(TagInfo::getTagName, tagInfo.getTagName()));
            }
            QuestionTag saveQT = new QuestionTag(saveTag.getTagId(), questionId);
            boolean saved1 = questionTagService.save(saveQT);
            if (!saved1 || !saved) {
                throw new RuntimeException("更新问题标签失败");
            }
        }
        return AjaxResult.success();
    }

}
