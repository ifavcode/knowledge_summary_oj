package cn.guet.ksmvcmain;

import cn.guet.ksmvcmain.entity.Languages;
import cn.guet.ksmvcmain.entity.SubmitCode;
import cn.guet.ksmvcmain.entity.vo.CloudFile;
import cn.guet.ksmvcmain.enums.DirEnum;
import cn.guet.ksmvcmain.service.LanguagesService;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectResult;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.kscommon.entity.AjaxResult;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SpringBootTest
class KsMvcMainApplicationTests {

    @Autowired
    private LanguagesService languagesService;

    @Autowired
    private OSS ossClient;

    @Test
    void contextLoads() {
        String bucketName = "smart-campus-refactoring";
        PutObjectResult result = ossClient.putObject(bucketName, "test.txt", new ByteArrayInputStream("HAHA".getBytes()));
        System.out.println(JSONObject.toJSONString(result));
    }

    public static void main(String[] args) throws IllegalAccessException, IOException {
        int n = (int) 1e6;
        Random random = new Random();
        File in = new File("E:\\本地开发\\knowledge-summary-admin\\ks-mvc-main\\src\\main\\resources\\big.in");
        File out = new File("E:\\本地开发\\knowledge-summary-admin\\ks-mvc-main\\src\\main\\resources\\big.out");
        FileOutputStream fin = new FileOutputStream(in);
        FileOutputStream fout = new FileOutputStream(out);
        fin.write((n + "\n").getBytes());
        for (int i = 0; i < n; i++) {
            long a = random.nextLong((int) -1e5, (int) 1e9 + 1);
            long b = random.nextInt((int) -1e5, (int) 1e9 + 1);
            fin.write((a + " " + b + "\n").getBytes());
            fout.write(((a + b) + "\n").getBytes());
        }
    }

    public static Object getValueByFieldName(Object obj, String fieldName) throws IllegalAccessException {
        Field field = null;
        try {
            field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void downloadDir(File file) {
        File outFile = new File("E:\\本地开发\\knowledge-summary-admin\\ks-mvc-main\\src\\main\\resources\\test.zip");
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(outFile));
            downloadDir(zos, file, "");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void downloadDir(ZipOutputStream zos, File file, String parentPath) {
        try {
            for (File childrenFile : file.listFiles()) {
                String nextPath = parentPath + "/" + childrenFile.getName();
                if (nextPath.charAt(0) == '/') {
                    nextPath = nextPath.substring(1);
                }
                if (childrenFile.isDirectory()) {
                    downloadDir(zos, childrenFile, nextPath);
                    return;
                }
                zos.putNextEntry(new ZipEntry(nextPath));
                zos.write(IOUtils.toByteArray(new FileInputStream(childrenFile)));
//                zos.write(childrenFile.getAbsolutePath().getBytes(StandardCharsets.UTF_8));
                zos.flush();
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
