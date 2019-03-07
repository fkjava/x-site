package org.fkjava.sms.controller;

import org.fkjava.common.data.domain.Result;
import org.fkjava.sms.service.ShortMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
@RequestMapping("/sms/verify")
public class SMSVerifyCodeController {


    @Autowired
    private ShortMessageService shortMessageService;

    /**
     * @param phone   手机号码
     * @param request Spring MVC封装的请求对象，用于获取Session的ID
     * @return 返回图片下载，生成一个图片验证码
     */
    @RequestMapping
    public Result code(@RequestParam(name = "phone") String phone, WebRequest request) {
        String sessionId = request.getSessionId();
        shortMessageService.sendVerifyCode(sessionId, phone);
        return Result.ok("短信已经发送到 " + phone + "，请在15分钟内输入验证码");
    }
}
