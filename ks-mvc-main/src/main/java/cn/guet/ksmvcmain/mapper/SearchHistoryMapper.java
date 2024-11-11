package cn.guet.ksmvcmain.mapper;

import cn.guet.ksmvcmain.entity.vo.SearchHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 13080
* @description 针对表【search_history】的数据库操作Mapper
* @createDate 2023-08-01 13:07:01
* @Entity cn.guet.ksmvcmain.entity.vo.SearchHistory
*/
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    @Select("SELECT search_content,COUNT(1) as searchCount FROM `search_history` GROUP BY search_content " +
            "ORDER BY searchCount DESC limit 10")
    List<SearchHistory> getHotSearch();
}




