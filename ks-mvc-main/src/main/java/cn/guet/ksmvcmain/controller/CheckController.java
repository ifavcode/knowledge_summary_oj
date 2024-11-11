package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.example.kscommon.entity.AjaxResult;
import com.example.kscommon.entity.RedisConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "签到打卡类")
@RequestMapping("/check")
public class CheckController {

    int[] monthArr = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private final RedisTemplate<String, Object> redisTemplate;

    public CheckController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/in")
    @Operation(summary = "每日打卡")
    public AjaxResult checkIn() {
        if (check()) {
            return AjaxResult.error("今天已经签到过啦~");
        }
        LocalDate now = LocalDate.now();
        redisTemplate.opsForValue().setBit(getCheckInKey(now), now.getDayOfMonth(), true);
        return AjaxResult.success("签到成功.");
    }

    @PostMapping("/have")
    @Operation(summary = "检查是否打卡")
    public boolean check() {
        LocalDate now = LocalDate.now();
        Boolean res = redisTemplate.opsForValue().getBit(getCheckInKey(now), now.getDayOfMonth());
        return res != null ? res : false;
    }

    @GetMapping("/all")
    @Operation(summary = "按年月查询打卡状况")
    public AjaxResult getMonthCheckStatus(@RequestParam Integer year, @RequestParam Integer month) {
        List<Long> res = getCheckDay(year, month);
        return AjaxResult.success(res);
    }

    private String getCheckInKey(LocalDate now) {
        return RedisConstant.CHECK_IN + SecurityUtil.getUserId() + ":" + now.getYear() + "/" + now.getMonth().getValue();
    }

    private List<Long> getCheckDay(int year, int month) {
        LocalDate localDate = LocalDate.of(year, month, 1);
        BitFieldSubCommands.BitFieldSubCommand[] list = new BitFieldSubCommands.BitFieldSubCommand[monthArr[month - 1] + 1];
        for (int i = 0; i < list.length; i++) {
            BitFieldSubCommands.BitFieldGet bitFieldGet = BitFieldSubCommands.BitFieldGet.create(BitFieldSubCommands.BitFieldType.unsigned(1),
                    BitFieldSubCommands.Offset.offset(i));
            list[i] = bitFieldGet;
        }
        BitFieldSubCommands fieldSubCommands = BitFieldSubCommands.create(list);
        return redisTemplate.opsForValue().bitField(getCheckInKey(localDate), fieldSubCommands);
    }

    @GetMapping("/continuesCheck")
    @Operation(summary = "最大连续打卡天数")
    public int getContinuesCheckDay(@RequestParam Integer year, @RequestParam Integer month) {
        List<Long> res = getCheckDay(year, month);
        int ans = 0;
        int cnt = 0;
        for (Long re : res) {
            if (re == 1) {
                cnt++;
            } else {
                ans = Math.max(ans, cnt);
                cnt = 0;
            }
        }
        return ans;
    }
}
