package cn.guet.ksmvcmain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum MQDelayEnum {

    ONE_SECONDS(1, "1秒"),
    FIVE_SECONDS(2, "5秒"),
    TEN_SECONDS(3, "10秒"),
    THIRTY_SECONDS(4, "30秒"),
    ONE_MINUTES(5, "1分钟"),
    TWO_MINUTES(6, "2分钟"),
    THREE_MINUTES(7, "3分钟"),
    FOUR_MINUTES(8, "4分钟"),
    FIVE_MINUTES(9, "5分钟"),
    SIX_MINUTES(10, "6分钟"),
    SEVEN_MINUTES(11, "7分钟"),
    EIGHT_MINUTES(12, "8分钟"),
    NINE_MINUTES(13, "9分钟"),
    TEN_MINUTES(14, "10分钟"),
    TWENTY_MINUTES(15, "20分钟"),
    THIRTY_MINUTES(16, "30分钟"),
    ONE_HOURS(17, "1小时"),
    TWO_HOURS(18, "2小时");

    private final Integer delayLevel;
    private final String desc;

    private static Map<Integer, MQDelayEnum> cache;

    static {
        cache = Arrays.stream(MQDelayEnum.values()).collect(Collectors.toMap(MQDelayEnum::getDelayLevel, Function.identity()));
    }

    public MQDelayEnum of(Integer delayLevel) {
        return cache.get(delayLevel);
    }

}
