package com.example.ksfluxmain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class XzGroup {

    @TableId(type = IdType.AUTO)
    @Schema(name = "id")
    private Integer groupId;

    @Schema(name = "创建者id", defaultValue = "默认为自己的用户id")
    private Integer userId;

    @Schema(name = "小组标题")
    private String groupTitle;

    private Date CreateTime;
}
