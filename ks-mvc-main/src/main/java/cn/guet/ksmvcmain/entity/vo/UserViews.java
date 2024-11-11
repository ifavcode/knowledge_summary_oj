package cn.guet.ksmvcmain.entity.vo;

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

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class UserViews {

    @TableId(type = IdType.AUTO)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer viewId;

    @Schema(description = "浏览者id")
    private Integer userId;

    @Schema(description = "文章id")
    private Integer articleId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(timezone = "GMT+8")
    private Date createTime;

    @JsonIgnore
    private Character isDel;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Articles articles;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User user;
}
