package cn.guet.ksmvcmain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Integer roleId;

    private String roleName;

    private String roleAuth;

    private Character isDel;
}
