package cn.guet.ksmvcmain.entity.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class SearchHistory {

    @TableId(type = IdType.AUTO)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer searchId;

    @Schema(description = "搜索内容")
    private String searchContent;

    @Schema(description = "谁搜索的")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer userId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(exist = false)
    private Integer searchCount;
}
