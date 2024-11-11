package cn.guet.ksmvcmain.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@XStreamAlias("wxMag")
public class WxMsg implements Serializable {

    @XStreamAlias("url")
    private String URL;

    @XStreamAlias("toUserName")
    private String ToUserName;

    @XStreamAlias("fromUserName")
    private String FromUserName;

    @XStreamAlias("createTime")
    private Long CreateTime;

    @XStreamAlias("msgType")
    private String MsgType;

    @XStreamAlias("content")
    private String Content;

    @XStreamAlias("msgId")
    private Long MsgId;

    @XStreamAlias("msgDataId")
    private Long MsgDataId;

    @XStreamAlias("idx")
    private Long Idx;

    @XStreamAlias("eventKey")
    private String EventKey;

    @XStreamAlias("event")
    private String Event;

    @XStreamAlias("msgID")
    private String MsgID;

    @XStreamAlias("status")
    private String Status;
}
