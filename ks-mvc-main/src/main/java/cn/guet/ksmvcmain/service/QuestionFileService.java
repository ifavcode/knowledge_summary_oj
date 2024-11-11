package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.FileIdAndContent;
import cn.guet.ksmvcmain.entity.vo.QuestionFile;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
* @author rog
* @description 针对表【question_file】的数据库操作Service
* @createDate 2024-11-04 11:54:41
*/
public interface QuestionFileService extends IService<QuestionFile> {

    List<FileIdAndContent>[] getQuestionStdinFileStrList(String questionCode) throws IOException;

    List<QuestionFile> getQuestionFile(String questionCode);
}
