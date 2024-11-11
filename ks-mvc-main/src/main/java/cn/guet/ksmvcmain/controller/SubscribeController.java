package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.entity.SubDateDetails;
import cn.guet.ksmvcmain.service.SubDateDetailsService;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kscommon.entity.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Tag(name = "预约系统", description = "采用时间戳管理")
@RequestMapping("/subscribe")
public class SubscribeController {

    /**
     * 预约某个时间段
     * 查询某个时间段是否预约
     * 罗列选定日期的所有预定信息
     */

    //一个时间段最大预约人数
    final static int SUB_MAX = 10;
    final static int NEED_HOUR = 1000 * 60 * 60;

    //从2023，1月1日算起
    static long initTime;
    static long TREE_INF = (long) 1e6;

    //10年 到达87672
    Tree root = new Tree(1L, TREE_INF);

    static {
        Calendar instance = Calendar.getInstance();
        instance.set(2023, Calendar.JANUARY, 1);
        initTime = instance.getTimeInMillis() / NEED_HOUR;
    }

    public SubscribeController(SubDateDetailsService service) {
        this.service = service;
    }

    static class Tree {
        long start;
        long end;
        Tree left = null;
        Tree right = null;
        int count = 0;

        public Tree(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    public void update(Tree root, long l, long r, long L, long R) {
        if (l == r) {
            root.count++;
            return;
        }
        long m = (r - l) / 2 + l;
        if (root.left == null) root.left = new Tree(l, m);
        if (root.right == null) root.right = new Tree(m + 1, r);
        if (m >= L) {
            update(root.left, l, m, L, R);
        }
        if (m < R) {
            update(root.right, m + 1, r, L, R);
        }
        root.count = Math.max(root.left.count, root.right.count);
    }

    public int query(Tree root, long l, long r, long L, long R) {
        if (root == null) return 0;
        if (l >= L && r <= R) {
            return root.count;
        }
        long m = (r - l) / 2 + l;
        int ans = 0;
        if (m >= L) {
            ans = query(root.left, l, m, L, R);
        }
        if (m < R) {
            ans = Math.max(ans, query(root.right, m + 1, r, L, R));
        }
        return ans;
    }

    private final SubDateDetailsService service;

    @PostConstruct
    private void initSub() {
        List<SubDateDetails> list = service.list();
        for (SubDateDetails subDateDetails : list) {
            long s = (subDateDetails.getStartTime().getTime() - initTime) / NEED_HOUR;
            long e = (subDateDetails.getStartTime().getTime() - initTime) / NEED_HOUR;
            update(root, 1, TREE_INF, s, e);
        }
    }

    @PostMapping("/date")
    @Operation(summary = "预约某个时间段")
    public AjaxResult subscribeDate(@RequestBody SubDateDetails subDateDetails) {
        long s = (subDateDetails.getStartTime().getTime() - initTime) / NEED_HOUR;
        long e = (subDateDetails.getStartTime().getTime() - initTime) / NEED_HOUR;
        //预约有区间最大值超过最大人数
        LambdaQueryWrapper<SubDateDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(SubDateDetails::getStartTime, subDateDetails.getStartTime())
                .ge(SubDateDetails::getEndTime, subDateDetails.getStartTime())
                .or(v -> v.le(SubDateDetails::getStartTime, subDateDetails.getEndTime())
                        .ge(SubDateDetails::getEndTime, subDateDetails.getEndTime()))
                .or(v -> v.ge(SubDateDetails::getStartTime, subDateDetails.getStartTime())
                        .le(SubDateDetails::getEndTime, subDateDetails.getEndTime()))
                .eq(SubDateDetails::getUserId, SecurityUtil.getUserId());
        long count = service.count(wrapper);
        if (count > 0) {
            return AjaxResult.error("请勿重复预约某个时间");
        }
        if (query(root, 1, TREE_INF, s, e) > SUB_MAX) {
            return AjaxResult.error("预约区间最大值超过最大人数（10人）");
        }
        subDateDetails.setUserId(SecurityUtil.getUserId());
        boolean res = service.save(subDateDetails);
        if (res) {
            update(root, 1, TREE_INF, s, e);
        }
        return AjaxResult.success("预约成功");
    }

    @GetMapping("/info")
    @Operation(summary = "获取预约信息")
    public AjaxResult getSubInfo() {
        LambdaQueryWrapper<SubDateDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubDateDetails::getUserId, SecurityUtil.getUserId());
        wrapper.orderByAsc(SubDateDetails::getStartTime);
        List<SubDateDetails> list = service.list(wrapper);
        Map<String, List<SubDateDetails>> map = new HashMap<>();
        List<SubDateDetails> overTimeList = new ArrayList<>();
        List<SubDateDetails> notOverTimeList = new ArrayList<>();
        long cur = System.currentTimeMillis();
        for (SubDateDetails subDateDetails : list) {
            if (subDateDetails.getEndTime().getTime() + 1000 * 60 * 60 <= cur) {
                overTimeList.add(subDateDetails);
            } else {
                notOverTimeList.add(subDateDetails);
            }
        }
        map.put("overTime", overTimeList);
        map.put("notOverTime", notOverTimeList);
        return AjaxResult.success(map);
    }

    @GetMapping("/query")
    @Operation(summary = "查询时间段是否可预约")
    public boolean checkSubDate(SubDateDetails subDateDetails) {
        long s = (subDateDetails.getStartTime().getTime() - initTime) / NEED_HOUR;
        long e = (subDateDetails.getStartTime().getTime() - initTime) / NEED_HOUR;
        return query(root, 1, TREE_INF, s, e) <= 10;
    }


}
