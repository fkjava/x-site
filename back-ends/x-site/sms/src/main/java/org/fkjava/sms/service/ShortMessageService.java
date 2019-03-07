package org.fkjava.sms.service;

public interface ShortMessageService {

    /**
     * 生成一个图片验证码，验证码存储在Redis数据库里面，有效时间为15分钟。<br/>
     * 如果一个手机号调用了多次发送验证码的方法，会生成多个验证码存储起来。
     *
     * @param sessionId 用户的会话ID
     * @param phone     用户输入的手机号码
     * @return 短信发送成功返回true
     */
    boolean sendVerifyCode(String sessionId, String phone);

    /**
     * 验证用户输入的验证码是否正确，验证通过以后同一个Session、同一个手机号码的所有验证码全部删除。
     *
     * @param sessionId 用户的会话ID
     * @param phone     用户输入的手机号码
     * @param userCode  用户输入的手机号码
     * @return 验证通过返回true
     */
    boolean verify(String sessionId, String phone, String userCode);
}
