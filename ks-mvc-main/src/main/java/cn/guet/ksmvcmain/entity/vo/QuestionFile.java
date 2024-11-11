package cn.guet.ksmvcmain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

/**
 * @TableName question_file
 */
@TableName(value ="question_file")
@Data
@Accessors(chain = true)
public class QuestionFile implements Serializable {

    @NotNull
    private String questionCode;

    @NotNull
    private Integer stdinFileId;

    @NotNull
    private Integer stdoutFileId;

    @TableField(exist = false)
    private CloudFile inFile;

    @TableField(exist = false)
    private CloudFile outFile;

    private static final long serialVersionUID = 1L;
}