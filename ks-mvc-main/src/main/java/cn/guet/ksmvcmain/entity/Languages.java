package cn.guet.ksmvcmain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName languages
 */
@TableName(value ="languages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Languages implements Serializable {
    private Integer id;

    private String languageName;

    private static final long serialVersionUID = 1L;
}