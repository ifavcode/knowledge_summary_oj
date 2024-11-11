package cn.guet.ksmvcmain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum DirEnum {

    DIR("1", "文件夹"),
    FILE("0", "文件");

    private String code;
    private String desc;

    static Map<String, DirEnum> map;

    static {
        map = Arrays.stream(DirEnum.values()).collect(Collectors.toMap(e -> e.code, Function.identity()));
    }

    public static DirEnum typeOf(String code) {
        return map.get(code);
    }
}
