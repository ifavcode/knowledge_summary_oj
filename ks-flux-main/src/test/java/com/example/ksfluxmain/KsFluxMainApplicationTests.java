package com.example.ksfluxmain;

import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class KsFluxMainApplicationTests {

    @Test
    void contextLoads() {

    }

    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:8085/flux/test/sendArticle")
                .post(RequestBody.create("", MediaType.get("application/json; charset=utf-8")))
                .build();
        int cnt = 1;
        for (int i = 0; i < 10; i++) {
            try (Response response = client.newCall(request).execute()) {
                System.out.println(response.body().string());
            } catch (IOException e) {
                System.out.println("出错了");
            }
            System.out.println("完成" + cnt + "次");
            cnt++;
        }
    }


}
