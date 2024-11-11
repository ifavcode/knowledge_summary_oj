package cn.guet.ksmvcmain.mapper;

import cn.guet.ksmvcmain.entity.vo.ExecResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author rog
 * @description 针对表【exec_result】的数据库操作Mapper
 * @createDate 2024-11-05 13:05:31
 * @Entity cn.guet.ksmvcmain.entity.vo.ExecResult
 */
public interface ExecResultMapper extends BaseMapper<ExecResult> {

    ExecResult getLastAcUpload(@RequestParam("questionCode") String questionCode, @RequestParam("userId") Integer userId);

    ExecResult getLastUpload(@RequestParam("questionCode") String questionCode, @RequestParam("userId") Integer userId);

    List<ExecResult> getLastAcUploadList(@RequestParam("questionCode") List<String> questionCodeList, @RequestParam("userId") Integer userId);

    List<ExecResult> getLastUploadList(@RequestParam("questionCode") List<String> questionCodeList, @RequestParam("userId") Integer userId);
}




