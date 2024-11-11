package cn.guet.ksmvcmain.entity.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class TagInfo {

    @TableId(type = IdType.AUTO)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer tagId;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date createTime;

    @Schema(description = "发布者id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer userId;
}
