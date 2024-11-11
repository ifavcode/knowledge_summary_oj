package com.example.ksfluxmain.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.cloud.commons.io.FileUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.kscommon.entity.AjaxResult;
import com.example.kscommon.entity.User;
import com.example.kscommon.utils.UploadUtils;
import com.example.kscommon.utils.UserUtil;
import com.example.ksfluxmain.entity.GenerateImage;
import com.example.ksfluxmain.entity.SpeakObject;
import com.example.ksfluxmain.entity.vo.XzDialogue;
import com.example.ksfluxmain.service.XzDialogueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/bigData")
@Tag(name = "文心一言")
@RequiredArgsConstructor
public class BigDataController {

    private static final String clientId = "4TDt8gCfKzIfzstvaHqvknZU";
    private static final String clientSecret = "do6SLmwMjp5mKw0uC9kYQzPKVE1s5yh1";

    private final Map<Integer, FluxSink<Object>> fluxSinkMap;

    private final UserUtil userUtil;

    private final XzDialogueService service;

    private final OkHttpClient client;

    private static final MediaType JSON = MediaType.get("application/json");


    @PostMapping("/send")
    @Operation(summary = "发送信息")
    @Transactional
    public String sendMessage(@RequestBody SpeakObject speakObject) {
        return service.sendMsg(speakObject);
    }

    @PostConstruct
    public String getAccessToken() {
        String body = HttpRequest.post("https://aip.baidubce.com/oauth/2.0/token?" + "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret).execute().body();
        JSONObject object = JSONObject.parseObject(body);
        service.setAccessToken(object.get("access_token").toString());
        return object.get("access_token").toString();
    }


    @GetMapping("/connect/{token}")
    public Flux<String> esConnect(@PathVariable String token) {
        User user = userUtil.getUser(token);
        if (user != null) {
            Flux<Object> flux = Flux.create(sink -> {
                fluxSinkMap.put(user.getUserId(), sink);
                sink.next("connected");
            });
            return flux.map(String::valueOf);
        }
        return Flux.empty();
    }

    @PostMapping("/generateImage")
    public AjaxResult generateImage(@RequestBody GenerateImage generateImage) {
        Request request = new Request.Builder().url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/text2image/sd_xl?access_token=" +
                        service.getAccessToken())
                .post(okhttp3.RequestBody.create(JSONObject.toJSONString(generateImage), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JSONObject object = JSONObject.parseObject(body);
            if (object.get("error_code") != null) {
                return AjaxResult.error(object);
            }
            //自动保存一份到服务器
            Map[] data = JSONObject.parseObject(object.get("data").toString(), Map[].class);
            for (Map map : data) {
                String base64ImageUrl = map.get("b64_image").toString();
                File file = File.createTempFile(UUID.randomUUID().toString(), ".png");
                Base64.decodeToFile(base64ImageUrl, file);
                FileInputStream inputStream = new FileInputStream(file);
//                UploadUtils.uploadToServer(inputStream);
                inputStream.close();
            }
            return AjaxResult.success(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
