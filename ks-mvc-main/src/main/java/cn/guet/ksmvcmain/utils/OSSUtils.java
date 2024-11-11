package cn.guet.ksmvcmain.utils;

import cn.guet.ksmvcmain.config.OSSProperties;
import com.aliyun.oss.OSS;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OSSUtils {

    private final OSS ossClient;
    private final OSSProperties ossProperties;

    @SneakyThrows
    public String upload(MultipartFile file, String filePath) {
        List<String> pathList = new ArrayList<>();
        pathList.add(ossProperties.getBasePath());
        pathList.add(SecurityUtil.getUserId().toString());
        pathList.addAll(Arrays.asList(filePath.split("/")));
        pathList.add(file.getOriginalFilename());
        String path = StringUtils.join(pathList, "/");
        if (!"".equals(path) && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        ossClient.putObject(ossProperties.getBucketName(), path, file.getInputStream());
        String url = "";
        if (StringUtils.isNotEmpty(ossProperties.getDomain())) {
            url = ossProperties.getDomain() + (ossProperties.getDomain().endsWith("/") ? "" : "/") + path;
        } else {
            url = "https://" + ossProperties.getBucketName() + (ossProperties.getBucketName().endsWith("/") ? "" : "/") + path;
        }
        return url;
    }
}
