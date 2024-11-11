package cn.guet.ksmvcmain.entity.vo;

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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserInfo {

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String nickName;

    @TableField(exist = false)
    private String userDesc;

    @TableField(exist = false)
    private String userAvatar;

    @TableId
    private Integer userId;

    @Schema(description = "地址")
    private String userAddr;

    @Schema(description = "性别")
    private Character userSex;

    @Schema(description = "手机号码")
    private String userPhone;

    @Schema(description = "ip地址")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String ipAddr;

    @Schema(description = "学习")
    private String userSchool;

    @Schema(description = "用户角色")
    @TableField(exist = false)
    private List<Role> roles;

    @Schema(description = "活跃时间")
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8")
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date finalUpTime;

    public UserInfo(String userName, String nickName, String userDesc, String userAvatar) {
        this.userName = userName;
        this.nickName = nickName;
        this.userDesc = userDesc;
        this.userAvatar = userAvatar;
    }
}
