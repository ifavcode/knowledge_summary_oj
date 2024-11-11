package com.example.kscommon.utils;

import lombok.experimental.UtilityClass;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@UtilityClass
public class UploadUtils {

    public String uploadToServer(InputStream stream, String fileName, String filePath) {
        return new JschUtil().upload(stream, "/opt/files/", filePath, fileName);
    }
}
