package com.example.kscommon.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    static final Filter autoTypeFilter = JSONReader.autoTypeFilter(
            "org.springframework.security.core.authority.SimpleGrantedAuthority",
            "org.springframework.security.web.savedrequest.DefaultSavedReques",
            "cn.guet.ksmvcmain.entity",
            "com.example.kscommon.entity"
    );

    private Class<T> clazz;

    public FastJson2JsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, StandardCharsets.UTF_8);

        return JSON.parseObject(str, clazz, autoTypeFilter);
    }
}
