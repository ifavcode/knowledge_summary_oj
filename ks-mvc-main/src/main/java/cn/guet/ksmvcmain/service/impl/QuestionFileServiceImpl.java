package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.FileIdAndContent;
import cn.guet.ksmvcmain.entity.vo.CloudFile;
import cn.guet.ksmvcmain.mapper.CloudFileMapper;
import cn.guet.ksmvcmain.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.QuestionFile;
import cn.guet.ksmvcmain.service.QuestionFileService;
import cn.guet.ksmvcmain.mapper.QuestionFileMapper;
import com.example.kscommon.constant.RedisConstant;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author rog
 * @description 针对表【question_file】的数据库操作Service实现
 * @createDate 2024-11-04 11:54:41
 */
@Service
@RequiredArgsConstructor
public class QuestionFileServiceImpl extends ServiceImpl<QuestionFileMapper, QuestionFile>
        implements QuestionFileService {

    private final CloudFileMapper cloudFileMapper;
    private final static int expireTime = 24;

    @Override
    public List<FileIdAndContent>[] getQuestionStdinFileStrList(String questionCode) throws IOException {
        List<QuestionFile> list = baseMapper.selectList(Wrappers.<QuestionFile>lambdaQuery().eq(QuestionFile::getQuestionCode, questionCode));
        List<FileIdAndContent>[] back = new List[2];
        Arrays.setAll(back, v -> new ArrayList<>());
        if (list.isEmpty()) {
            return back;
        }
        List<Integer> inIds = list.stream().map(QuestionFile::getStdinFileId).toList();
        List<Integer> outIds = list.stream().map(QuestionFile::getStdoutFileId).toList();
        List<CloudFile> stdinFiles = cloudFileMapper.selectList(Wrappers.<CloudFile>lambdaQuery().in(CloudFile::getId, inIds))
                .stream().sorted(Comparator.comparingInt(cf->inIds.indexOf(cf.getId()))).toList();
        List<CloudFile> stdoutFiles = cloudFileMapper.selectList(Wrappers.<CloudFile>lambdaQuery().in(CloudFile::getId, outIds))
                .stream().sorted(Comparator.comparingInt(cf->outIds.indexOf(cf.getId()))).toList();;
        for (CloudFile stdinFile : stdinFiles) {
            String fileContent = RedisUtils.get(String.format(RedisConstant.CACHE_FILE, stdinFile.getId()), String.class);
            if (StringUtils.isEmpty(fileContent)) {
                fileContent = IOUtils.toString(new URL(stdinFile.getFileUrl()), StandardCharsets.UTF_8);
                RedisUtils.set(String.format(RedisConstant.CACHE_FILE, stdinFile.getId()), fileContent, expireTime, TimeUnit.HOURS);
            }
            back[0].add(new FileIdAndContent(stdinFile.getId(), fileContent));
        }
        for (CloudFile stdoutFile : stdoutFiles) {
            String fileContent = RedisUtils.get(String.format(RedisConstant.CACHE_FILE, stdoutFile.getId()), String.class);
            if (StringUtils.isEmpty(fileContent)) {
                fileContent = IOUtils.toString(new URL(stdoutFile.getFileUrl()), StandardCharsets.UTF_8);
                RedisUtils.set(String.format(RedisConstant.CACHE_FILE, stdoutFile.getId()), fileContent, expireTime, TimeUnit.HOURS);
            }
            back[1].add(new FileIdAndContent(stdoutFile.getId(), fileContent));
        }
        return back;
    }

    @Override
    public List<QuestionFile> getQuestionFile(String questionCode) {
        MPJLambdaWrapper<QuestionFile> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(CloudFile.class, "aaa", CloudFile::getId, QuestionFile::getStdinFileId)
                .leftJoin(CloudFile.class, "bbb", CloudFile::getId, QuestionFile::getStdoutFileId)
                .selectAssociation("aaa", CloudFile.class, QuestionFile::getInFile)
                .selectAssociation("bbb", CloudFile.class, QuestionFile::getOutFile)
                .eq(QuestionFile::getQuestionCode, questionCode);
        return baseMapper.selectList(wrapper);
    }
}




