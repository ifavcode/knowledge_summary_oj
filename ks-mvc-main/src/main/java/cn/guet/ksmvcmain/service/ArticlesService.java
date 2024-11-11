package cn.guet.ksmvcmain.service;

import cn.guet.ksmvcmain.entity.vo.Articles;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author 13080
 * @description 针对表【articles】的数据库操作Service
 * @createDate 2023-07-20 16:33:40
 */
public interface ArticlesService extends IService<Articles> {

    List<Articles> getAllArticles(Integer pageNum, Integer pageSize);

    Articles getArticleById(Integer id);

    List<Articles> getUserLikes(List<Integer> ids);

    List<Integer> getUserPublishArticlesId(Integer userId);

    /**
     * @param ids
     * @param pageNum
     * @param pageSize
     * @param type
     * @param flag     0表示置顶不置顶都取，1表示不取置顶，2表示取置顶
     * @return
     */
    List<Articles> getArticleByIds(List<Integer> ids, Integer pageNum, Integer pageSize, String type, Character flag);

    List<Articles> getArticleByUserId(Integer userId, Integer pageNum, Integer pageSize, String type, Character flag);

    List<Articles> getRecommendArticles();

    List<Articles> getUserArticlesByKeyWord(Integer userId, String keyWord, Integer pageNum, Integer pageSize);

}
