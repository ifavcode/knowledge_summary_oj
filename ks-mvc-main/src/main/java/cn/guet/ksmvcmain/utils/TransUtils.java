package cn.guet.ksmvcmain.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TransUtils {

    /**
     * 实体驼峰命名转下划线命名
     */
    public static <T> Map<String, Object> convertToUnderScoreMap(T obj) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = field.get(obj);
            name = name.replaceAll("[A-Z]", "_$0").toLowerCase();
            map.put(name, value);
        }
        return map;
    }

    public static long getFileSizeFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return connection.getContentLengthLong();
        } else {
            log.error("无法获取文件大小，响应码：{}", responseCode);
            return 0;
        }
    }
}
