package com.example.ksfluxmain.service;

import com.example.ksfluxmain.entity.SpeakObject;
import com.example.ksfluxmain.entity.vo.XzDialogue;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 13080
* @description 针对表【xz_dialogue】的数据库操作Service
* @createDate 2023-09-06 22:34:06
*/
public interface XzDialogueService extends IService<XzDialogue> {

    String sendMsg(SpeakObject speakObject);

    String sendMsgByAi(SpeakObject speakObject);

    void setAccessToken(String accessToken);

    String getAccessToken();
}
