package com.example.ksfluxmain.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateImage {

    private String prompt;

    private String size;

    @JSONField(name = "negative_prompt")
    private String negativePrompt;

    private Integer n;

    private Integer steps;

    @JSONField(name = "sampler_index")
    private String samplerIndex;

    private Integer userId;
}
