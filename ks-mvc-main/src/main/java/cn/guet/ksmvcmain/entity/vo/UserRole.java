package cn.guet.ksmvcmain.entity.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRole {

    private Integer userId;

    private Integer roleId;
}
