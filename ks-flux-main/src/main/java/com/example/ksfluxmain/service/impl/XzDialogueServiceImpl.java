package com.example.ksfluxmain.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kscommon.utils.UserUtil;
import com.example.ksfluxmain.entity.SpeakObject;
import com.example.ksfluxmain.entity.vo.XzDialogue;
import com.example.ksfluxmain.service.XzDialogueService;
import com.example.ksfluxmain.mapper.XzDialogueMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @author 13080
 * @description 针对表【xz_dialogue】的数据库操作Service实现
 * @createDate 2023-09-06 22:34:06
 */
@Service
@RequiredArgsConstructor
public class XzDialogueServiceImpl extends ServiceImpl<XzDialogueMapper, XzDialogue>
        implements XzDialogueService {

    private String accessToken = "";

    private final Map<Integer, FluxSink<Object>> fluxSinkMap;

    private final OkHttpClient client;

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String getAccessToken() {
        return this.accessToken;
    }


    @Override
    public String sendMsg(SpeakObject speakObject) {
        initSpeakObject(speakObject);
//        String res = HttpUtil.post("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" +
//                accessToken, JSONObject.toJSONString(speakObject));
        /**
         * https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/llama_2_70b Llama-2-70b-chat
         * https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions ERNIE-Bot
         * https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant ERNIE-Bot-turbo
         * https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/bloomz_7b1 BLOOMZ-7B
         * https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro ERNIE-Bot 4.0
         */
        String url = StringUtils.isEmpty(speakObject.getAiUrl()) ? "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant"
                : speakObject.getAiUrl();
        Flux<String> flux = WebClient
                .create().post().uri(url + "?access_token=" +
                        accessToken)
                .bodyValue(JSONObject.toJSONString(speakObject))
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(String.class);
        StringBuilder res = new StringBuilder();
        FluxSink<Object> sink = fluxSinkMap.getOrDefault(UserUtil.get().getUserId(), null);
        if (sink == null) return "您当前未连接";
        flux.doOnComplete(() -> {
            //用户信息保存
            XzDialogue xzDialogueUser = new XzDialogue();
            xzDialogueUser.setDiaRole("user");
            xzDialogueUser.setCreateTime(new Date());
            xzDialogueUser.setDiaContent(speakObject.getMessages().get(speakObject.getMessages().size() - 1).getContent());
            xzDialogueUser.setGroupId(speakObject.getGroupId());
            baseMapper.insert(xzDialogueUser);
            //AI回答信息保存
            xzDialogueUser.setDiaRole("assistant");
            xzDialogueUser.setDiaContent(res.toString());
            xzDialogueUser.setDialogueId(null);
            baseMapper.insert(xzDialogueUser);
        }).subscribe(v -> {
            sink.next(v);
            JSONObject obj = JSONObject.parseObject(v);
            if (obj.get("error_code") != null) {
                res.append(obj.get("error_msg"));
            } else {
                res.append(obj.get("result"));
            }
        });
        return res.toString();
    }

    @Override
    public String sendMsgByAi(SpeakObject speakObject) {
        initSpeakObject(speakObject);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant?access_token=" +
                        accessToken)
                .post(RequestBody.create(JSONObject.toJSONString(speakObject), MediaType.get("application/json")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            //用户信息保存
            XzDialogue xzDialogueUser = new XzDialogue();
            xzDialogueUser.setDiaRole("user");
            xzDialogueUser.setCreateTime(new Date());
            xzDialogueUser.setDiaContent(speakObject.getMessages().get(speakObject.getMessages().size() - 1).getContent());
            xzDialogueUser.setGroupId(speakObject.getGroupId());
            baseMapper.insert(xzDialogueUser);
            //AI回答信息保存
            xzDialogueUser.setDiaRole("assistant");
            JSONObject jsonObject = JSONObject.parseObject(response.body().string());
            String res = "";
            if (jsonObject.get("error_code") == null) {
                res = jsonObject.get("result").toString();
            } else {
                throw new RuntimeException(jsonObject.toString());
            }
            xzDialogueUser.setDiaContent(res);
            xzDialogueUser.setDialogueId(null);
            baseMapper.insert(xzDialogueUser);
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initSpeakObject(SpeakObject speakObject) {
        if (UserUtil.get() == null) {
            speakObject.setUserId("21");
        } else {
            speakObject.setUserId(UserUtil.get().getUserId().toString());
        }
        if (speakObject.getTemperature() == null) {
            speakObject.setTemperature(0.95f);
        }
        if (speakObject.getTopP() == null) {
            speakObject.setTopP(0.8f);
        }
        if (speakObject.getPenaltyScore() == null) {
            speakObject.setPenaltyScore(1.0f);
        }
    }
}




