package cn.guet.ksmvcmain.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class SubDateDetails{


    @JsonFormat(pattern = "yyyy/MM/dd HH", timezone = "GMT+8")
    @Schema(description = "起始时间")
    private Date startTime;

    @JsonFormat(pattern = "yyyy/MM/dd HH", timezone = "GMT+8")
    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "预约用户")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer userId;

    @Schema(description = "预约事项")
    private String todoThing;

    @Schema(description = "备注")
    private String todoLog;
}
