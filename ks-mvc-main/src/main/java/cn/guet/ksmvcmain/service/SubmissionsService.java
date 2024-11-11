package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.Submissions;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author rog
 * @description 针对表【submissions】的数据库操作Service
 * @createDate 2024-11-04 13:18:20
 */
public interface SubmissionsService extends IService<Submissions> {

    List<Submissions> getAllSubmissionsByQuestionCode(String questionCode);

    Submissions getSubmissionsDetailsByToken(String token);

    List<Submissions> getSubmissionsByQuestionCodeList(List<String> questionCodeList);
}
