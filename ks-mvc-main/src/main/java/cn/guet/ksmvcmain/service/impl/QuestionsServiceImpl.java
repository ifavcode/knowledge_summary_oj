package cn.guet.ksmvcmain.service.impl;

import cn.guet.ksmvcmain.entity.QuestionTag;
import cn.guet.ksmvcmain.entity.vo.TagInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.Questions;
import cn.guet.ksmvcmain.service.QuestionsService;
import cn.guet.ksmvcmain.mapper.QuestionsMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author rog
 * @description 针对表【questions】的数据库操作Service实现
 * @createDate 2024-11-01 16:17:30
 */
@Service
public class QuestionsServiceImpl extends ServiceImpl<QuestionsMapper, Questions>
        implements QuestionsService {

    @Override
    public Page<Questions> getQuestionsPage(Page<Questions> page, String search) {
        MPJLambdaWrapper<Questions> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(QuestionTag.class, QuestionTag::getQuestionId, Questions::getId)
                .leftJoin(TagInfo.class, TagInfo::getTagId, QuestionTag::getTagId)
                .select(Questions::getId, Questions::getTitle, Questions::getQuestionCode
                        , Questions::getDiffLevel, Questions::getUserId, Questions::getCreateTime)
                .selectCollection(TagInfo.class, Questions::getTagList)
                .like(StringUtils.isNotEmpty(search), Questions::getTitle, search)
                .or()
                .like(StringUtils.isNotEmpty(search), Questions::getQuestionCode, search);
        return baseMapper.selectPage(page, wrapper);
    }
}




