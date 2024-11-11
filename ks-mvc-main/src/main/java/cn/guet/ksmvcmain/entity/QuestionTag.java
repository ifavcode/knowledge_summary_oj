package cn.guet.ksmvcmain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName question_tag
 */
@TableName(value ="question_tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionTag implements Serializable {
    private Integer tagId;

    private Integer questionId;

    private static final long serialVersionUID = 1L;
}