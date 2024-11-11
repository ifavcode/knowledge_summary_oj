package com.example.ksfluxmain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class SpeakObject {

    private List<SpeakMessage> messages;

    private Float temperature;

    private Float topP;

    private Float penaltyScore;

    private boolean stream;

    private String userId;

    private Integer groupId;

    private String aiUrl;
}
