package com.example.ksfluxmain.controller;


import com.alibaba.fastjson2.JSONObject;
import com.example.kscommon.entity.AjaxResult;
import com.example.kscommon.utils.UserUtil;
import com.example.ksfluxmain.entity.SpeakMessage;
import com.example.ksfluxmain.entity.SpeakObject;
import com.example.ksfluxmain.service.XzDialogueService;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    public static final MediaType JSON = MediaType.get("application/json");

    private final XzDialogueService service;

    private final OkHttpClient client;

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/sendArticle")
    public AjaxResult sendArticle() {
        SpeakObject speakObject = new SpeakObject();
        speakObject.setStream(false);
        speakObject.setGroupId(141);
        speakObject.setMessages(List.of(new SpeakMessage("user", "你是一名科普大师，请给我科普一些有用的小技巧\n" +
                "内容可以是任何方面的，直接写，不要说明其他任何信息")));
        String articleContent = service.sendMsgByAi(speakObject);
        Map<String, Object> body = new HashMap<>();
        body.put("articleContent", articleContent);
        body.put("articleTitle", articleContent.split("。")[0]);
        Request request = new Request.Builder()
                .url("http://127.0.0.1:8085/mvc/articles/add")
                .post(RequestBody.create(JSONObject.toJSONString(body), JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return AjaxResult.success(response.body().string());
        } catch (IOException e) {
            return AjaxResult.error("自动生成文章失败.");
        }
    }

}