package cn.guet.ksmvcmain.entity;

import cn.guet.ksmvcmain.entity.vo.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class QdComment {

    private Integer commentId;

    private String commentContent;

    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8")
    private Date updateTime;

    private String commentImg;

    private Integer userId;

    private Integer targetUserId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer oucId;

    private User user;

    private User TargetUser;

    private List<QdComment> childrenComment;
}
