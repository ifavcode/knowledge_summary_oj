package com.example.kscommon.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Role {

    private Integer roleId;

    private String roleName;

    private String roleAuth;

    private Character isDel;
}
