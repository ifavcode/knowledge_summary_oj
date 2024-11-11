package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.enums.DirEnum;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.CloudFile;
import cn.guet.ksmvcmain.service.CloudFileService;
import cn.guet.ksmvcmain.mapper.CloudFileMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.util.URLEncoder;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author rog
 * @description 针对表【cloud_file】的数据库操作Service实现
 * @createDate 2024-10-29 16:26:09
 */
@Service
public class CloudFileServiceImpl extends ServiceImpl<CloudFileMapper, CloudFile>
        implements CloudFileService {

    @Override
    public void downloadDir(List<CloudFile> fileList, HttpServletResponse response) {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(response.getOutputStream());
            downloadDir(zos, fileList, "");
            response.setHeader(HttpHeaders.CONTENT_LENGTH, "" + response.getBufferSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void downloadDir(ZipOutputStream zos, List<CloudFile> fileList, String parentPath) {
        try {
            for (CloudFile file : fileList) {
                String nextPath = parentPath + "/" + file.getFileName();
                if (nextPath.charAt(0) == '/') {
                    nextPath = nextPath.substring(1);
                }
                if (file.getDirFlag().equals(DirEnum.DIR.getCode())) {
                    List<CloudFile> nextList = baseMapper.selectList(Wrappers.<CloudFile>lambdaQuery()
                            .eq(CloudFile::getUserId, SecurityUtil.getUserId())
                            .eq(CloudFile::getFilePath, file.getFilePath() + "/" + file.getFileName()));
                    downloadDir(zos, nextList, nextPath);
                    continue;
                }
                zos.putNextEntry(new ZipEntry(nextPath));
                zos.write(IOUtils.toByteArray(new URL(file.getFileUrl())));
                zos.flush();
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}




