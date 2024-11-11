package cn.guet.ksmvcmain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class ViewStatistics {

    private Integer userId;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private Date startTime;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private Date endTime;
}
