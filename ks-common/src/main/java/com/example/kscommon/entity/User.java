package com.example.kscommon.entity;

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
public class User {

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

    private List<Role> role;

}
