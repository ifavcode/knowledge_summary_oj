package cn.guet.ksmvcmain.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.*;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public OSS ossClient() {
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        String accessKeyId = "LTAI5tRjYr5f8FecYJuFzP8S";
        String accessKeySecret = "lhtPoOnsm1ZFnsuNxSSoYbTWCZOInY";
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        // 创建OSSClient实例。
        return new OSSClientBuilder().build(endpoint, credentialsProvider);
    }
}
