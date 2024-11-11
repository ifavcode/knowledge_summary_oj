package cn.guet.ksmvcmain.entity.vo;

import cn.guet.ksmvcmain.entity.ArticlesData;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class Articles {

    @TableId(type = IdType.AUTO)
    private Integer articleId;

    @Schema(description = "内容")
    private String articleContent;

    @Schema(description = "发布者id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer userId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "文章标题")
    private String articleTitle;

    @Schema(description = "文章封面")
    private String articleImage;

    @Schema(description = "组件地址（优先）")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String componentAddr;

    @Schema(description = "嵌套网页地址,用于功能展示")
    private String iframeAddr;

    @Schema(description = "内容纯文本")
    private String rawText;

    @Schema(description = "渲染样式")
    private String articleType;

    @Schema(description = "是否置顶")
    @JsonIgnore
    private boolean pinned;

    @TableField(exist = false)
    private List<ArticleTag> articleTags;

    @TableField(exist = false)
    private List<Integer> tags;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<TagInfo> tagInfoList;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User publishUser;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ArticlesData articlesData;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean isLike;
}
