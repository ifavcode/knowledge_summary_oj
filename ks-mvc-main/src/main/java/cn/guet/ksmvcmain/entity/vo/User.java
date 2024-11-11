package cn.guet.ksmvcmain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer userId;

    private String userName;

    private String nickName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String userDesc;

    private String userAvatar;

    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8")
    private Date finalUpTime;

    private Character isDelete;

    @TableField(exist = false)
    private List<Role> role;

}
