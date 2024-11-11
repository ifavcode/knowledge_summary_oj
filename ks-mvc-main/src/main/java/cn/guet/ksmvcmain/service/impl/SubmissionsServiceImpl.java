package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.Languages;
import cn.guet.ksmvcmain.entity.vo.ExecResult;
import cn.guet.ksmvcmain.entity.vo.Questions;
import cn.guet.ksmvcmain.entity.vo.User;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.Submissions;
import cn.guet.ksmvcmain.service.SubmissionsService;
import cn.guet.ksmvcmain.mapper.SubmissionsMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author rog
 * @description 针对表【submissions】的数据库操作Service实现
 * @createDate 2024-11-04 13:18:20
 */
@Service
public class SubmissionsServiceImpl extends ServiceImpl<SubmissionsMapper, Submissions>
        implements SubmissionsService {

    @Override
    public List<Submissions> getAllSubmissionsByQuestionCode(String questionCode) {
        MPJLambdaWrapper<Submissions> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(ExecResult.class, ExecResult::getSubmissionId, Submissions::getId)
                .leftJoin(Languages.class, Languages::getId, Submissions::getLanguageId)
                .selectAssociation(ExecResult.class, Submissions::getExecResults)
                .selectAssociation(Languages.class, Submissions::getLanguages)
                .eq(Submissions::getUserId, SecurityUtil.getUserId())
                .eq(Submissions::getQuestionCode, questionCode)
                .orderByDesc(Submissions::getCreateTime);
        return baseMapper.selectJoinList(Submissions.class, wrapper);
    }

    @Override
    public Submissions getSubmissionsDetailsByToken(String token) {
        MPJLambdaWrapper<Submissions> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(ExecResult.class, ExecResult::getSubmissionId, Submissions::getId)
                .leftJoin(Questions.class, Questions::getQuestionCode, Submissions::getQuestionCode)
                .leftJoin(Languages.class, Languages::getId, Submissions::getLanguageId)
                .leftJoin(User.class, User::getUserId, Submissions::getUserId)
                .selectAssociation(ExecResult.class, Submissions::getExecResults)
                .selectAssociation(Questions.class, Submissions::getQuestions, map -> map.filter(col -> !"question_content".equals(col.getColumn())))
                .selectAssociation(Languages.class, Submissions::getLanguages)
                .selectAssociation(User.class, Submissions::getUser, map -> map.result(User::getUserAvatar).result(User::getNickName).result(User::getUserId))
                .eq(Submissions::getUserId, SecurityUtil.getUserId())
                .eq(Submissions::getToken, token);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public List<Submissions> getSubmissionsByQuestionCodeList(List<String> questionCodeList) {
        if (questionCodeList.isEmpty()) {
            return Collections.emptyList();
        }
        MPJLambdaWrapper<Submissions> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(ExecResult.class, ExecResult::getSubmissionId, Submissions::getId)
                .selectAssociation(ExecResult.class, Submissions::getExecResults)
                .in(Submissions::getQuestionCode, questionCodeList);
        return baseMapper.selectJoinList(Submissions.class, wrapper);
    }

}




