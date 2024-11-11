package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.entity.FileIdAndContent;
import cn.guet.ksmvcmain.entity.WebSubmitCode;
import cn.guet.ksmvcmain.entity.vo.CloudFile;
import cn.guet.ksmvcmain.entity.vo.Submissions;
import cn.guet.ksmvcmain.enums.MQDelayEnum;
import cn.guet.ksmvcmain.service.CloudFileService;
import cn.guet.ksmvcmain.service.ExecResultService;
import cn.guet.ksmvcmain.service.QuestionFileService;
import cn.guet.ksmvcmain.service.SubmissionsService;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import cn.guet.ksmvcmain.utils.TransUtils;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.kscommon.constant.MQConstant;
import com.example.kscommon.entity.AjaxResult;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/oj")
@RequiredArgsConstructor
@Slf4j
public class OJController {

    private static OkHttpClient client;
    public static final MediaType JSON = MediaType.get("application/json");
    private final QuestionFileService questionFileService;
    private final SubmissionsService submissionsService;
    private final CloudFileService cloudFileService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Resource(name = "defaultSetting")
    private Map<String, Object> defaultSetting;

    private static Set<String> languagesSet = new HashSet<>();

    @Value("${guet.oj.address}")
    private String submitAddress;

    static {
        client = new OkHttpClient();
        languagesSet.add("java");
        languagesSet.add("python");
        languagesSet.add("c++");
    }

    @GetMapping("/languages")
    public AjaxResult getLanguages() {
        Request request = new Request.Builder()
                .url(submitAddress + "/languages")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            List list = JSONObject.parseObject(res, List.class);
            List<Map> back = new ArrayList<>();
            for (Object o : list) {
                Map map = JSONObject.parseObject(o.toString(), Map.class);
                String id = map.get("id").toString();
                String name = map.get("name").toString();
                for (String s : languagesSet) {
                    if (name.toLowerCase().contains(s)) {
                        back.add(Map.of("id", Integer.parseInt(id), "name", name));
                        break;
                    }
                }
            }
            return AjaxResult.success(back);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/submissions")
    @Transactional(rollbackFor = Exception.class)
    public Map submit(@org.springframework.web.bind.annotation.RequestBody WebSubmitCode webSubmitCode) throws IllegalAccessException, IOException {
        List<FileIdAndContent>[] fileStrList = questionFileService.getQuestionStdinFileStrList(webSubmitCode.getQuestionCode());
        String token = UUID.randomUUID().toString();
        List<String> submitTokenList = new ArrayList<>();
        List<Integer> inIdList = new ArrayList<>();
        List<Integer> outIdList = new ArrayList<>();
        for (int i = 0; i < fileStrList[0].size(); i++) {
            String in = fileStrList[0].get(i).getFileContent();
            Integer inId = fileStrList[0].get(i).getFileId();
            String out = fileStrList[1].get(i).getFileContent();
            Integer outId = fileStrList[1].get(i).getFileId();
            Map<String, Object> params = TransUtils.convertToUnderScoreMap(webSubmitCode);
            params.putAll(defaultSetting);
            params.put("stdin", in);
            params.put("expected_output", out);
            RequestBody body = RequestBody.create(JSONObject.toJSONString(params, JSONWriter.Feature.LargeObject), JSON);
            Request request = new Request.Builder()
                    .url(submitAddress + "/submissions/?base64_encoded=false&wait=false")
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String res = response.body().string();
                log.info(res);
                Map map = JSONObject.parseObject(res, Map.class);
                String submitToken = map.get("token").toString();
                submitTokenList.add(submitToken);
                inIdList.add(inId);
                outIdList.add(outId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String tokens = StringUtils.join(submitTokenList, ",");
        String inIdStr = StringUtils.join(inIdList, ",");
        String outIdStr = StringUtils.join(outIdList, ",");
        Submissions submissions = new Submissions(null, webSubmitCode.getSourceCode(), token, tokens, SecurityUtil.getUserId()
                , new Date(), "0", inIdStr, outIdStr, webSubmitCode.getQuestionCode(), webSubmitCode.getLanguageId());
        submissionsService.save(submissions);
        Message<Submissions> message = MessageBuilder.withPayload(submissions).build();
        rocketMQTemplate.asyncSend(MQConstant.DELAY_CODE_CHECK, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("{}放入异步队列成功，等待设置查询结果", submissions.getToken());
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("{}放入异步队列失败，异常原因：{}", submissions.getToken(), throwable.getMessage());
            }
        }, 3000L, MQDelayEnum.FIVE_SECONDS.getDelayLevel());
        return Map.of("token", token);
    }

    @GetMapping("/submissions/{token}")
    public AjaxResult getSubmissions(@PathVariable("token") String token) {
        Submissions submissions = submissionsService.getOne(Wrappers.<Submissions>lambdaQuery().eq(Submissions::getToken, token));
        if (submissions == null) {
            return AjaxResult.error("未查到此token对应的提交");
        }
        String[] codeSubmitArr = StringUtils.split(submissions.getSubmitTokens(), ",");
        String[] inIdArr = StringUtils.split(submissions.getStdinFileIdsStr(), ",");
        String[] outIdArr = StringUtils.split(submissions.getStdoutFileIdsStr(), ",");
        List<Map> back = new ArrayList<>();
        for (int i = 0; i < codeSubmitArr.length; i++) {
            Request request = new Request.Builder()
                    .url(submitAddress + "/submissions/" + codeSubmitArr[i])
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String res = response.body().string();
                Map map = JSONObject.parseObject(res, Map.class);
                if (map == null) {
                    return AjaxResult.error("未查到此次提交的内容");
                }
                CloudFile inFile = cloudFileService.getById(inIdArr[i]);
                CloudFile outFile = cloudFileService.getById(outIdArr[i]);
                map.put("inFile", inFile);
                map.put("outFile", outFile);
                back.add(map);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return AjaxResult.success(back);
    }

    @PostMapping("/test")
    public Map test(@org.springframework.web.bind.annotation.RequestBody WebSubmitCode webSubmitCode) throws IllegalAccessException {
        Map<String, Object> params = TransUtils.convertToUnderScoreMap(webSubmitCode);
        params.putAll(defaultSetting);
        RequestBody body = RequestBody.create(JSONObject.toJSONString(params), JSON);
        Request request = new Request.Builder()
                .url(submitAddress + "/submissions/?base64_encoded=false&wait=false")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            return JSONObject.parseObject(res, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/submitToken/{token}")
    public Map getSubmitToken(@PathVariable("token") String token) {
        Request detailsRequest = new Request.Builder()
                .url(submitAddress + "/submissions/" + token)
                .get()
                .build();
        try (Response detailsResponse = client.newCall(detailsRequest).execute()) {
            String back = detailsResponse.body().string();
            return JSONObject.parseObject(back);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/submissions/db/{token}")
    public AjaxResult getSubmissionsDb(@PathVariable("token") String token) {
        Submissions submissions = submissionsService.getSubmissionsDetailsByToken(token);
        return AjaxResult.success(submissions);
    }

    @GetMapping("/submissions/self")
    public AjaxResult getSubmissionsSelf(@RequestParam String questionCode) {
        List<Submissions> list = submissionsService.getAllSubmissionsByQuestionCode(questionCode);
        return AjaxResult.success(list);
    }

}
