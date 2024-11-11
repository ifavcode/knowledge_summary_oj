package cn.guet.ksmvcmain.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class ArticleTag {

    private Integer tagId;
    private Integer articleId;
}
