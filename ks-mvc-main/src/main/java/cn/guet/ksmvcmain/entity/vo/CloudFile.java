package cn.guet.ksmvcmain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @TableName cloud_file
 */
@TableName(value ="cloud_file")
@Data
@Accessors(chain = true)
public class CloudFile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String fileName;

    private Long fileSize;

    private String fileUrl;

    private Date createTime;

    private String delFlag;

    private Integer userId;

    private String filePath;

    private String dirFlag;

    private static final long serialVersionUID = 1L;
}