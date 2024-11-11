package cn.guet.ksmvcmain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.guet.ksmvcmain.entity.vo.SearchHistory;
import cn.guet.ksmvcmain.service.SearchHistoryService;
import cn.guet.ksmvcmain.mapper.SearchHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 13080
* @description 针对表【search_history】的数据库操作Service实现
* @createDate 2023-08-01 13:07:01
*/
@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory>
    implements SearchHistoryService{

    @Override
    public List<SearchHistory> getHotSearch() {
        return baseMapper.getHotSearch();
    }
}




