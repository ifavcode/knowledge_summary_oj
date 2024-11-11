package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.vo.CloudFile;
import cn.guet.ksmvcmain.entity.vo.Submissions;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.ExecResult;
import cn.guet.ksmvcmain.service.ExecResultService;
import cn.guet.ksmvcmain.mapper.ExecResultMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author rog
 * @description 针对表【exec_result】的数据库操作Service实现
 * @createDate 2024-11-05 13:05:31
 */
@Service
public class ExecResultServiceImpl extends ServiceImpl<ExecResultMapper, ExecResult>
        implements ExecResultService {

    @Value("${guet.oj.address}")
    private String submitAddress;

    private static OkHttpClient client = new OkHttpClient();

    @Override
    public void updateExecResult(Submissions submissions) {
        ExecResult execResult = baseMapper.selectById(submissions.getId());
        Map<String, String> detailsMoreMap = new HashMap<>();
        BigDecimal totalTime = BigDecimal.ZERO, totalMemory = BigDecimal.ZERO;
        if (execResult == null) {
            int acCnt = 0, teCnt = 0, waCnt = 0, reCnt = 0, ceCnt = 0, otherCnt = 0;
            String[] submitTokens = StringUtils.split(submissions.getSubmitTokens(), ",");
            String[] inFileIds = StringUtils.split(submissions.getStdinFileIdsStr(), ",");
            for (int i = 0; i < submitTokens.length; i++) {
                String submitToken = submitTokens[i];
                Request request = new Request.Builder()
                        .url(submitAddress + "/submissions/" + submitToken)
                        .get()
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    String res = response.body().string();
                    Map map = JSONObject.parseObject(res, Map.class);
                    if (map.containsKey("status")) {
                        JSONObject status = JSONObject.parseObject(map.get("status").toString());
                        Integer id = status.getInteger("id");
                        if (id == 3) {
                            acCnt += 1;
                        } else if (id == 4) {
                            waCnt += 1;
                        } else if (id == 5) {
                            teCnt += 1;
                        } else if (id == 6) {
                            ceCnt += 1;
                        } else if (id <= 12) {
                            reCnt += 1;
                        } else {
                            otherCnt += 1;
                        }
                        String description = status.get("description").toString();
                        detailsMoreMap.put(inFileIds[i], description);
                        totalMemory = totalMemory.add(BigDecimal.valueOf(Float.parseFloat(map.get("memory").toString())));
                        totalTime = totalTime.add(BigDecimal.valueOf(Float.parseFloat(map.get("time").toString())));
                    } else {
                        throw new RuntimeException("judge0 异常！");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            ExecResult result = new ExecResult(submissions.getId(), acCnt, teCnt, waCnt, reCnt, ceCnt, otherCnt,
                    new Date(), JSONObject.toJSONString(detailsMoreMap), totalMemory, totalTime);
            baseMapper.insert(result);
        }
    }

    @Override
    public ExecResult getLastAcUpload(String questionCode) {
        return baseMapper.getLastAcUpload(questionCode, SecurityUtil.getUserId());
    }

    @Override
    public ExecResult getLastUpload(String questionCode) {
        return baseMapper.getLastUpload(questionCode, SecurityUtil.getUserId());
    }

    @Override
    public List<ExecResult> getLastAcUploadList(List<String> questionCodeList) {
        return baseMapper.getLastAcUploadList(questionCodeList, SecurityUtil.getUserId());
    }

    @Override
    public List<ExecResult> getLastUploadList(List<String> questionCodeList) {
        return baseMapper.getLastUploadList(questionCodeList, SecurityUtil.getUserId());
    }

}




