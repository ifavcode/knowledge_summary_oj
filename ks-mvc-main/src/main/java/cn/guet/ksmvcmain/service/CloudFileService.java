package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.CloudFile;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author rog
 * @description 针对表【cloud_file】的数据库操作Service
 * @createDate 2024-10-29 16:26:09
 */
public interface CloudFileService extends IService<CloudFile> {

    void downloadDir(List<CloudFile> fileList, HttpServletResponse response) throws URISyntaxException, MalformedURLException;
}
