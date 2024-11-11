package cn.guet.ksmvcmain.entity.vo;

import cn.guet.ksmvcmain.entity.Languages;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName submissions
 */
@TableName(value = "submissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Submissions implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String sourceCode;

    private String token;

    private String submitTokens;

    private Integer userId;

    private Date createTime;

    private String delFlag;

    private String stdinFileIdsStr;

    private String stdoutFileIdsStr;

    private String questionCode;

    private Integer languageId;

    public Submissions(Integer id, String sourceCode, String token, String submitTokens, Integer userId, Date createTime,
                       String delFlag, String stdinFileIdsStr, String stdoutFileIdsStr, String questionCode, Integer languageId) {
        this.id = id;
        this.sourceCode = sourceCode;
        this.token = token;
        this.submitTokens = submitTokens;
        this.userId = userId;
        this.createTime = createTime;
        this.delFlag = delFlag;
        this.stdinFileIdsStr = stdinFileIdsStr;
        this.stdoutFileIdsStr = stdoutFileIdsStr;
        this.questionCode = questionCode;
        this.languageId = languageId;
    }

    @TableField(exist = false)
    private ExecResult execResults;

    @TableField(exist = false)
    private Questions questions;

    @TableField(exist = false)
    private Languages languages;

    @TableField(exist = false)
    private User user;

    private static final long serialVersionUID = 1L;
}