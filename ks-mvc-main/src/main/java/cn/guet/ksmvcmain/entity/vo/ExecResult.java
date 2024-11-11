package cn.guet.ksmvcmain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName exec_result
 */
@TableName(value = "exec_result")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecResult implements Serializable {

    @TableId
    private Integer submissionId;

    private Integer acCnt = 0;

    private Integer teCnt = 0;

    private Integer waCnt = 0;

    private Integer reCnt = 0;

    private Integer ceCnt = 0;

    private Integer otherCnt = 0;

    private Date updateTime;

    private String detailsMore;

    private BigDecimal totalMemory;

    private BigDecimal totalTime;

    public ExecResult(Integer submissionId, Integer acCnt, Integer teCnt, Integer waCnt, Integer reCnt, Integer ceCnt,
                      Integer otherCnt, Date updateTime, String detailsMore, BigDecimal totalMemory, BigDecimal totalTime) {
        this.submissionId = submissionId;
        this.acCnt = acCnt;
        this.teCnt = teCnt;
        this.waCnt = waCnt;
        this.reCnt = reCnt;
        this.ceCnt = ceCnt;
        this.otherCnt = otherCnt;
        this.updateTime = updateTime;
        this.detailsMore = detailsMore;
        this.totalMemory = totalMemory;
        this.totalTime = totalTime;
    }

    @TableField(exist = false)
    private Submissions submissions;

    public int getTotalCnt() {
        return acCnt + teCnt + waCnt + reCnt + ceCnt + otherCnt;
    }

    public String[] getMoreCntAndName() {
        int mx = acCnt;
        String name = "AC";
        if (teCnt > mx) {
            mx = teCnt;
            name = "TE";
        }
        if (waCnt > mx) {
            mx = waCnt;
            name = "WA";
        }
        if (reCnt > mx) {
            mx = reCnt;
            name = "RE";
        }
        if (ceCnt > mx) {
            mx = ceCnt;
            name = "CE";
        }
        if (otherCnt > mx) {
            mx = otherCnt;
            name = "ERROR";
        }
        return new String[]{name, String.valueOf(mx)};
    }

    private static final long serialVersionUID = 1L;
}