package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.entity.WxMsg;
import com.alibaba.fastjson2.JSONObject;
import com.thoughtworks.xstream.XStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/greeting")
    public String handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String s = sb.toString().replace("xml", "cn.guet.ksmvcmain.entity.WxMsg");
        WxMsg wxMsg = xmlToBean(s, WxMsg.class);
        XStream xStream = new XStream();
        xStream.alias("xml", WxMsg.class);
        WxMsg repay = new WxMsg();
        repay.setFromUserName("<![CDATA[" + wxMsg.getToUserName() + "]]>");
        repay.setToUserName("<![CDATA[" + wxMsg.getFromUserName() + "]]>");
        repay.setCreateTime(wxMsg.getCreateTime());
        repay.setMsgType("<![CDATA[text]]>");
        if (StringUtils.isNotEmpty(wxMsg.getEvent())) {
            if (wxMsg.getEvent().equals("subscribe")) {
                repay.setContent("<![CDATA[感谢订阅]]>");
                String xml = xStream.toXML(repay);
                xml = xml.replace("&lt;", "<").replace("&gt;", ">");
                System.out.println(xml);
                return xml;
            } else {
                return "";
            }
        } else {
            repay.setContent("<![CDATA[黄香云不要洗澡了！]]>");
            String xml = xStream.toXML(repay);
            xml = xml.replace("&lt;", "<").replace("&gt;", ">");
            System.out.println(xml);
            return xml;
        }
    }

    public static <T> T xmlToBean(String xmlStr, Class<T> cls) {
        XStream xstream = new XStream();
        //不使用默认的类加载器，需要手动设置类加载器
        xstream.setClassLoader(cls.getClassLoader());
        xstream.processAnnotations(cls);
        xstream.allowTypesByRegExp(new String[]{".*"});
        Object obj = xstream.fromXML(xmlStr);
        return (T) obj;
    }

    private void check(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = "rukoujihua233";
        // 微信加密签名
        String signature = request.getParameter("signature");
        // 随机字符串
        String echostr = request.getParameter("echostr");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        String[] str = {token, timestamp, nonce};
        // 字典排序
        Arrays.sort(str);
        String bigStr = str[0] + str[1] + str[2];
        // SHA1加密
        String digest = SHA1(bigStr);
        // 确认请求来至微信
        if (digest.equals(signature)) {
            response.getWriter().print(echostr);
        } else {
            response.getWriter().print("错误");
        }
    }

    ;

    @PostMapping("/receiveMsg")
    public WxMsg receive(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        WxMsg wxMsg = JSONObject.parseObject(sb.toString(), WxMsg.class);
        WxMsg repay = new WxMsg();
        repay.setFromUserName("Bo0Jin0817");
//        repay.setToUserName("o6s2G6mmjhxb_m6zMnbAjFR845Ic");
        repay.setToUserName("o6s2G6upAQJr60oV1lvXi1h81Akw");
        repay.setCreateTime(System.currentTimeMillis());
        repay.setMsgType("text");
        repay.setContent("黄香云大笨蛋");
        return wxMsg;
    }

    private String SHA1(String originalString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(originalString.getBytes());

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "error";
    }


}
