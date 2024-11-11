package com.example.ksfluxmain.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kscommon.entity.AjaxResult;
import com.example.kscommon.utils.UserUtil;
import com.example.ksfluxmain.entity.vo.XzDialogue;
import com.example.ksfluxmain.entity.vo.XzGroup;
import com.example.ksfluxmain.service.XzDialogueService;
import com.example.ksfluxmain.service.XzGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/dialogue")
@Tag(name = "对话组")
public class DialogueController {

    private final XzGroupService xzGroupService;
    private final XzDialogueService xzDialogueService;

    public DialogueController(XzGroupService xzGroupService, XzDialogueService xzDialogueService) {
        this.xzGroupService = xzGroupService;
        this.xzDialogueService = xzDialogueService;
    }

    @GetMapping("/user/group/get")
    @Operation(summary = "获取用户所有对话组")
    public AjaxResult getAllDialogueGroup() {
        List<XzGroup> list = xzGroupService.list(new LambdaQueryWrapper<XzGroup>()
                .eq(XzGroup::getUserId, UserUtil.get().getUserId())
                .orderByDesc(XzGroup::getCreateTime));
        return AjaxResult.success(list);
    }

    @PostMapping("/user/group/delete/{ids}")
    @Operation(summary = "删除用户对话组")
    @Transactional
    public boolean deleteDialogue(@PathVariable List<Integer> ids) {
        if (ids.size() != 0) {
            boolean res = xzGroupService.removeBatchByIds(ids);
            for (Integer id : ids) {
                res &= xzDialogueService.remove(new LambdaQueryWrapper<XzDialogue>().eq(XzDialogue::getGroupId, id));
            }
            return res;
        }
        return false;
    }

    @PostMapping("/user/group/add")
    @Operation(summary = "添加用户对话组")
    public Integer addDialogue(@RequestBody XzGroup xzGroup) {
        Integer groupId = -1;
        xzGroup.setCreateTime(new Date());
        xzGroup.setUserId(UserUtil.get().getUserId());
        if (xzGroupService.save(xzGroup)) {
            groupId = xzGroup.getGroupId();
        }
        return groupId;
    }

    @GetMapping("/group/all")
    @Operation(summary = "获取对话记录")
    public AjaxResult getDialogueByGroupId(@RequestParam Integer groupId) {
        XzGroup xzGroup = xzGroupService.getById(groupId);
        if (xzGroup != null && !Objects.equals(xzGroup.getUserId(), UserUtil.get().getUserId())) {
            return AjaxResult.error("没有权限查询他人对话记录.");
        }
        List<XzDialogue> list = xzDialogueService.list(new LambdaQueryWrapper<XzDialogue>().eq(XzDialogue::getGroupId, groupId));
        return AjaxResult.success(list);
    }
}
