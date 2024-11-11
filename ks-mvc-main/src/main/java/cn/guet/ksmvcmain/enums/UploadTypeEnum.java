package cn.guet.ksmvcmain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum UploadTypeEnum {

    OSS("oss"), SERVER("server");

    private String type;

    private static Map<String, UploadTypeEnum> cache;

    static {
        cache = Arrays.stream(UploadTypeEnum.values()).collect(Collectors.toMap(UploadTypeEnum::getType, Function.identity()));
    }

    public static UploadTypeEnum of(String type) {
        return cache.get(type);
    }

}
