package cn.guet.ksmvcmain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class ArticleComment {

    @TableId(type = IdType.AUTO)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer commentId;

    @NotNull
    private Integer articleId;

    @NotNull
    private Integer oucId;

    @NotNull
    private String commentContent;

    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(timezone = "GMT+8")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(timezone = "GMT+8")
    private Date updateTime;

    private Character isDel;

    private String commentImg;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer userId;

    private Integer targetUserId;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User user;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User targetUser;

}
