package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.entity.vo.CloudFile;
import cn.guet.ksmvcmain.enums.DirEnum;
import cn.guet.ksmvcmain.enums.UploadTypeEnum;
import cn.guet.ksmvcmain.service.CloudFileService;
import cn.guet.ksmvcmain.utils.OSSUtils;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import cn.guet.ksmvcmain.utils.TransUtils;
import cn.guet.ksmvcmain.utils.UploadUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.kscommon.entity.AjaxResult;
import com.example.kscommon.utils.JschUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/cloud/file")
@Tag(name = "我的文件")
@RequiredArgsConstructor
public class CloudFileController {

    private final CloudFileService cloudFileService;
    private final OSSUtils ossUtils;

    @Value("${guet.upload.type}")
    private String uploadType;

    @PostMapping("/upload")
    @Operation(summary = "上传")
    public AjaxResult upload(MultipartFile file, String filePath) throws IOException {
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        CloudFile cloudFile = new CloudFile().setFileName(fileName).setFileSize(fileSize).setCreateTime(new Date()).setUserId(SecurityUtil.getUserId()).setFilePath(filePath).setDirFlag(DirEnum.FILE.getCode());
        UploadTypeEnum uploadTypeEnum = UploadTypeEnum.of(uploadType);
        switch (uploadTypeEnum) {
            case OSS:
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                cloudFile.setFileUrl(ossUtils.upload(file, filePath));
                cloudFileService.save(cloudFile);
                break;
            case SERVER:
                filePath = "/" + SecurityUtil.getUserId() + ("/".equals(filePath) ? "" : filePath);
                String fileUrl = UploadUtils.uploadToServer(file.getInputStream(), fileName, filePath);
                cloudFile.setFileUrl(fileUrl);
                cloudFileService.save(cloudFile);
        }
        return AjaxResult.success();
    }

    @GetMapping("/list")
    public AjaxResult getCloudFileList(@RequestParam String filePath, @RequestParam(required = false) String fileName) {
        List<CloudFile> list = cloudFileService.list(Wrappers.<CloudFile>lambdaQuery()
                .eq(CloudFile::getFilePath, filePath)
                .eq(CloudFile::getUserId, SecurityUtil.getUserId()).orderByDesc(CloudFile::getDirFlag)
                .like(StringUtils.isNotEmpty(fileName), CloudFile::getFileName, fileName)
                .orderByAsc(CloudFile::getFileName));
        return AjaxResult.success(list);
    }

    @PostMapping("/mkdir")
    public AjaxResult mkdir(@RequestParam String filePath, @RequestParam String dirName) {
        String serverPath = "/opt/files/" + SecurityUtil.getUserId() + "/" + filePath;
        new JschUtil().mkdir(serverPath, dirName);
        CloudFile cloudFile = new CloudFile().setFileName(dirName).setFileSize(null).setCreateTime(new Date()).setUserId(SecurityUtil.getUserId()).setFilePath(filePath).setDirFlag(DirEnum.DIR.getCode());
        cloudFileService.save(cloudFile);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody CloudFile cloudFile) {
        cloudFileService.update(cloudFile, Wrappers.<CloudFile>lambdaUpdate().eq(CloudFile::getId, cloudFile.getId()).eq(CloudFile::getUserId, SecurityUtil.getUserId()));
        return AjaxResult.success();
    }

    @PostMapping("/delete/{ids}")
    public AjaxResult delete(@PathVariable("ids") List<Integer> ids) {
        for (Integer id : ids) {
            DirEnum dirEnum = getType(id);
            switch (dirEnum) {
                case FILE:
                    cloudFileService.remove(Wrappers.<CloudFile>lambdaQuery().eq(CloudFile::getId, id).eq(CloudFile::getUserId, SecurityUtil.getUserId()));
                    break;
                case DIR:
                    CloudFile byId = cloudFileService.getById(id);
                    cloudFileService.removeById(id);
                    cloudFileService.remove(Wrappers.<CloudFile>lambdaQuery().likeRight(CloudFile::getFilePath, byId.getFilePath() + "/" + byId.getFileName()).eq(CloudFile::getUserId, SecurityUtil.getUserId()));
            }
        }
        return AjaxResult.success();
    }

    @GetMapping("/download")
    public void download(@RequestParam Integer id, HttpServletResponse response) throws IOException, URISyntaxException {
        DirEnum dirEnum = getType(id);
        CloudFile one = cloudFileService.getOne(Wrappers.<CloudFile>lambdaQuery().eq(CloudFile::getId, id).eq(CloudFile::getUserId, SecurityUtil.getUserId()));
        String fileUrl = one.getFileUrl();
        long fileSize = 0;
        switch (dirEnum) {
            case FILE:
                fileSize += TransUtils.getFileSizeFromUrl(fileUrl);
                response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
                IOUtils.copy(new URL(fileUrl), response.getOutputStream());
                break;
            case DIR:
                String tmp = (one.getFilePath() + "/" + one.getFileName()).replaceAll("/+", "/");
                List<CloudFile> list = cloudFileService.list(
                        Wrappers.<CloudFile>lambdaQuery().eq(CloudFile::getUserId, SecurityUtil.getUserId()).eq(CloudFile::getFilePath, tmp)
                );
                cloudFileService.downloadDir(list, response);
                break;
        }
    }

    private DirEnum getType(Integer id) {
        return DirEnum.typeOf(cloudFileService.getOne(Wrappers.<CloudFile>lambdaQuery().eq(CloudFile::getId, id).select(CloudFile::getDirFlag)).getDirFlag());
    }
}
