package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.ExecResult;
import cn.guet.ksmvcmain.entity.vo.Submissions;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author rog
 * @description 针对表【exec_result】的数据库操作Service
 * @createDate 2024-11-05 13:05:31
 */
public interface ExecResultService extends IService<ExecResult> {

    void updateExecResult(Submissions submissions);

    ExecResult getLastAcUpload(String questionCode);

    ExecResult getLastUpload(String questionCode);

    List<ExecResult> getLastAcUploadList(List<String> questionCodeList);

    List<ExecResult> getLastUploadList(List<String> questionCodeList);

}
