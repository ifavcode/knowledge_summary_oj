package cn.guet.ksmvcmain.mapper;

import cn.guet.ksmvcmain.entity.vo.Articles;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 13080
 * @description 针对表【articles】的数据库操作Mapper
 * @createDate 2023-07-20 16:33:40
 * @Entity cn.guet.ksmvcmain.entity.vo.Articles
 */
public interface ArticlesMapper extends MPJBaseMapper<Articles> {

    @Select("select article_id from articles where user_id = #{userId}")
    List<Integer> getUserPublishArticlesId(Integer userId);

}




