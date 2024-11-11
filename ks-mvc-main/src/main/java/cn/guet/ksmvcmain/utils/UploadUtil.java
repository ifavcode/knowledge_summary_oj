package cn.guet.ksmvcmain.utils;

import com.aliyun.oss.OSS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Component
public class UploadUtil {

    @Autowired
    private OSS ossClient;

    public String uploadImage(MultipartFile file) throws IOException {
        LocalDate now = LocalDate.now();
        String date = now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth();
        String fileName = date + "/" + file.getOriginalFilename();
        ossClient.putObject("smart-campus-refactoring", fileName, file.getInputStream());
        return "https://guetzjb.cn/" + fileName;
    }

    public String uploadImageByPath(MultipartFile file, String filePath) throws IOException {
        LocalDate now = LocalDate.now();
        String fileName = filePath + "/" + file.getOriginalFilename();
        ossClient.putObject("smart-campus-refactoring", fileName, file.getInputStream());
        return "https://guetzjb.cn/" + fileName;
    }
}
