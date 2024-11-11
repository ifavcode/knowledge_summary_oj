package cn.guet.ksmvcmain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * @TableName questions
 */
@TableName(value = "questions")
@Data
public class Questions implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String questionCode;

    private String questionContent;

    private String diffLevel;

    private Integer userId;

    private Date createTime;

    private String delFlag;

    @TableField(exist = false)
    private boolean acFlag;

    @TableField(exist = false)
    private String questionDesc;

    @TableField(exist = false)
    private String token;

    @TableField(exist = false)
    private Integer totalAc;

    @TableField(exist = false)
    private Integer totalTry;

    @TableField(exist = false)
    private List<TagInfo> tagList;

    private static final long serialVersionUID = 1L;
}