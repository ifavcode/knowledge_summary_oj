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
public class XzDialogue {

    @TableId(type = IdType.AUTO)
    @Schema(name = "id")
    private Integer dialogueId;

    @Schema(name = "对话内容")
    private String diaContent;

    @Schema(name = "对话所属组")
    private Integer groupId;

    @Schema(name = "对话角色")
    private String diaRole;

    @Schema(name = "对话时间")
    private Date createTime;

}
