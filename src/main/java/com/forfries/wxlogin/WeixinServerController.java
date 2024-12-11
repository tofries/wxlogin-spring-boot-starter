package com.forfries.wxlogin;

import cn.hutool.Hutool;
import cn.hutool.core.util.XmlUtil;
import com.forfries.wxlogin.properties.WeixinProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.Map;

@Slf4j
@RestController
public class WeixinServerController {

    private WeixinLoginService weixinLoginService;
    private WeixinProperties properties;

    public WeixinServerController(WeixinLoginService weixinLoginService, WeixinProperties properties) {
        this.weixinLoginService = weixinLoginService;
        this.properties = properties;
    }


    @GetMapping(produces = "text/plain;charset=utf-8", path = "${weixin.login.verify-path:/wxverify}")
    public String get(@RequestParam(name = "signature", required = false) String signature,
                      @RequestParam(name = "timestamp", required = false) String timestamp,
                      @RequestParam(name = "nonce", required = false) String nonce,
                      @RequestParam(name = "echostr", required = false) String echostr) {
        return echostr;
    }

    @PostMapping(produces = "application/xml; charset=UTF-8", path = "${weixin.login.verify-path:/wxverify}")
    public String post(@RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {


        log.info("\n接收微信请求：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],"
                        + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

        Map<String, Object> weixinMessage = XmlUtil.xmlToMap(requestBody);
        log.info(weixinMessage.toString());

        String reply = "";

        if("subscribe".equals(weixinMessage.get("SCAN"))) {
           reply =  weixinLoginService.markLoginSuccess(openid,weixinMessage.get("EventKey").toString());
        }

        if("subscribe".equals(weixinMessage.get("subscribe"))) {
           reply = weixinLoginService.markSubscribeSuccess(openid,weixinMessage.get("EventKey").toString()
                   .substring("qrscene_".length()));
        }
        //这里简单的做了一个返回
        String wxReply = "success";
        if(reply!=null && !reply.isEmpty()){
            wxReply = MessageFormat.format("<xml>\n" +
                            "  <ToUserName><![CDATA[{0}]]></ToUserName>\n" +
                            "  <FromUserName><![CDATA[{1}]]></FromUserName>\n" +
                            "  <CreateTime>{2}</CreateTime>\n" +
                            "  <MsgType><![CDATA[text]]></MsgType>\n" +
                            "  <Content><![CDATA[{3}]]></Content>\n" +
                            "</xml>",
                    weixinMessage.get("FromUserName").toString(),
                    weixinMessage.get("ToUserName").toString(),
                    weixinMessage.get("CreateTime").toString(),
                    reply);
        }

        return wxReply;
    }

}
