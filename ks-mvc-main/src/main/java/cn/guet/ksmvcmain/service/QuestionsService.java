package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.Questions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author rog
 * @description 针对表【questions】的数据库操作Service
 * @createDate 2024-11-01 16:17:30
 */
public interface QuestionsService extends IService<Questions> {

    Page<Questions> getQuestionsPage(Page<Questions> page, String search);
}
