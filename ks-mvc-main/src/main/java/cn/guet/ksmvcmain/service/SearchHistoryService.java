package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.SearchHistory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 13080
* @description 针对表【search_history】的数据库操作Service
* @createDate 2023-08-01 13:07:01
*/
public interface SearchHistoryService extends IService<SearchHistory> {

    List<SearchHistory> getHotSearch();

}
