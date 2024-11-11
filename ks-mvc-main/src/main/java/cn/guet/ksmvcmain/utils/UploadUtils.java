package cn.guet.ksmvcmain.utils;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.example.kscommon.utils.JschUtil;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@UtilityClass
public class UploadUtils {

    public String uploadToServer(MultipartFile file, boolean randomName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return new JschUtil().upload(file, "/opt/files/", dateFormat.format(new Date()), randomName);
    }

    public String uploadToServer(InputStream stream, String fileName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return new JschUtil().upload(stream, "/opt/files/", dateFormat.format(new Date()), fileName);
    }

    public String uploadToServer(InputStream stream, String fileName, String filePath) {
        return new JschUtil().upload(stream, "/opt/files/", filePath, fileName);
    }

    public String serverPath(MultipartFile file, boolean randomName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String url = "/opt/files/" + dateFormat.format(new Date()) + "/";
        if (randomName) {
            url += UuidUtils.generateUuid();
        } else {
            url += file.getOriginalFilename();
        }
        return url;
    }
}
